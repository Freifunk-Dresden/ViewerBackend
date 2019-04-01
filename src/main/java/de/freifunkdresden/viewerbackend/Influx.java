/*
 * The MIT License
 *
 * Copyright 2019 Niklas Merkelt.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

public class Influx {

    private static final Logger LOG = DataGen.getLogger();

    private InfluxDB connection;
    private String url;
    private String username;
    private String password;
    private String database;

    public Influx() {
        if (this.loadConfig()) {
            if (!this.openConnection()) {
                LOG.log(Level.SEVERE, "Connection to database failed!");
            }
        } else {
            LOG.log(Level.SEVERE, "Configuration file couldn't be read!");
        }
    }

    private boolean loadConfig() {
        File f = new File("influx.ini");
        if (!f.exists() || !f.canRead()) {
            return false;
        }
        BufferedReader fr = null;
        try {
            fr = new BufferedReader(new FileReader(f));
            String line;
            while ((line = fr.readLine()) != null) {
                if (line.startsWith("url=")) {
                    url = line.replace("url=", "");
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
            this.connection = InfluxDBFactory.connect(url, username, password);
            this.connection.setDatabase(database);
            return true;
        } catch (IllegalArgumentException e) {
            LOG.log(Level.SEVERE, null, e);
            return false;
        }
    }

    public boolean hasConnection() {
        return this.connection != null;
    }
    
    public void write(Point p) {
        this.connection.write(p);
    }
    
    public void write(Collection<Point> points) {
        this.connection.write(BatchPoints.builder().points(points).build());
    }
    
    public void closeConnection() {
        this.connection.close();
        this.connection = null;
    }
}
