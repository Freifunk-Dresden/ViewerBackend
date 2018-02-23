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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.freifunk_dresden.hopglass.logging.FancyConsoleHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class DataGen {

    public static SimpleDateFormat DATE_MESH = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    public static SimpleDateFormat DATE_HOP = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final ExecutorService POOL = Executors.newFixedThreadPool(10);
    private static final Logger LOG = Logger.getLogger(DataGen.class.getName());
    private static final LinkedHashMap<Integer, Node> NODES = new LinkedHashMap<>();
    private static final HashMap<Integer, HashMap<Integer, Link>> LINKS = new HashMap<>();
    private static MySQL DB;

    private void getNodes() throws IOException {
        URLConnection con = new URL("http://api.freifunk-dresden.de/freifunk-nodes.json").openConnection();
        InputStreamReader reader;
        try (InputStream stream = con.getInputStream()) {
            reader = new InputStreamReader(stream, "UTF-8");
            JsonObject api = new JsonParser().parse(reader).getAsJsonObject();
            reader.close();
            api.get("nodes").getAsJsonArray().forEach((node) -> {
                JsonObject n = node.getAsJsonObject();
                Node nod = getNode(n.get("id").getAsInt());
                JsonObject pos = n.get("position").getAsJsonObject();
                try {
                    nod.setLatitude(pos.get("lat").getAsDouble());
                    nod.setLongitude(pos.get("long").getAsDouble());
                } catch (NumberFormatException ex) {
                }
            });
        }
    }

    private void parseRegister() throws IOException {
        URLConnection register = new URL("http://register.freifunk-dresden.de/").openConnection();
        InputStreamReader reader;
        try (InputStream stream = register.getInputStream()) {
            reader = new InputStreamReader(stream, "UTF-8");
            String html = new BufferedReader(reader).lines().collect(Collectors.joining(" "));
            reader.close();
            Document doc = Jsoup.parse(html);
            Element tbody = doc.select("tbody").first();
            tbody.children().select(".node_db_color0").forEach(this::parseRegister);
            tbody.children().select(".node_db_color2").forEach(this::parseRegister);
        }
    }

    private void parseRegister(Element tr) {
        try {
            Node node = getNode(Integer.parseInt(tr.child(1).child(0).text()));
            node.setGateway(tr.child(3).child(1).attr("alt").equals("Ja"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getDefault());
            String firstseen = tr.child(6).child(0).getElementsByTag("td").get(0).text();
            String lastseen = tr.child(6).child(0).getElementsByTag("td").get(1).text();
            node.setFirstseen(firstseen.isEmpty() ? -1 : sdf.parse(firstseen).getTime());
            node.setLastseen(lastseen.isEmpty() ? -1 : sdf.parse(lastseen).getTime());
        } catch (ParseException ex) {
            Logger.getLogger(DataGen.class.getName()).log(Level.SEVERE, tr.child(1).child(0).text(), ex);
        }
    }

    public static Node getNode(int id) {
        Node n = NODES.get(id);
        if (n == null) {
            n = new Node(id);
            NODES.put(id, n);
        }
        return n;
    }

    public static MySQL getDB() {
        return DB;
    }

    public static Link getLink(int node1, int node2) {
        int min = Math.min(node1, node2);
        int max = Math.max(node1, node2);
        HashMap<Integer, Link> get = LINKS.get(min);
        if (get == null) {
            LINKS.put(min, new HashMap<>());
        }
        return LINKS.get(min).get(max);
    }

    public static void addLink(Link l) {
        int min = Math.min(l.getSource().getId(), l.getTarget().getId());
        int max = Math.max(l.getSource().getId(), l.getTarget().getId());
        if (LINKS.get(min) == null) {
            LINKS.put(min, new HashMap<>());
        }
        LINKS.get(min).put(max, l);
    }

    public static void main(String[] args) {
        DATE_HOP.setTimeZone(TimeZone.getTimeZone("UTC"));
        setupLogging();
        LOG.log(Level.INFO, "Getting connection to DB...");
        DB = new MySQL();
        if (DB.hasConnection()) {
            DB.queryUpdate("CREATE TABLE IF NOT EXISTS `nodes` ( "
                    + " `id` INT(11) NOT NULL, "
                    + "	`community` VARCHAR(50) NULL DEFAULT NULL, "
                    + "	`role` TEXT NULL, "
                    + "	`model` TEXT NULL, "
                    + "	`firmwareVersion` VARCHAR(10) NULL DEFAULT NULL, "
                    + "	`firmwareBase` TEXT NULL, "
                    + "	`firstseen` INT(11) NULL DEFAULT NULL, "
                    + "	`lastseen` INT(11) NULL DEFAULT NULL, "
                    + "	`gatewayIp` TEXT NULL, "
                    + "	`latitude` DOUBLE NULL DEFAULT NULL, "
                    + "	`longitude` DOUBLE NULL DEFAULT NULL, "
                    + "	`uptime` DOUBLE NULL DEFAULT NULL, "
                    + "	`memory_usage` DOUBLE NULL DEFAULT NULL, "
                    + "	`loadavg` DOUBLE NULL DEFAULT NULL, "
                    + "	`clients` INT(4) NULL DEFAULT NULL, "
                    + "	`gateway` INT(1) NULL DEFAULT NULL, "
                    + "	`online` INT(1) NULL DEFAULT NULL, "
                    + "	`name` TEXT NULL, "
                    + "	`email` TEXT NULL, "
                    + "	PRIMARY KEY (`id`) "
                    + ") COLLATE='utf8_general_ci' ENGINE=InnoDB;");
            DataGen dataGen = new DataGen();
            try {
                LOG.log(Level.INFO, "Getting nodes...");
                dataGen.getNodes();
                LOG.log(Level.INFO, "Getting register...");
                dataGen.parseRegister();
            } catch (IOException ex) {
                Logger.getLogger(DataGen.class.getName()).log(Level.SEVERE, null, ex);
            }
            NODES.values().forEach((n) -> POOL.submit(new NodeThread(n)));
            POOL.shutdown();
            LOG.log(Level.INFO, "Wating threads to finish...");
            try {
                POOL.awaitTermination(3, TimeUnit.MINUTES);
            } catch (InterruptedException ex) {
                Logger.getLogger(DataGen.class.getName()).log(Level.SEVERE, null, ex);
            }
            LOG.log(Level.INFO, "Validate nodes...");
            validateNodes();
            LOG.log(Level.INFO, "Collect links...");
            collectLinks();
            LOG.log(Level.INFO, "Generate JSON files...");
            genJson();
            LOG.log(Level.INFO, "Save to database...");
            NODES.values().forEach((node) -> node.updateDatabase());
            LOG.log(Level.INFO, "Done!");
        }
    }

    public static void collectLinks() {
        NODES.values().forEach((node) -> {
            node.getLinks().forEach((link) -> {
                Link lnk = getLink(link.getSource().getId(), link.getTarget().getId());
                if (lnk == null) {
                    addLink(link);
                } else {
                    lnk.setTargetTq(link.getSourceTq());
                }
            });
        });
    }

    public static void validateNodes() {
        NODES.values().forEach((n) -> {
            if (!n.isValid()) {
                ResultSet rs = DB.querySelect("SELECT * FROM nodes WHERE id = ?", n.getId());
                try {
                    if (rs.first()) {
                        n.parseData(rs);
                    }
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public static void genJson() {
        JsonFileGen jfg = new JsonFileGen(NODES.values(), LINKS.values());
        try {
            jfg.genNodes();
            jfg.genGraph();
            jfg.genMeshViewer();
        } catch (IOException ex) {
            Logger.getLogger(DataGen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void setupLogging() {
        for (Handler h : LOG.getHandlers()) {
            LOG.removeHandler(h);
        }
        
        LOG.addHandler(new FancyConsoleHandler());
    }

    public static Logger getLogger() {
        return LOG;
    }
}
