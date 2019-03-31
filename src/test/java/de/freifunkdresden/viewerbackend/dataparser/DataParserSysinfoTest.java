/*
 * The MIT License
 *
 * Copyright 2019 Niklas Merkelt
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
package de.freifunkdresden.viewerbackend.dataparser;

import com.google.gson.JsonObject;
import de.freifunkdresden.viewerbackend.NodeType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;

public class DataParserSysinfoTest {
    
    public DataParserSysinfoTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getLastseen method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetLastseen() throws Exception {
        JsonObject json = new JsonObject();
        json.add("statistics", new JsonObject());
        DataParserSysinfo instance = new DataParserSysinfo(json, 0);
        assertEquals(System.currentTimeMillis(), instance.getLastseen().longValue());
    }

    /**
     * Test of getCommunity method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetCommunity() throws Exception {
        //OL
        JsonObject json = new JsonObject();
        JsonObject common = new JsonObject();
        common.addProperty("city", "OL");
        json.add("common", common);
        json.add("statistics", new JsonObject());
        DataParserSysinfo instance = new DataParserSysinfo(json, 0);
        assertEquals("OL", instance.getCommunity());
        //Empty
        json = new JsonObject();
        common = new JsonObject();
        common.addProperty("city", "");
        json.add("common", common);
        json.add("statistics", new JsonObject());
        instance = new DataParserSysinfo(json, 0);
        assertEquals("Dresden", instance.getCommunity());
        //Meissen
        json = new JsonObject();
        common = new JsonObject();
        common.addProperty("city", "Meissen");
        json.add("common", common);
        json.add("statistics", new JsonObject());
        instance = new DataParserSysinfo(json, 0);
        assertEquals("Meißen", instance.getCommunity());
    }

    /**
     * Test of getRole method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetRole() throws Exception {
        JsonObject json = new JsonObject();
        JsonObject system = new JsonObject();
        system.addProperty("node_type", "mobile");
        json.add("system", system);
        json.add("statistics", new JsonObject());
        DataParserSysinfo instance = new DataParserSysinfo(json, 13);
        assertEquals(NodeType.MOBILE, instance.getRole());
        instance = new DataParserSysinfo(json, 0);
        assertEquals(NodeType.STANDARD, instance.getRole());
    }

    /**
     * Test of getModel method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetModel() throws Exception {
        JsonObject json = new JsonObject();
        JsonObject system = new JsonObject();
        system.addProperty("model", "Modellbezeichnung");
        json.add("system", system);
        json.add("statistics", new JsonObject());
        DataParserSysinfo instance = new DataParserSysinfo(json, 13);
        assertEquals("Modellbezeichnung", instance.getModel());
        json = new JsonObject();
        system = new JsonObject();
        system.addProperty("model2", "Modellbezeichnung");
        json.add("system", system);
        json.add("statistics", new JsonObject());
        instance = new DataParserSysinfo(json, 14);
        assertEquals("Modellbezeichnung", instance.getModel());
    }

    /**
     * Test of getFirmwareVersion method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetFirmwareVersion() throws Exception {
        JsonObject json = new JsonObject();
        JsonObject firmware = new JsonObject();
        firmware.addProperty("version", "Versionsbezeichnung");
        json.add("firmware", firmware);
        json.add("statistics", new JsonObject());
        DataParserSysinfo instance = new DataParserSysinfo(json, 0);
        assertEquals("Versionsbezeichnung", instance.getFirmwareVersion());
    }

    /**
     * Test of getFirmwareBase method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetFirmwareBase() throws Exception {
        JsonObject json = new JsonObject();
        JsonObject firmware = new JsonObject();
        firmware.addProperty("DISTRIB_ID", "DISTRIB_ID");
        firmware.addProperty("DISTRIB_RELEASE", "DISTRIB_RELEASE");
        firmware.addProperty("DISTRIB_REVISION", "DISTRIB_REVISION");
        json.add("firmware", firmware);
        json.add("statistics", new JsonObject());
        DataParserSysinfo instance = new DataParserSysinfo(json, 0);
        assertEquals("DISTRIB_ID DISTRIB_RELEASE DISTRIB_REVISION", instance.getFirmwareBase());
    }

    /**
     * Test of getGatewayIp method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetGatewayIp() throws Exception {
        JsonObject json = new JsonObject();
        JsonObject bmxd = new JsonObject();
        JsonObject gateways = new JsonObject();
        gateways.addProperty("selected", "10.200.0.1");
        bmxd.add("gateways", gateways);
        json.add("bmxd", bmxd);
        json.add("statistics", new JsonObject());
        DataParserSysinfo instance = new DataParserSysinfo(json, 0);
        assertEquals("10.200.0.1", instance.getGatewayIp());
    }

    /**
     * Test of getUptime method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetUptime() throws Exception {
        JsonObject json = new JsonObject();
        JsonObject system = new JsonObject();
        system.addProperty("uptime", " 13:05:14 up 63 days, 14:46,  0 users,  load average: 0.07, 0.07, 0.05");
        json.add("system", system);
        json.add("statistics", new JsonObject());
        DataParserSysinfo instance = new DataParserSysinfo(json, 9);
        assertEquals(5496360f, instance.getUptime().floatValue());
    }

    /**
     * Test of getMemoryUsage method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetMemoryUsage() throws Exception {
//        JsonObject json = new JsonObject();
//        JsonObject system = new JsonObject();
//        system.addProperty("uptime", " 13:05:14 up 63 days, 14:46,  0 users,  load average: 0.07, 0.07, 0.05");
//        json.add("system", system);
//        json.add("statistics", new JsonObject());
//        DataParserSysinfo instance = new DataParserSysinfo(json, 9);
//        assertEquals(91514f, instance.getMemoryUsage().doubleValue());
    }

    /**
     * Test of getLoadAvg method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetLoadAvg() throws Exception {
//        DataParserSysinfo instance = null;
//        Float expResult = null;
//        Float result = instance.getLoadAvg();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getClients method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetClients() throws Exception {
//        DataParserSysinfo instance = null;
//        Short expResult = null;
//        Short result = instance.getClients();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getLinkSet method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetLinkSet() throws Exception {
//        DataParserSysinfo instance = null;
//        HashSet<Link> expResult = null;
//        HashSet<Link> result = instance.getLinkSet();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetName() throws Exception {
//        DataParserSysinfo instance = null;
//        String expResult = "";
//        String result = instance.getName();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getEMail method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetEMail() throws Exception {
//        DataParserSysinfo instance = null;
//        String expResult = "";
//        String result = instance.getEMail();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getAutoUpdate method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetAutoUpdate() throws Exception {
//        DataParserSysinfo instance = null;
//        Boolean expResult = null;
//        Boolean result = instance.getAutoUpdate();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getLocation method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testGetLocation() throws Exception {
//        DataParserSysinfo instance = null;
//        Location expResult = null;
//        Location result = instance.getLocation();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of isOnline method, of class DataParserSysinfo.
     */
    @org.junit.jupiter.api.Test
    public void testIsOnline() throws Exception {
//        DataParserSysinfo instance = null;
//        Boolean expResult = null;
//        Boolean result = instance.isOnline();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
    
}
