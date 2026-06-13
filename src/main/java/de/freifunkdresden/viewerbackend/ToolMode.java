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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Scanner;

public class ToolMode {

    private boolean running = false;
    private String context = "/";

    public void start() {
        running = true;
        while (running) {
            String command = readCommand();
            processCommand(command);
        }
    }

    private String readCommand() {
        System.out.printf("%s > ", getContextString());
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    @Contract(pure = true)
    private void processCommand(@NotNull String command) {
        String[] commandSplit = command.split(" ");
        switch (commandSplit[0]) {
            case "cd":
                context = commandSplit[1];
                if (!context.endsWith("/")) {
                    context = context + "/";
                }
                break;
            case "ls":
                if (context.equals("/")) {
                    System.out.println("node/");
                    System.out.println();
                    System.out.println("Count: 1");
                }
                if (context.equals("/node/")) {
                    DataGen.getDataHolder().getNodes().values().forEach(node -> System.out.println(node.getId() + "/"));
                    System.out.println();
                    System.out.println("Count: " + DataGen.getDataHolder().getNodes().size());
                }
                break;
            case "search":
                if (commandSplit[1].equals("link")) {
                    int node = Integer.parseInt(commandSplit[2]);
                    DataGen.getDataHolder().getNodes().values().forEach(n -> n.getLinks().forEach(link -> {
                        if (link.getSource().getId() == node || link.getTarget().getId() == node) {
                            System.out.println("[Node: " + n.getId() + "] Link: S: " + link.getSource().getId() + " T: " + link.getTarget().getId());
                        }
                    }));
                }
                break;
            case "exit":
                running = false;
                break;
        }
    }

    @NotNull
    @Contract(pure = true)
    private String getContextString() {
        return context;
    }
}
