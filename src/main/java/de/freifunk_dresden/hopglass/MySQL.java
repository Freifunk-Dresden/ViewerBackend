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

package de.freifunk_dresden.hopglass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQL {

    private Connection conn;
    private static final Logger LOG = Logger.getLogger(MySQL.class.getName());

    public MySQL() {
        if (!this.openConnection()) {
            LOG.log(Level.SEVERE, "Keine Datenbankverbindung!");
        }
    }

    private boolean openConnection() {
        try {
            //@TODO: load from config file
            String host = "localhost";
            int port = 3306;
            String username = "freifunk";
            String pw = "";
            String dataB = "freifunk";
            Class.forName("com.mysql.jdbc.Driver");
            this.conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dataB, username, pw);
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            LOG.log(Level.SEVERE, null, e);
            return false;
        }
    }

    public boolean hasConnection() {
        try {
            return this.conn != null && this.conn.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }

    public void queryUpdate(String query, Object... args) {
        Connection connLoc = conn;
        PreparedStatement st = null;
        try {
            st = connLoc.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            for (Object o : args) {
                st.setObject(i, o);
                i++;
            }
            st.executeUpdate();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to send update: {0} - {1}", new Object[] { query, e.getLocalizedMessage() });
        }
        closeRessources(st);
    }

    public PreparedUpdate queryPrepUpdate(String query) {
        PreparedStatement st;
        try {
            st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            return new PreparedUpdate(st);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to send update: " + query, e);
        }
        return null;
    }

    public ResultSet querySelect(String query, Object... args) {
        try {
            PreparedStatement st = conn.prepareStatement(query);
            int i = 1;
            for (Object o : args) {
                st.setObject(i, o);
                i++;
            }
            return querySelect(st);
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "Error trying to build Prepared Statement", ex);
        }
        return null;
    }

    private ResultSet querySelect(PreparedStatement st) {
        ResultSet rs;
        try {
            rs = st.executeQuery();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to send SELECT query: " + st.toString(), e);
            return null;
        }
        return rs;
    }

    private void closeRessources(PreparedStatement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {}
        }
    }

    public void closeConnection() {
        try {
            if (this.conn != null) {
                this.conn.close();
            }
        } catch (SQLException e) {
        } finally {
            this.conn = null;
        }
    }

    public class PreparedUpdate {

        private final PreparedStatement stmt;

        public PreparedUpdate(PreparedStatement statement) {
            stmt = statement;
        }

        public PreparedUpdate add(Object... args) {
            try {
                int i = 1;
                for (Object o : args) {
                    stmt.setObject(i, o);
                    i++;
                }
                stmt.executeUpdate();
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            return this;
        }

        public void done() {
            closeRessources(stmt);
        }
    }
}
