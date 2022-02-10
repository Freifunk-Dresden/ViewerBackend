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
import de.freifunkdresden.viewerbackend.dataparser.DataParserSysInfo;
import de.freifunkdresden.viewerbackend.dataparser.TrafficInfo;
import de.freifunkdresden.viewerbackend.datasource.AirtimeSQL;
import de.freifunkdresden.viewerbackend.stats.StatsSQL;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class Node {

    private static final long DAYS_30 = 1000L * 60 * 60 * 24 * 30;
    private static final Set<String> IGNORE_AIRTIME = Set.of("ipq40xx/generic", "ramips/rt305x");

    private final int id;
    private DataParserAPI dpApi;
    private DataParserDB dpDatabase;
    private DataParserSysInfo dpSysInfo;

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

    public void setDpSysInfo(DataParserSysInfo dp) {
        this.dpSysInfo = dp;
    }

    public int getId() {
        return id;
    }

    public boolean isValid() {
        return !isTemporaryNode();
    }

    public boolean isDisplayed() {
        //display only nodes last seen within the last 30 days
        return isValid() && (getLastSeen() > System.currentTimeMillis() - DAYS_30);
    }

    public boolean isShown() {
        switch (getRole()) {
            case STANDARD:
                return hasValidLocation();
            case MOBILE:
                return hasValidLocation() && isOnline() 
                        && isFWVersionHigher(6, 0, 9);
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
        return dpSysInfo != null;
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
        if (dpSysInfo != null) {
            return dpSysInfo.getAutoUpdate();
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

    public boolean isFWVersionHigher(int major, int minor, int patch) {
        String[] fw = getFirmwareVersion().split("\\.");
        if (fw.length == 3) {
            if (Integer.parseInt(fw[0]) > major) {
                return true;
            } else if (Integer.parseInt(fw[0]) == major 
                    && Integer.parseInt(fw[1]) > minor) {
                return true;
            } else if (Integer.parseInt(fw[0]) == major 
                    && Integer.parseInt(fw[1]) == minor 
                    && Integer.parseInt(fw[2]) >= patch) {
                return true;
            }
        }
        return false;
    }

    public Collection<Link> getLinks() {
        if (dpSysInfo != null) {
            return dpSysInfo.getLinkSet();
        }
        return Collections.emptySet();
    }

    public int getFastDCount() {
        if (dpSysInfo != null) {
            return dpSysInfo.getLinkCountFastD();
        }
        return 0;
    }

    public int getWireGuardCount() {
        if (dpSysInfo != null) {
            return dpSysInfo.getLinkCountWireGuard();
        }
        return 0;
    }

    public short getClients() {
        if (dpSysInfo != null) {
            return dpSysInfo.getClients();
        }
        return 0;
    }

    public double getMemoryUsage() {
        if (dpSysInfo != null) {
            return dpSysInfo.getMemoryUsage();
        }
        return 0;
    }

    public float getLoadAvg() {
        if (dpSysInfo != null) {
            return dpSysInfo.getLoadAvg();
        }
        return 0;
    }

    public float getUptime() {
        if (dpSysInfo != null) {
            return dpSysInfo.getUptime();
        }
        return 0;
    }

    public int getNproc() {
        if (dpSysInfo != null) {
            return dpSysInfo.getCPUCount();
        }
        return 0;
    }

    public String getFirmwareVersion() {
        if (dpSysInfo != null) {
            return dpSysInfo.getFirmwareVersion();
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
        if (dpSysInfo != null) {
            return dpSysInfo.getFirmwareBase();
        }
        if (dpDatabase != null) {
            return dpDatabase.getFirmwareBase();
        }
        return null;
    }

    public Community getCommunity() {
        if (dpSysInfo != null) {
            return dpSysInfo.getCommunity();
        }
        if (dpDatabase != null) {
            return dpDatabase.getCommunity();
        }
        return Community.DEFAULT;
    }

    public Node getGateway() {
        if (dpSysInfo != null) {
            return dpSysInfo.getGateway();
        }
        return null;
    }

    public NodeType getRole() {
        if (dpSysInfo != null) {
            return dpSysInfo.getRole();
        }
        if (dpDatabase != null) {
            return dpDatabase.getRole();
        }
        return NodeType.STANDARD;
    }

    public String getName() {
        if (dpSysInfo != null) {
            return dpSysInfo.getName();
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
        if (dpSysInfo != null) {
            return dpSysInfo.getEMail();
        }
        if (dpDatabase != null) {
            return dpDatabase.getEMail();
        }
        return null;
    }

    public String getModel() {
        if (dpSysInfo != null) {
            return dpSysInfo.getModel();
        }
        if (dpApi != null) {
            return dpApi.getModel();
        }
        if (dpDatabase != null) {
            return dpDatabase.getModel();
        }
        return null;
    }

    public String getManufacturer() {
        String model = this.getModel();
        if (model.toLowerCase().contains("fujitsu")) {
            return "FUJITSU SIEMENS";
        } else if (model.toLowerCase().contains("ubnt")) {
            return "Ubiquiti";
        } else if (model.toLowerCase().contains("newifi")) {
            return "Newifi";
        } else if (model.toLowerCase().contains("vmware, inc.")) {
            return "VMware, Inc.";
        } else if (model.toLowerCase().contains("western digital")) {
            return "Western Digital";
        } else if (model.toLowerCase().contains("tp-link")) {
            return "TP-Link";
        } else if (model.toLowerCase().contains("netgear")) {
            return "Netgear";
        } else {
            return model.split(" ")[0];
        }
    }
    
    public Location getLocation() {
        if (dpSysInfo != null) {
            return dpSysInfo.getLocation();
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
        long lastSeenApi = 0;
        long lastSeenDatabase = 0;
        if (dpSysInfo != null) {
            return dpSysInfo.getLastSeen();
        }
        if (dpApi != null) {
            lastSeenApi = dpApi.getLastSeen();
        }
        if (dpDatabase != null) {
            lastSeenDatabase = dpDatabase.getLastSeen();
        }
        return Math.max(lastSeenDatabase, lastSeenApi);
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
        if (dpSysInfo != null) {
            if (IGNORE_AIRTIME.contains(dpSysInfo.getFirmwareTarget())) {
                return Airtime.EMPTY;
            }
            return dpSysInfo.getAirtime2g();
        }
        return Airtime.EMPTY;
    }

    public Airtime getAirtime5g() {
        if (dpSysInfo != null) {
            if (IGNORE_AIRTIME.contains(dpSysInfo.getFirmwareTarget())) {
                return Airtime.EMPTY;
            }
            return dpSysInfo.getAirtime5g();
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
        if (dpSysInfo != null) {
            return dpSysInfo.getTraffic();
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
        StatsSQL.addModel(getModel(), getManufacturer());
        if (isNormalNode()) {
            StatsSQL.addGatewayUsage(getGateway());
            StatsSQL.addGatewayUsageClients(getGateway(), getClients());
        }
        VPN vpn = VPN.getVPN(id);
        if (vpn != null) {
            StatsSQL.addVpnUsage(vpn, getLinks().size());
            StatsSQL.addVpnUsageFastD(vpn, getFastDCount());
            StatsSQL.addVpnUsageWireGuard(vpn, getWireGuardCount());
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
