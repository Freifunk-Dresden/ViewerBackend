/*
 * The MIT License
 *
 * Copyright 2022 Niklas Merkelt.
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

import de.freifunkdresden.viewerbackend.exception.GatewaysCollectionException;
import de.freifunkdresden.viewerbackend.exception.RouteCollectionException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LocalDataCollector {

    private static final Logger LOGGER = LogManager.getLogger(LocalDataCollector.class);

    private LocalDataCollector() {
    }

    public static void collectRoutes() throws RouteCollectionException {
        try {
            Process process = Runtime.getRuntime().exec("ip r list table bat_route");
            process.waitFor(200, TimeUnit.MILLISECONDS);
            InputStream inputStream = process.getInputStream();
            String routes = new String(inputStream.readAllBytes());
            inputStream.close();
            List<String> routeArray = Arrays.asList(routes.split("\\n"));
            List<String> collect = routeArray.stream().map(s -> s.split("\\s")[0])
                    .filter(s -> s.startsWith("10.200.") && !s.endsWith("/16"))
                    .collect(Collectors.toList());
            String[] s = routeArray.get(0).split("\\s");
            collect.add(s[s.length - 1]);
            if (DataGen.isDebug()) {
                LOGGER.log(Level.DEBUG, "Collected routes: {}", collect);
            }
            DataGen.getDataHolder().addRoutes(collect);
        } catch (IOException e) {
            throw new RouteCollectionException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RouteCollectionException(e);
        }
    }

    public static void collectGateways() throws GatewaysCollectionException {
        try {
            Process process = Runtime.getRuntime().exec("sudo /usr/sbin/bmxd -c --gateways");
            process.waitFor(200, TimeUnit.MILLISECONDS);
            InputStream inputStream = process.getInputStream();
            String gateways = new String(inputStream.readAllBytes());
            inputStream.close();
            List<String> collect = Arrays.stream(gateways.split("\\n"))
                    .skip(1)
                    .map(s -> s.split("\\s+")[1])
                    .collect(Collectors.toList());
            if (DataGen.isDebug()) {
                LOGGER.log(Level.DEBUG, "Collected gateways: {}", collect);
            }
            DataGen.getDataHolder().addGateways(collect);
        } catch (IOException e) {
            throw new GatewaysCollectionException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GatewaysCollectionException(e);
        }
    }
}
