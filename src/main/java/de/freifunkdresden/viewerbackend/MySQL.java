/*
 * The MIT License
 *
 * Copyright 2017 Niklas Merkelt.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.freifunkdresden.viewerbackend;

import de.freifunkdresden.viewerbackend.exception.DatabaseConnectionException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL {

    private static final Logger LOGGER = LogManager.getLogger(MySQL.class);

    private final String host;
    private final short port;
    private final String username;
    private final String password;
    private final String database;

    private Connection conn;

    public MySQL() {
        host = DataGen.getConfig().getValue("mysql_host");
        port = Short.parseShort(DataGen.getConfig().getValue("mysql_port"));
        username = DataGen.getConfig().getValue("mysql_username");
        password = DataGen.getConfig().getValue("mysql_password");
        database = DataGen.getConfig().getValue("mysql_database");
    }

    public void openConnection() {
        try {
            this.conn = DriverManager.getConnection("jdbc:mariadb://" + host + ":" + port + "/" + database, username, password);
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Could not connect to database", e);
        }
    }

    public boolean hasConnection() {
        try {
            return this.conn != null && this.conn.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }

    public void checkConnection() {
        if (!hasConnection()) {
            throw new DatabaseConnectionException("Not connected to database - type: mysql");
        }
    }

    public void queryUpdate(String query, @NotNull Object... args) {
        if (DataGen.isReadOnly()) {
            return;
        }
        checkConnection();
        try (PreparedStatement st = conn.prepareStatement(query)) {
            int i = 1;
            for (Object o : args) {
                st.setObject(i, o);
                i++;
            }
            st.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, "Failed to send update: {} - {}", query, e.getLocalizedMessage());
        }
    }

    public PreparedUpdate queryPrepUpdate(String query) {
        if (DataGen.isReadOnly()) {
            return null;
        }
        checkConnection();
        try (PreparedStatement st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            return new PreparedUpdate(st);
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, String.format("Failed to send update: %s", query), e);
            return null;
        }
    }

    public ResultSet querySelect(String query, @NotNull Object... args) {
        checkConnection();
        try (PreparedStatement st = conn.prepareStatement(query)) {
            int i = 1;
            for (Object o : args) {
                st.setObject(i, o);
                i++;
            }
            return querySelect(st);
        } catch (SQLException ex) {
            LOGGER.log(Level.ERROR, "Error trying to build Prepared Statement", ex);
            return null;
        }
    }

    @Nullable
    private ResultSet querySelect(@NotNull PreparedStatement st) {
        try {
            return st.executeQuery();
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, String.format("Failed to send SELECT query: %s", st), e);
            return null;
        }
    }

    public void closeConnection() {
        try {
            if (this.conn != null) {
                this.conn.close();
            }
        } catch (SQLException ignored) {
            // Empty on purpose
        } finally {
            this.conn = null;
        }
    }

    public static class PreparedUpdate {

        private final PreparedStatement stmt;

        public PreparedUpdate(PreparedStatement statement) {
            stmt = statement;
        }

        public PreparedUpdate add(@NotNull Object... args) {
            try {
                int i = 1;
                for (Object o : args) {
                    stmt.setObject(i, o);
                    i++;
                }
                stmt.executeUpdate();
            } catch (SQLException ex) {
                LOGGER.log(Level.ERROR, "", ex);
            }
            return this;
        }

        public void done() {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                    // Empty on purpose
                }
            }
        }
    }
}
