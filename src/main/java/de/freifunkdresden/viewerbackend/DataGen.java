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
import de.freifunkdresden.viewerbackend.datasource.FreifunkApi;
import de.freifunkdresden.viewerbackend.exception.GatewaysCollectionException;
import de.freifunkdresden.viewerbackend.exception.JsonGenerationException;
import de.freifunkdresden.viewerbackend.exception.NodeInfoCollectionException;
import de.freifunkdresden.viewerbackend.exception.OfflineNodeProcessingException;
import de.freifunkdresden.viewerbackend.exception.RouteCollectionException;
import de.freifunkdresden.viewerbackend.json.JsonFileGen;
import de.freifunkdresden.viewerbackend.stats.GeneralStatType;
import de.freifunkdresden.viewerbackend.stats.StatsSQL;
import de.freifunkdresden.viewerbackend.thread.NodeSysInfoThread;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DataGen {

    private static final Logger LOGGER = LogManager.getLogger(DataGen.class);
    private static final DataHolder HOLDER = new DataHolder();
    private static final ExecutorService POOL = Executors.newFixedThreadPool(10);
    private static final Config CONFIG = new Config();
    private static final Cache CACHE = new Cache();
    private static boolean debug = false;
    private static MySQL mysqlDb;
    private static Influx influxDb;

    public static MySQL getDB() {
        return mysqlDb;
    }

    public static Influx getInflux() {
        return influxDb;
    }

    public static DataHolder getDataHolder() {
        return HOLDER;
    }

    public static Config getConfig() {
        return CONFIG;
    }

    public static Cache getCache() {
        return CACHE;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void main(@NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("--debug")) {
            debug = true;
            LOGGER.log(Level.INFO, "DEBUG mode on");
        }
        try {
            CONFIG.loadConfig();
            CACHE.initialize();
            setupDatabase();
            processFreifunkApi();
            collectLocalData();
            collectNodeInfo();
            fillOfflineNodes();
            startDbSave();
            collectLinks();
            genJson();
            saveStats();
            endDbSave();
            influxDb.closeConnection();
            mysqlDb.closeConnection();
            LOGGER.log(Level.INFO, "Done!");
        } catch (JsonGenerationException | NodeInfoCollectionException | OfflineNodeProcessingException |
                 RouteCollectionException | GatewaysCollectionException ex) {
            LOGGER.log(Level.ERROR, "Execution Exception: ", ex);
        }
        if (debug) {
            LOGGER.log(Level.DEBUG, "{} nodes, {} nodes online",
                    getDataHolder().getNodes().size(),
                    getDataHolder().getNodes().values().stream().filter(Node::isOnline).count());
        }
    }

    private static void processFreifunkApi() {
        LOGGER.log(Level.INFO, "Processing API...");
        FreifunkApi.downloadApiFile();
        FreifunkApi.processApi();
    }

    private static void collectLocalData() throws RouteCollectionException, GatewaysCollectionException {
        LOGGER.log(Level.INFO, "Collect local data...");
        LocalDataCollector.collectRoutes();
        LocalDataCollector.collectGateways();
    }

    private static void collectNodeInfo() throws NodeInfoCollectionException {
        try {
            ExecutorService pool = Executors.newFixedThreadPool(10);
            HOLDER.getNodes().values().stream().filter(n -> getDataHolder().isReachable(n))
                    .forEach(n -> pool.submit(new NodeSysInfoThread(n)));
            pool.shutdown();
            LOGGER.log(Level.INFO, "Waiting threads to finish...");
            if (!pool.awaitTermination(2, TimeUnit.MINUTES)) {
                LOGGER.log(Level.ERROR, "Node Collector hit execution limit!");
                List<Runnable> notExecuted = pool.shutdownNow();
                LOGGER.log(Level.ERROR, "{} tasks not processed", notExecuted.size());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new NodeInfoCollectionException(ex);
        }
    }

    private static void collectLinks() {
        LOGGER.log(Level.INFO, "Collect links...");
        HOLDER.getNodes().values().forEach(node -> node.getLinks().forEach(link -> {
            Link lnk = HOLDER.getLink(link.getSource(), link.getTarget(), link.getType());
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
                .map(n -> String.valueOf(n.getId()))
                .collect(Collectors.joining(","));
        if (ids.isEmpty()) {
            return;
        }
        try (ResultSet rs = mysqlDb.querySelect("SELECT * FROM nodes_current WHERE id IN (" + ids + ")")) {
            while (rs.next()) {
                DataParserDB dataParserDB = new DataParserDB();
                dataParserDB.parse(rs);
                HOLDER.getNode(rs.getInt("id")).setDpDatabase(dataParserDB);
            }
        } catch (SQLException ex) {
            throw new OfflineNodeProcessingException(ex);
        }
    }

    private static void genJson() throws JsonGenerationException {
        try {
            LOGGER.log(Level.INFO, "Generate JSON files...");
            Path dir = Paths.get(CONFIG.getValue("json_path"));
            if (Files.notExists(dir)) {
                Files.createDirectory(dir);
            }
            JsonFileGen jfg = new JsonFileGen(dir);
            jfg.genNodes();
            jfg.genGraph();
            jfg.genMeshViewer();
        } catch (IOException ex) {
            throw new JsonGenerationException(ex);
        }
    }

    private static void startDbSave() {
        if (debug) {
            return;
        }
        LOGGER.log(Level.INFO, "Start Save to database");
        HOLDER.getNodes().values().stream()
                .filter(Node::isOnline)
                .filter(Node::isDisplayed)
                .forEach(node -> POOL.submit(node::updateDatabase));
        POOL.shutdown();
    }

    private static void endDbSave() {
        if (debug) {
            return;
        }
        LOGGER.log(Level.INFO, "End Save to database...");
        try {
            if (!POOL.awaitTermination(3, TimeUnit.MINUTES)) {
                LOGGER.log(Level.ERROR, "Database save hit execution limit!");
                POOL.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.ERROR, "Execution Interrupted", ex);
        }
    }

    private static void saveStats() {
        if (debug) {
            return;
        }
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
        mysqlDb = new MySQL();
        mysqlDb.openConnection();
        mysqlDb.queryUpdate("CREATE TABLE IF NOT EXISTS `nodes` ( "
                + " `id` INT(11) NOT NULL, "
                + " `community` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8_general_ci', "
                + " `role` VARCHAR(10) NULL DEFAULT NULL COLLATE 'utf8_general_ci', "
                + " `model` TEXT NULL DEFAULT NULL COLLATE 'utf8_general_ci', "
                + " `firstseen` INT(11) NULL DEFAULT NULL, "
                + " `lastseen` INT(11) NULL DEFAULT NULL, "
                + " `latitude` DOUBLE NULL DEFAULT NULL, "
                + " `longitude` DOUBLE NULL DEFAULT NULL, "
                + " `name` TEXT NULL DEFAULT NULL COLLATE 'utf8_general_ci', "
                + " `email` TEXT NULL DEFAULT NULL COLLATE 'utf8_general_ci', "
                + " PRIMARY KEY (`id`) USING BTREE "
                + ") COLLATE='utf8_general_ci' ENGINE=InnoDB;");
        mysqlDb.queryUpdate("CREATE TABLE IF NOT EXISTS `airtime` ( " +
                " `id` INT(11) NOT NULL AUTO_INCREMENT, " +
                " `type` TINYINT(4) NOT NULL, " +
                " `active` BIGINT(20) UNSIGNED NOT NULL, " +
                " `busy` BIGINT(20) UNSIGNED NOT NULL, " +
                " `receive` BIGINT(20) UNSIGNED NOT NULL, " +
                " `transmit` BIGINT(20) UNSIGNED NOT NULL, " +
                " PRIMARY KEY (`id`, `type`) USING BTREE, " +
                " CONSTRAINT `FK_airtime_nodes` FOREIGN KEY (`id`) REFERENCES `nodes` (`id`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") COLLATE='utf8mb4_general_ci' ENGINE=InnoDB;");
        mysqlDb.queryUpdate("CREATE TABLE IF NOT EXISTS `nodes_firmware` ( " +
                " `id` INT(11) NOT NULL, " +
                " `version` VARCHAR(10) NULL DEFAULT NULL COLLATE 'utf8_general_ci', " +
                " `branch` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8_general_ci', " +
                " `git_rev` CHAR(40) NULL DEFAULT NULL COLLATE 'utf8_general_ci', " +
                " `base` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8_general_ci', " +
                " `auto_update` INT(1) NULL DEFAULT NULL, " +
                " PRIMARY KEY (`id`) USING BTREE, " +
                " CONSTRAINT `FK_nodesFirmware_nodes` FOREIGN KEY (`id`) REFERENCES `nodes` (`id`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") COLLATE='utf8_general_ci' ENGINE=InnoDB;");
        influxDb = new Influx();
        influxDb.openConnection();
    }
}
