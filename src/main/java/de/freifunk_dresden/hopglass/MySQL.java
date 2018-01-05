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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
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
    private String host;
    private short port;
    private String username;
    private String password;
    private String database;

    public MySQL() {
        if (this.loadConfig()) {
            if (!this.openConnection()) {
                LOG.log(Level.SEVERE, "No connection to database!");
            }
        } else {
            LOG.log(Level.SEVERE, "Configurationfile couldn't be read!");
        }
    }

    private boolean loadConfig() {
        File f = new File("conf.ini");
        if (!f.exists() || !f.canRead()) {
            return false;
        }
        BufferedReader fr = null;
        try {
            fr = new BufferedReader(new FileReader(f));
            String line;
            while ((line = fr.readLine()) != null) {
                if (line.startsWith("host=")) {
                    host = line.replace("host=", "");
                } else if (line.startsWith("port=")) {
                    port = Short.parseShort(line.replace("port=", ""));
                } else if (line.startsWith("username=")) {
                    username = line.replace("username=", "");
                } else if (line.startsWith("password=")) {
                    password = line.replace("password=", "");
                } else if (line.startsWith("database=")) {
                    database = line.replace("database=", "");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }

    private boolean openConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
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
            LOG.log(Level.SEVERE, "Failed to send update: {0} - {1}", new Object[]{query, e.getLocalizedMessage()});
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
            } catch (SQLException e) {
            }
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
