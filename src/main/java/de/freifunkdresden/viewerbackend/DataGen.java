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

import de.freifunkdresden.viewerbackend.dataparser.DataParserDB;
import de.freifunkdresden.viewerbackend.exception.APIProcessingException;
import de.freifunkdresden.viewerbackend.exception.JsonGenerationException;
import de.freifunkdresden.viewerbackend.exception.NodeInfoCollectionException;
import de.freifunkdresden.viewerbackend.exception.OfflineNodeProcessingException;
import de.freifunkdresden.viewerbackend.json.JsonFileGen;
import de.freifunkdresden.viewerbackend.stats.GeneralStatType;
import de.freifunkdresden.viewerbackend.stats.StatsSQL;
import de.freifunkdresden.viewerbackend.thread.NodeDatabaseThread;
import de.freifunkdresden.viewerbackend.thread.NodeSysinfoThread;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataGen {

    private static final Logger LOGGER = LogManager.getLogger(DataGen.class);
    private static final DataHolder HOLDER = new DataHolder();
    private static final ExecutorService POOL = Executors.newFixedThreadPool(10);
    private static MySQL DB;
    private static Influx INFLUX;

    public static MySQL getDB() {
        return DB;
    }

    public static Influx getInflux() {
        return INFLUX;
    }

    public static void main(String[] args) {
        try {
            setupDatabase();
            collectAPIData();
            collectNodeInfo();
            fillOfflineNodes();
            startDbSave();
            collectLinks();
            genJson();
            saveStats();
            endDbSave();
            INFLUX.closeConnection();
            DB.closeConnection();
            LOGGER.log(Level.INFO, "Done!");
        } catch (APIProcessingException | JsonGenerationException | NodeInfoCollectionException | OfflineNodeProcessingException ex) {
            LOGGER.log(Level.ERROR, "", ex);
        }
    }

    private static void collectAPIData() throws APIProcessingException {
        try {
            LOGGER.log(Level.INFO, "Processing API...");
            HOLDER.processAPI();
        } catch (Exception ex) {
            throw new APIProcessingException(ex);
        }
    }

    private static void collectNodeInfo() throws NodeInfoCollectionException {
        try {
            ExecutorService pool = Executors.newFixedThreadPool(10);
            HOLDER.getNodes().values().forEach((n) -> pool.submit(new NodeSysinfoThread(n)));
            pool.shutdown();
            LOGGER.log(Level.INFO, "Waiting threads to finish...");
            pool.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            throw new NodeInfoCollectionException(ex);
        }
    }

    private static void collectLinks() {
        LOGGER.log(Level.INFO, "Collect links...");
        HOLDER.getNodes().values().forEach((node) -> node.getLinks().forEach((link) -> {
            Link lnk = HOLDER.getLink(link.getSource().getId(), link.getTarget().getId());
            if (lnk == null) {
                HOLDER.addLink(link);
            } else {
                lnk.setTargetTq(link.getSourceTq());
            }
        }));
    }

    private static void fillOfflineNodes() throws OfflineNodeProcessingException {
        LOGGER.log(Level.INFO, "Fill offline nodes from database...");
        String ids = HOLDER.getNodes().values().stream()
                .filter((n) -> !n.isOnline())
                .map((n) -> String.valueOf(n.getId()))
                .collect(Collectors.joining(","));
        if (ids.isEmpty()) {
            return;
        }
        try (ResultSet rs = DB.querySelect("SELECT * FROM nodes WHERE id IN (" + ids + ")")) {
            while (rs.next()) {
                HOLDER.getNode(rs.getInt("id")).fill(new DataParserDB(rs));
            }
        } catch (Throwable ex) {
            throw new OfflineNodeProcessingException(ex);
        }
    }

    private static void genJson() throws JsonGenerationException {
        try {
            LOGGER.log(Level.INFO, "Generate JSON files...");
            JsonFileGen jfg = new JsonFileGen(HOLDER.getNodes().values(), HOLDER.getLinks().values());
            jfg.genNodes();
            jfg.genGraph();
            jfg.genMeshViewer();
        } catch (IOException ex) {
            throw new JsonGenerationException(ex);
        }
    }

    private static void startDbSave() {
        LOGGER.log(Level.INFO, "Start Save to database");
        HOLDER.getNodes().values().stream()
                .filter(Node::isOnline)
                .filter(Node::isDisplayed)
                .forEach((node) -> POOL.submit(new NodeDatabaseThread(node)));
        POOL.shutdown();
    }

    private static void endDbSave() {
        LOGGER.log(Level.INFO, "End Save to database...");
        try {
            if (!POOL.awaitTermination(3, TimeUnit.MINUTES)) {
                LOGGER.log(Level.ERROR, "3 min limit!");
                POOL.shutdownNow();
            }
        } catch (InterruptedException ex) {
            LOGGER.log(Level.ERROR, "", ex);
        }
    }

    private static void saveStats() {
        LOGGER.log(Level.INFO, "Save stats to database...");
        HOLDER.getNodes().values().stream().filter(Node::isDisplayed)
                .forEach(Node::collectStats);
        StatsSQL.addGeneralStats(GeneralStatType.NODES, HOLDER.getNodes().values().stream().filter(Node::isDisplayed).count());
        StatsSQL.addGeneralStats(GeneralStatType.NODES_ONLINE, HOLDER.getNodes().values().stream().filter(Node::isOnline).count());
        StatsSQL.addGeneralStats(GeneralStatType.CLIENTS, HOLDER.getNodes().values().stream()
                .filter(Node::isOnline)
                .reduce(BigInteger.ZERO, (result, node) -> result.add(BigInteger.valueOf(node.getClients())), BigInteger::add)
                .intValue());
        StatsSQL.processStats();
    }

    private static void setupDatabase() {
        LOGGER.log(Level.INFO, "Getting connection to DB...");
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
        } else {
            throw new RuntimeException("No Database Connection!");
        }
        INFLUX = new Influx();
    }

    public static DataHolder getDataHolder() {
        return HOLDER;
    }
}
