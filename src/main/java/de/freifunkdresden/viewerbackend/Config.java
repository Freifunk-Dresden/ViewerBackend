/*
 * The MIT License
 *
 * Copyright 2020 Niklas Merkelt.
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

import de.freifunkdresden.viewerbackend.exception.ConfigurationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    private final Map<String, String> configValues = new HashMap<>();

    public void loadConfig() {
        Path path = Paths.get("config.ini");
        if (!Files.exists(path) || !Files.isReadable(path)) {
            throw new ConfigurationException("Config files don't exist");
        }
        try {
            List<String> lines = Files.readAllLines(path);
            lines.stream().filter(line -> !(line.startsWith("#")))
                    .map(line -> line.split("=", 2))
                    .forEachOrdered(split -> {
                        configValues.put(split[0], split[1]);
                    });
        } catch (IOException ex) {
            throw new ConfigurationException("Config file couldn't be loaded", ex);
        }
    }

    public String getValue(String key) {
        return configValues.get(key);
    }
}
