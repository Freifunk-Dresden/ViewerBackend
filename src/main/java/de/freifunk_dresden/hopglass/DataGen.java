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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class DataGen {

    private static final ExecutorService POOL = Executors.newFixedThreadPool(25);
    private static final Logger LOG = Logger.getLogger(DataGen.class.getName());
    private static MySQL DB;

    private void getNodes() throws IOException {
        URLConnection con = new URL("http://api.freifunk-dresden.de/freifunk-nodes.json").openConnection();
        InputStreamReader reader;
        try (InputStream stream = con.getInputStream()) {
            reader = new InputStreamReader(stream, "UTF-8");
            JsonObject api = new JsonParser().parse(reader).getAsJsonObject();
            reader.close();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            DB.queryUpdate("UPDATE settings SET lastupdate = ?", sdf.format(new Date()));
            api.get("nodes").getAsJsonArray().forEach((node) -> {
                JsonObject n = node.getAsJsonObject();
                int id = n.get("id").getAsInt();
                JsonObject pos = n.get("position").getAsJsonObject();
                Double latitude = null;
                Double longitude = null;
                try {
                    latitude = pos.get("lat").getAsDouble();
                    longitude = pos.get("long").getAsDouble();
                } catch (NumberFormatException ex) {
                }
                if (latitude == null || longitude == null) {
                    DB.queryUpdate("INSERT INTO nodes SET id = ?, latitude = NULL, longitude = NULL ON DUPLICATE KEY UPDATE latitude = NULL, longitude = NULL", id);
                } else {
                    DB.queryUpdate("INSERT INTO nodes SET id = ?, latitude = ?, longitude = ? ON DUPLICATE KEY UPDATE latitude = ?, longitude = ?", id, latitude, longitude, latitude, longitude);
                }
                POOL.submit(new NodeThread(id));
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
            tbody.children().select(".node_db_color0").forEach((tr) -> parseRegister(tr));
            tbody.children().select(".node_db_color2").forEach((tr) -> parseRegister(tr));
        }
    }

    private void parseRegister(Element tr) {
        try {
            int nodeId = Integer.parseInt(tr.child(1).child(0).text());
            boolean gateway = tr.child(3).child(1).attr("alt").equals("Ja");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String firstseen_html = tr.child(6).child(0).getElementsByTag("td").get(0).text();
            String lastseen_html = tr.child(6).child(0).getElementsByTag("td").get(1).text();
            long firstseen = firstseen_html.isEmpty() ? -1 : sdf.parse(firstseen_html).getTime() / 1000 - 60 * 60;
            long lastseen = lastseen_html.isEmpty() ? -1 : sdf.parse(lastseen_html).getTime() / 1000 - 60 * 60;
            DB.queryUpdate("INSERT INTO nodes SET id = ?, firstseen = ?, lastseen = ?, gateway = ? ON DUPLICATE KEY UPDATE firstseen = ?, lastseen = ?, gateway = ?", nodeId, firstseen, lastseen, gateway, firstseen, lastseen, gateway);
        } catch (ParseException ex) {
            Logger.getLogger(DataGen.class.getName()).log(Level.SEVERE, tr.child(1).child(0).text(), ex);
        }
    }

    public static MySQL getDB() {
        return DB;
    }

    public static void main(String[] args) {
        LOG.log(Level.INFO, "Getting Connection to DB...");
        DB = new MySQL();
        if (DB.hasConnection()) {
            DB.queryUpdate("CREATE TABLE IF NOT EXISTS `links` ( "
                    + "	`from` INT(11) NOT NULL, "
                    + "	`to` INT(11) NOT NULL, "
                    + "	`interface` VARCHAR(45) NOT NULL, "
                    + "	`tq` INT(3) NOT NULL, "
                    + "	PRIMARY KEY (`from`, `to`) "
                    + ") COLLATE='utf8_general_ci' ENGINE=InnoDB;");
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
            DB.queryUpdate("CREATE TABLE IF NOT EXISTS `settings` ( "
                    + "	`lastupdate` VARCHAR(20) NOT NULL, "
                    + "	PRIMARY KEY (`lastupdate`) "
                    + ") COLLATE='utf8_general_ci' ENGINE=InnoDB;");
            DB.queryUpdate("TRUNCATE links");
            DataGen dataGen = new DataGen();
            try {
                LOG.log(Level.INFO, "Getting Register...");
                dataGen.parseRegister();
                LOG.log(Level.INFO, "Getting Nodes...");
                dataGen.getNodes();
            } catch (IOException ex) {
                Logger.getLogger(DataGen.class.getName()).log(Level.SEVERE, null, ex);
            }
            POOL.shutdown();
            LOG.log(Level.INFO, "Wating Threads for finish...");
            try {
                POOL.awaitTermination(3, TimeUnit.MINUTES);
            } catch (InterruptedException ex) {
                Logger.getLogger(DataGen.class.getName()).log(Level.SEVERE, null, ex);
            }
            LOG.log(Level.INFO, "Done!");
        }
    }
}
