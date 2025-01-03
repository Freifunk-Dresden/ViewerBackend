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

import de.freifunkdresden.viewerbackend.exception.DatabaseConnectionException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.Collection;

public class Influx {

    private static final Logger LOGGER = LogManager.getLogger(Influx.class);

    private final String url;
    private final String username;
    private final String password;
    private final String database;

    private InfluxDB connection;

    public Influx() {
        url = DataGen.getConfig().getValue("influx_url");
        username = DataGen.getConfig().getValue("influx_username");
        password = DataGen.getConfig().getValue("influx_password");
        database = DataGen.getConfig().getValue("influx_database");
    }

    public void openConnection() {
        try {
            this.connection = InfluxDBFactory.connect(url, username, password);
            this.connection.setDatabase(database);
        } catch (IllegalArgumentException e) {
            throw new DatabaseConnectionException("Could not connect to database", e);
        }
    }

    public boolean hasConnection() {
        return this.connection != null;
    }

    public void checkConnection() {
        if (!hasConnection()) {
            throw new DatabaseConnectionException("Not connected to database - type: influx");
        }
    }

    public void write(Point p) {
        if (DataGen.isDebug()) {
            LOGGER.log(Level.DEBUG, "[Influx] Write Point: {}", p);
        }
        if (DataGen.isReadOnly()) {
            return;
        }
        checkConnection();
        this.connection.write(p);
    }

    public void write(Collection<Point> points) {
        if (DataGen.isDebug()) {
            for (Point p : points) {
                LOGGER.log(Level.DEBUG, "[Influx] Write Multi-Point: {}", p);
            }
        }
        if (DataGen.isReadOnly()) {
            return;
        }
        checkConnection();
        this.connection.write(BatchPoints.builder().points(points).build());
    }

    public void closeConnection() {
        if (this.connection != null) {
            this.connection.close();
            this.connection = null;
        }
    }
}
