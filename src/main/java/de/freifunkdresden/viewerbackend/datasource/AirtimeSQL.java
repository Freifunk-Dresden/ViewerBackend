package de.freifunkdresden.viewerbackend.datasource;

import de.freifunkdresden.viewerbackend.Airtime;
import de.freifunkdresden.viewerbackend.DataGen;
import de.freifunkdresden.viewerbackend.Node;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AirtimeSQL {

    private static final Logger LOGGER = LogManager.getLogger(AirtimeSQL.class);

    public static Airtime getAirtime2G(Node n) {
        return getAirtime(n, 2);
    }

    public static Airtime getAirtime5G(Node n) {
        return getAirtime(n, 5);
    }

    public static void updateAirtime2G(Node n) {
        Airtime at = n.getAirtime2g();
        if (!Airtime.EMPTY.equals(at)) {
            updateAirtime(n.getId(), 2, at);
        }
    }

    public static void updateAirtime5G(Node n) {
        Airtime at = n.getAirtime5g();
        if (!Airtime.EMPTY.equals(at)) {
            updateAirtime(n.getId(), 5, at);
        }
    }

    private static Airtime getAirtime(Node n, int type) {
        try (ResultSet rs = DataGen.getDB().querySelect("SELECT * FROM airtime WHERE id = ? AND type = ?",
                n.getId(), type)) {
            if (rs.next()) {
                int active = rs.getInt("active");
                int busy = rs.getInt("busy");
                int receive = rs.getInt("receive");
                int transmit = rs.getInt("transmit");
                return new Airtime(active, busy, receive, transmit);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.ERROR, String.format("DB Airtime %d", n.getId()), ex);
        }
        return Airtime.EMPTY;
    }

    private static void updateAirtime(int id, int type, Airtime at) {
        DataGen.getDB().queryUpdate("INSERT INTO airtime SET id = ?, type = ?, active = ?, busy = ?, " +
                        "receive = ?, transmit = ? ON DUPLICATE KEY UPDATE active = ?, busy = ?, receive = ?, " +
                        "transmit = ?",
                id, type, at.getActive(), at.getBusy(), at.getReceive(), at.getTransmit(), at.getActive(), at.getBusy(),
                at.getReceive(), at.getTransmit());
    }
}
