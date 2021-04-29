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

import de.freifunkdresden.viewerbackend.dataparser.DataParserAPI;
import de.freifunkdresden.viewerbackend.dataparser.DataParserDB;
import de.freifunkdresden.viewerbackend.dataparser.DataParserSysinfo;
import de.freifunkdresden.viewerbackend.dataparser.TrafficInfo;
import de.freifunkdresden.viewerbackend.datasource.AirtimeSQL;
import de.freifunkdresden.viewerbackend.stats.StatsSQL;

import java.util.Collection;
import java.util.Collections;

public class Node {

    private final int id;
    private DataParserAPI dpApi;
    private DataParserDB dpDatabase;
    private DataParserSysinfo dpSysinfo;

    private Airtime airtime2GOld;
    private Airtime airtime5GOld;

    public Node(int id) {
        this.id = id;
    }

    public String getIpAddress() {
        return String.format("10.200.%s.%s", (id / 255), ((id % 255) + 1));
    }

    public void setDpApi(DataParserAPI dp) {
        this.dpApi = dp;
    }

    public void setDpDatabase(DataParserDB dp) {
        this.dpDatabase = dp;
        airtime2GOld = AirtimeSQL.getAirtime2G(this);
        airtime5GOld = AirtimeSQL.getAirtime5G(this);
    }

    public void setDpSysinfo(DataParserSysinfo dp) {
        this.dpSysinfo = dp;
    }

    public int getId() {
        return id;
    }

    public boolean isValid() {
        return !isTemporaryNode();
    }

    public boolean isDisplayed() {
        //display only nodes last seen within the last 30 days
        return isValid() && (getLastSeen() > System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 30));
    }

    public boolean isShown() {
        switch (getRole()) {
            case STANDARD:
                return hasValidLocation();
            case MOBILE:
                return hasValidLocation() && isOnline() && isFWVersionHigher(0, 9);
            case SERVER:
            default:
                return false;
        }
    }

    public boolean canHasClients() {
        switch (getRole()) {
            case STANDARD:
            case MOBILE:
                return true;
            case SERVER:
            default:
                return false;
        }
    }

    public boolean isOnline() {
        return dpSysinfo != null;
    }

    public boolean isGateway() {
        if (dpApi != null) {
            return dpApi.isGateway();
        }
        return false;
    }

    public boolean hasBackbone() {
        if (dpApi != null) {
            return dpApi.hasBackbone();
        }
        return false;
    }

    public boolean isAutoUpdateEnabled() {
        if (dpSysinfo != null) {
            return dpSysinfo.getAutoUpdate();
        }
        if (dpApi != null) {
            return dpApi.getAutoUpdate();
        }
        if (dpDatabase != null) {
            return dpDatabase.getAutoUpdate();
        }
        return false;
    }

    public boolean isServerNode() {
        return id <= 256;
    }

    public boolean isTemporaryNode() {
        return id >= 900 && id < 1000;
    }

    public boolean isNormalNode() {
        return id > 1000 && id < 51000;
    }

    public boolean isAlternativeNode() {
        return id > 51000 && id < 60000;
    }

    public boolean isFWVersionHigher(int minor, int patch) {
        String[] fw = getFirmwareVersion().split("\\.");
        if (fw.length == 3) {
            if (Integer.parseInt(fw[1]) > minor) {
                return true;
            } else if (Integer.parseInt(fw[1]) == minor && Integer.parseInt(fw[2]) >= patch) {
                return true;
            }
        }
        return false;
    }

    public Collection<Link> getLinks() {
        if (dpSysinfo != null) {
            return dpSysinfo.getLinkSet();
        }
        return Collections.emptySet();
    }

    public short getClients() {
        if (dpSysinfo != null) {
            return dpSysinfo.getClients();
        }
        return 0;
    }

    public double getMemoryUsage() {
        if (dpSysinfo != null) {
            return dpSysinfo.getMemoryUsage();
        }
        return 0;
    }

    public float getLoadAvg() {
        if (dpSysinfo != null) {
            return dpSysinfo.getLoadAvg();
        }
        return 0;
    }

    public float getUptime() {
        if (dpSysinfo != null) {
            return dpSysinfo.getUptime();
        }
        return 0;
    }

    public int getNproc() {
        if (dpSysinfo != null) {
            return dpSysinfo.getCPUCount();
        }
        return 0;
    }

    public String getFirmwareVersion() {
        if (dpSysinfo != null) {
            return dpSysinfo.getFirmwareVersion();
        }
        if (dpApi != null) {
            return dpApi.getFirmwareVersion();
        }
        if (dpDatabase != null) {
            return dpDatabase.getFirmwareVersion();
        }
        return null;
    }

    public String getFirmwareBase() {
        if (dpSysinfo != null) {
            return dpSysinfo.getFirmwareBase();
        }
        if (dpDatabase != null) {
            return dpDatabase.getFirmwareBase();
        }
        return null;
    }

    public Community getCommunity() {
        if (dpSysinfo != null) {
            return dpSysinfo.getCommunity();
        }
        if (dpDatabase != null) {
            return dpDatabase.getCommunity();
        }
        return Community.DEFAULT;
    }

    public Node getGateway() {
        if (dpSysinfo != null) {
            return dpSysinfo.getGateway();
        }
        return null;
    }

    public NodeType getRole() {
        if (dpSysinfo != null) {
            return dpSysinfo.getRole();
        }
        if (dpDatabase != null) {
            return dpDatabase.getRole();
        }
        return NodeType.STANDARD;
    }

    public String getName() {
        if (dpSysinfo != null) {
            return dpSysinfo.getName();
        }
        if (dpApi != null) {
            return dpApi.getName();
        }
        if (dpDatabase != null) {
            return dpDatabase.getName();
        }
        return null;
    }

    public String getEMail() {
        if (dpSysinfo != null) {
            return dpSysinfo.getEMail();
        }
        if (dpDatabase != null) {
            return dpDatabase.getEMail();
        }
        return null;
    }

    public String getModel() {
        if (dpSysinfo != null) {
            return dpSysinfo.getModel();
        }
        if (dpApi != null) {
            return dpApi.getModel();
        }
        if (dpDatabase != null) {
            return dpDatabase.getModel();
        }
        return null;
    }

    public Location getLocation() {
        if (dpSysinfo != null) {
            return dpSysinfo.getLocation();
        }
        if (dpApi != null) {
            return dpApi.getLocation();
        }
        if (dpDatabase != null) {
            return dpDatabase.getLocation();
        }
        return null;
    }

    public long getLastSeen() {
        if (dpSysinfo != null) {
            return dpSysinfo.getLastSeen();
        }
        if (dpApi != null) {
            return dpApi.getLastSeen();
        }
        if (dpDatabase != null) {
            return dpDatabase.getLastSeen();
        }
        return 0;
    }

    public long getFirstSeen() {
        if (dpApi != null) {
            return dpApi.getFirstSeen();
        }
        if (dpDatabase != null) {
            return dpDatabase.getFirstSeen();
        }
        return 0;
    }

    public Airtime getAirtime2g() {
        if (dpSysinfo != null) {
            if (dpSysinfo.getFirmwareTarget().equals("ipq40xx/generic")) {
                return Airtime.EMPTY;
            }
            return dpSysinfo.getAirtime2g();
        }
        return Airtime.EMPTY;
    }

    public Airtime getAirtime5g() {
        if (dpSysinfo != null) {
            if (dpSysinfo.getFirmwareTarget().equals("ipq40xx/generic")) {
                return Airtime.EMPTY;
            }
            return dpSysinfo.getAirtime5g();
        }
        return Airtime.EMPTY;
    }

    public Airtime getAirtime2GOld() {
        return airtime2GOld;
    }

    public Airtime getAirtime5GOld() {
        return airtime5GOld;
    }

    public TrafficInfo getTraffic() {
        if (dpSysinfo != null) {
            return dpSysinfo.getTraffic();
        }
        return new TrafficInfo();
    }

    public String getHostname() {
        String name = getName();
        return (name == null || name.isEmpty()) ? String.valueOf(id) : id + "-" + name;
    }

    public String getFakeMac() {
        int third = id / 255;
        int fourth = (id % 255) + 1;
        return "ff:dd:00:00:" + String.format("%02x", third) + ":" + String.format("%02x", fourth);
    }

    public String getFakeId() {
        int third = id / 255;
        int fourth = (id % 255) + 1;
        return "ffdd0000" + String.format("%02x", third) + String.format("%02x", fourth);
    }

    public boolean hasValidLocation() {
        Location l = getLocation();
        return l != null && l.isValid();
    }

    public void updateDatabase() {
        Location l = getLocation();
        Double lat = null;
        Double lon = null;
        if (hasValidLocation()) {
            lat = l.getLatitude();
            lon = l.getLongitude();
        }
        DataGen.getDB().queryUpdate("CALL updateNode(?,?,?,?,?,?,?,?,?,?,?,?,?,?)", id, lat, lon,
                getCommunity().getName(), getRole().name(), getModel(), getFirmwareVersion(), getFirmwareBase(),
                getFirstSeen() / 1000, getLastSeen() / 1000, isAutoUpdateEnabled(), isGateway(), getName(), getEMail());
        AirtimeSQL.updateAirtime2G(this);
        AirtimeSQL.updateAirtime5G(this);
    }

    public void collectStats() {
        if (!isDisplayed()) return;
        StatsSQL.addVersion(getFirmwareVersion());
        StatsSQL.addCommunity(getCommunity());
        if (isNormalNode()) {
            StatsSQL.addGatewayUsage(getGateway());
            StatsSQL.addGatewayUsageClients(getGateway(), getClients());
        }
        if (isOnline()) {
            StatsSQL.addToStats(this);
        }
        VPN vpn = VPN.getVPN(id);
        if (vpn != null) {
            StatsSQL.addVpnUsage(vpn, getLinks().size());
        }
    }

    public static int convertIpToId(String ip) {
        String[] split = ip.split("\\.");
        if (split.length == 4) {
            int third = Integer.parseInt(split[2]);
            int fourth = Integer.parseInt(split[3]);
            return third * 255 + (fourth - 1);
        }
        return -1;
    }
}
