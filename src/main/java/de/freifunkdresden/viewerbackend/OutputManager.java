/*
 * The MIT License
 *
 * Copyright 2026 Niklas Merkelt.
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

import de.freifunkdresden.viewerbackend.output.HopGlass;
import de.freifunkdresden.viewerbackend.output.MeshViewer;
import de.freifunkdresden.viewerbackend.output.OutputGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OutputManager {

    private final List<OutputGenerator> generators = new ArrayList<>();
    private final Path outputDirectory;

    public OutputManager() {
        generators.add(new HopGlass());
        generators.add(new MeshViewer());
        outputDirectory = Paths.get(DataGen.getConfig().getValue("json_path"));
    }

    public void generate() throws IOException {
        initialize();
        iterateNodes();
        iterateLinks();
        if (Files.notExists(outputDirectory)) {
            Files.createDirectory(outputDirectory);
        }
        saveOutput();
    }

    private void initialize() {
        for (OutputGenerator generator : generators) {
            generator.initialize();
        }
    }

    private void iterateNodes() {
        DataGen.getDataHolder().getNodes().values().stream()
                .filter(Node::isDisplayed)
                .forEach(node -> {
                    for (OutputGenerator generator : generators) {
                        generator.addNode(node);
                    }
                });
    }

    private void iterateLinks() {
        DataGen.getDataHolder().getLinks().stream()
                .filter(link -> link.getSource().isDisplayed() && link.getTarget().isDisplayed())
                .filter(link -> link.getSource().isOnline() && link.getTarget().isOnline())
                .forEach(link -> {
                    for (OutputGenerator generator : generators) {
                        generator.addLink(link);
                    }
                });
    }

    private void saveOutput() throws IOException {
        for (OutputGenerator generator : generators) {
            generator.save(outputDirectory);
        }
    }
}
