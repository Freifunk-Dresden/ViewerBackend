/*
 * The MIT License
 *
 * Copyright 2020 Niklas Merkelt.
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

package de.freifunkdresden.viewerbackend.datasource;

import de.freifunkdresden.viewerbackend.Airtime;
import de.freifunkdresden.viewerbackend.DataGen;
import de.freifunkdresden.viewerbackend.Node;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AirtimeSQL {

    private static final Logger LOGGER = LogManager.getLogger(AirtimeSQL.class);

    private AirtimeSQL() {
    }

    @Nullable
    public static Airtime getAirtime2G(Node n) {
        return getAirtime(n, 2);
    }

    @Nullable
    public static Airtime getAirtime5G(Node n) {
        return getAirtime(n, 5);
    }

    public static void updateAirtime2G(@NotNull Node n) {
        n.getAirtime2g().ifPresent(airtime -> updateAirtime(n.getId(), 2, airtime));
    }

    public static void updateAirtime5G(@NotNull Node n) {
        n.getAirtime5g().ifPresent(airtime -> updateAirtime(n.getId(), 5, airtime));
    }

    @Nullable
    private static Airtime getAirtime(@NotNull Node n, int type) {
        try (ResultSet rs = DataGen.getDB().querySelect("SELECT * FROM airtime WHERE id = ? AND type = ?",
                n.getId(), type)) {
            if (rs.next()) {
                long active = rs.getLong("active");
                long busy = rs.getLong("busy");
                long receive = rs.getLong("receive");
                long transmit = rs.getLong("transmit");
                return new Airtime(active, busy, receive, transmit);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.ERROR, String.format("DB Airtime %d", n.getId()), ex);
        }
        return null;
    }

    private static void updateAirtime(int id, int type, @NotNull Airtime at) {
        DataGen.getDB().queryUpdate("INSERT INTO airtime SET id = ?, type = ?, active = ?, busy = ?, " +
                        "receive = ?, transmit = ? ON DUPLICATE KEY UPDATE active = ?, busy = ?, receive = ?, " +
                        "transmit = ?",
                id, type, at.active(), at.busy(), at.receive(), at.transmit(), at.active(), at.busy(),
                at.receive(), at.transmit());
    }
}
