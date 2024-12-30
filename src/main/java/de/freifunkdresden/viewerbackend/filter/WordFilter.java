/*
 * The MIT License
 *
 * Copyright 2023 Niklas Merkelt.
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

package de.freifunkdresden.viewerbackend.filter;

import de.freifunkdresden.viewerbackend.DataGen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class WordFilter {

    private static final Logger LOGGER = LogManager.getLogger(WordFilter.class);
    private final Set<String> wordList = new HashSet<>();
    private boolean initialized = false;

    public void setup() {
        String filterPath = DataGen.getConfig().getValue("filter_path");
        Path filterDirPath = Paths.get(filterPath);
        try {
            Files.walkFileTree(filterDirPath, new Visitor(filePath -> {
                if (!Files.isReadable(filePath)) {
                    LOGGER.warn("WordFilter: {} is not readable", filePath);
                    return FileVisitResult.CONTINUE;
                }
                try {
                    Files.readAllLines(filePath).stream()
                            .filter(word -> !word.isBlank())
                            .map(String::toLowerCase)
                            .forEach(wordList::add);
                } catch (RuntimeException | IOException ex) {
                    LOGGER.error(String.format("WordFilter: Exception while reading `%s`", filePath), ex);
                }
                return FileVisitResult.CONTINUE;
            }));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        initialized = true;
    }

    public boolean check(@Nullable String string) {
        if (!initialized) {
            throw new RuntimeException("WordFiler not initialized");
        }
        if (string == null) {
            return false;
        }
        for (String word : wordList) {
            if (string.toLowerCase().contains(word)) {
                if (DataGen.isDebug()) {
                    LOGGER.debug("WordFilter: action filter {} of `{}`", word, string);
                }
                return true;
            }
        }
        return false;
    }

    public static class Visitor implements FileVisitor<Path> {

        private final Function<Path, FileVisitResult> visitFile;

        public Visitor(Function<Path, FileVisitResult> visitFile) {
            this.visitFile = visitFile;
        }

        @NotNull
        @Override
        public FileVisitResult preVisitDirectory(Path dir, @NotNull BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @NotNull
        @Override
        public FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) {
            return visitFile.apply(file);
        }

        @NotNull
        @Override
        public FileVisitResult visitFileFailed(Path file, @NotNull IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        @NotNull
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }
}
