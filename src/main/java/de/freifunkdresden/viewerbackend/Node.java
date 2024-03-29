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
import de.freifunkdresden.viewerbackend.dataparser.TrafficInfoEmpty;
import de.freifunkdresden.viewerbackend.datasource.AirtimeSQL;
import de.freifunkdresden.viewerbackend.stats.StatsSQL;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class Node {

    private static final long DAYS_30 = 1000L * 60 * 60 * 24 * 30;
    private static final Set<String> IGNORE_AIRTIME = Set.of("ipq40xx/generic", "ramips/rt305x");

    private final int id;
    private InetAddress ip;
    private DataParserAPI dpApi;
    private DataParserDB dpDatabase;
    private DataParserSysInfo dpSysInfo;

    private Airtime airtime2GOld;
    private Airtime airtime5GOld;

    public Node(int id) {
        this.id = id;
    }

    public InetAddress getIpAddress() throws UnknownHostException {
        if (this.ip == null) {
            this.ip = InetAddress.getByAddress(new byte[]{10, (byte) 200, (byte) (id / 255), (byte) ((id % 255) + 1)});
        }
        return ip;
    }

    public String getIpAddressString() throws UnknownHostException {
        return this.getIpAddress().getHostAddress();
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
        return switch (getRole()) {
            case STANDARD -> hasValidLocation();
            case MOBILE -> hasValidLocation() && isOnline()
                    && isFWVersionHigher(6, 0, 9);
            case SERVER -> false;
        };
    }

    public boolean canHasClients() {
        return switch (getRole()) {
            case STANDARD, MOBILE -> true;
            case SERVER -> false;
        };
    }

    public boolean isOnline() {
        return dpSysInfo != null;
    }

    public boolean isGateway() {
        return DataGen.getDataHolder().isGateway(this);
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
        if (dpApi != null && dpDatabase != null) {
            if (dpApi.getLastSeen() > dpDatabase.getLastSeen()) {
                return dpApi.getAutoUpdate();
            } else {
                return dpDatabase.getAutoUpdate();
            }
        }
        if (dpApi != null) {
            return dpApi.getAutoUpdate();
        }
        if (dpDatabase != null) {
            return dpDatabase.getAutoUpdate();
        }
        return false;
    }

    public boolean isTemporaryNode() {
        return id >= 900 && id < 1000;
    }

    public boolean isNormalNode() {
        return id > 1000 && id < 51000;
    }

    public boolean isFWVersionHigher(int major, int minor, int patch) {
        String[] fw = getFirmwareVersion().split("\\.");
        if (fw.length == 3) {
            if (Integer.parseInt(fw[0]) > major) {
                return true;
            }
            if (Integer.parseInt(fw[0]) == major
                    && Integer.parseInt(fw[1]) > minor) {
                return true;
            }
            if (Integer.parseInt(fw[0]) == major
                    && Integer.parseInt(fw[1]) == minor
                    && Integer.parseInt(fw[2]) >= patch) {
                return true;
            }
        }
        return false;
    }

    public Collection<Link> getLinks() {
        if (dpSysInfo != null) {
            dpSysInfo.processLinks();
            return dpSysInfo.getLinkSet();
        }
        return Collections.emptyList();
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

    public int getCpuCount() {
        if (dpSysInfo != null) {
            return dpSysInfo.getCPUCount();
        }
        return 0;
    }

    public String getFirmwareVersion() {
        if (dpSysInfo != null) {
            return dpSysInfo.getFirmwareVersion();
        }
        if (dpApi != null && dpDatabase != null) {
            if (dpApi.getLastSeen() > dpDatabase.getLastSeen()) {
                return dpApi.getFirmwareVersion();
            } else {
                return dpDatabase.getFirmwareVersion();
            }
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

    public String getFirmwareBranch() {
        if (dpSysInfo != null) {
            return dpSysInfo.getFirmwareBranch();
        }
        return null;
    }

    public String getFirmwareGitRev() {
        if (dpSysInfo != null) {
            return dpSysInfo.getFirmwareGitRev();
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
        if (dpApi != null && dpDatabase != null) {
            if (dpApi.getLastSeen() > dpDatabase.getLastSeen()) {
                return dpApi.getName();
            } else {
                return dpDatabase.getName();
            }
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
        if (dpApi != null && dpDatabase != null) {
            if (dpApi.getLastSeen() > dpDatabase.getLastSeen()) {
                return dpApi.getModel();
            } else {
                return dpDatabase.getModel();
            }
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
        if (dpApi != null && dpDatabase != null) {
            if (dpApi.getLastSeen() > dpDatabase.getLastSeen()) {
                return dpApi.getLocation();
            } else {
                return dpDatabase.getLocation();
            }
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

    public Optional<Airtime> getAirtime2g() {
        if (dpSysInfo != null) {
            if (IGNORE_AIRTIME.contains(dpSysInfo.getFirmwareTarget())) {
                return Optional.empty();
            }
            return dpSysInfo.getAirtime2g();
        }
        return Optional.empty();
    }

    public Optional<Airtime> getAirtime5g() {
        if (dpSysInfo != null) {
            if (IGNORE_AIRTIME.contains(dpSysInfo.getFirmwareTarget())) {
                return Optional.empty();
            }
            return dpSysInfo.getAirtime5g();
        }
        return Optional.empty();
    }

    public Optional<Airtime> getAirtime2GOld() {
        return Optional.ofNullable(airtime2GOld);
    }

    public Optional<Airtime> getAirtime5GOld() {
        return Optional.ofNullable(airtime5GOld);
    }

    public Optional<Integer> getWifiChannel2g() {
        if (dpSysInfo != null) {
            return dpSysInfo.getWifiChannel2g();
        }
        return Optional.empty();
    }

    public Optional<Integer> getWifiChannel5g() {
        if (dpSysInfo != null) {
            return dpSysInfo.getWifiChannel5g();
        }
        return Optional.empty();
    }

    public TrafficInfo getTraffic() {
        if (dpSysInfo != null) {
            return dpSysInfo.getTraffic();
        }
        return new TrafficInfoEmpty();
    }

    public String getHostname() {
        String name = getName();
        if (name == null || name.isBlank() || DataGen.getWordFilter().check(name)) {
            return String.valueOf(id);
        }
        return String.format("%d - %s", id, name);
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
        Double lat = null;
        Double lon = null;
        if (hasValidLocation()) {
            Location l = getLocation();
            lat = l.latitude();
            lon = l.longitude();
        }
        String community = getCommunity().getName();
        String role = getRole().name();
        String model = getModel();
        long firstSeen = getFirstSeen() / 1000;
        long lastSeen = getLastSeen() / 1000;
        String name = getName();
        String eMail = getEMail();
        DataGen.getDB().queryUpdate("INSERT INTO nodes " +
                        "SET id = ?, community = ?, role = ?, model = ?, firstseen = ?, lastseen = ?, latitude = ?, " +
                        "longitude = ?, name = ?, email = ? " +
                        "ON DUPLICATE KEY UPDATE community = ?, role = ?, model = ?, firstseen = ?, lastseen = ?, " +
                        "latitude = ?, longitude = ?, name = ?, email = ?",
                id, community, role, model, firstSeen, lastSeen, lat, lon, name, eMail,
                community, role, model, firstSeen, lastSeen, lat, lon, name, eMail);
        String firmwareVersion = getFirmwareVersion();
        String firmwareBranch = getFirmwareBranch();
        String firmwareGitRev = getFirmwareGitRev();
        String firmwareBase = getFirmwareBase();
        boolean autoUpdateEnabled = isAutoUpdateEnabled();
        DataGen.getDB().queryUpdate("INSERT INTO nodes_firmware " +
                        "SET id = ?, version = ?, branch = ?, git_rev = ?, base = ?, auto_update = ? " +
                        "ON DUPLICATE KEY UPDATE version = ?, branch = ?, git_rev = ?, base = ?, auto_update = ?",
                id, firmwareVersion, firmwareBranch, firmwareGitRev, firmwareBase, autoUpdateEnabled,
                firmwareVersion, firmwareBranch, firmwareGitRev, firmwareBase, autoUpdateEnabled);
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

    public static int convertIpToId(@NotNull String ip) {
        String[] split = ip.split("\\.");
        if (split.length == 4) {
            int third = Integer.parseInt(split[2]);
            int fourth = Integer.parseInt(split[3]);
            return third * 255 + (fourth - 1);
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return id == node.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
