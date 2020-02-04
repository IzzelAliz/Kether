package io.izzel.kether.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.izzel.kether.common.api.*;
import io.izzel.kether.common.util.LocalizedException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QuestLoader {

    private static final Pattern BLOCK_LABEL = Pattern.compile("(?m)^\\s*(\\w+):\\s*$");

    private final Path path;

    private QuestLoader(Path path) {
        this.path = path;
    }

    public <CTX extends QuestContext> Quest load(QuestService<CTX> service, Logger logger, String id) throws LocalizedException, IOException {
        String content = Files.lines(path, StandardCharsets.UTF_8)
            .map(it -> it.replaceFirst("//.*$", ""))
            .collect(Collectors.joining("\n"));
        Map<String, String> blockContents = Maps.newHashMap();
        {
            Matcher matcher = BLOCK_LABEL.matcher(content);
            int end = 0;
            String labelName = "main";
            while (matcher.find()) {
                blockContents.put(labelName, content.substring(end, matcher.start()).trim());
                labelName = matcher.group(1);
                end = matcher.end();
            }
            blockContents.put(labelName, content.substring(end).trim());
        }
        Map<String, Quest.Block> map = Maps.newHashMap();
        boolean fail = false;
        for (Map.Entry<String, String> entry : blockContents.entrySet()) {
            ImmutableList.Builder<QuestAction<?, CTX>> builder = ImmutableList.builder();
            QuestResolver<CTX> resolver = QuestResolver.of(service, entry.getValue());
            try {
                while (resolver.hasNext()) {
                    builder.add(resolver.nextAction());
                    resolver.mark();
                }
                map.put(entry.getKey(), new SimpleQuest.SimpleBlock(entry.getKey(), builder.build()));
            } catch (Exception e) {
                fail = true;
                logger.warning(service.getLocalizedText("load-error.block-exception",
                    entry.getKey(),
                    lineOf(entry.getValue(), resolver.getMark()),
                    e instanceof LocalizedException
                        ? service.getLocalizedText(((LocalizedException) e).getNode(), ((LocalizedException) e).getParams())
                        : e.toString()
                ));
                logger.warning(entry.getValue().substring(resolver.getMark(), resolver.getIndex()).trim());
            }
        }
        if (fail) throw new RuntimeException();
        return new SimpleQuest(map, id);
    }

    private int lineOf(String str, int index) {
        return (int) (str.chars().limit(index).filter(i -> i == '\n').count() + 1);
    }

    public static QuestLoader of(Path path) {
        return new QuestLoader(path);
    }

    public static <CTX extends QuestContext> Map<String, Quest> loadFolder(
        Path folder, QuestService<CTX> service, Logger logger
    ) throws IOException {
        Map<String, Quest> questMap = new HashMap<>();
        if (Files.notExists(folder)) Files.createDirectories(folder);
        Files.walk(folder)
            .filter(it -> !Files.isDirectory(it))
            .forEach(path -> {
                try {
                    String name = folder.relativize(path).toString()
                        .replace(File.separatorChar, '.');
                    Quest load = QuestLoader.of(path).load(service, logger, name);
                    questMap.put(name, load);
                } catch (Exception e) {
                    logger.severe(service.getLocalizedText("load-error.fail", path));
                }
            });
        return questMap;
    }
}
