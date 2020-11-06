package io.izzel.kether.common.loader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestResolver;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.api.SimpleQuest;
import io.izzel.kether.common.util.LocalizedException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestLoader {

    private static final Pattern BLOCK_LABEL = Pattern.compile("(?m)^\\s*(\\w+):\\s*$");

    private final Path path;

    private QuestLoader(Path path) {
        this.path = path;
    }

    public <CTX extends QuestContext> Quest load(QuestService<CTX> service, Logger logger, String id) throws LocalizedException, IOException {
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        List<Map.Entry<Integer, UUID>> dataInsertions = Lists.newArrayList();
        List<Integer> blockIndexes = Lists.newArrayList();
        List<Map.Entry<String, String>> blockContents = Lists.newArrayList();
        {
            Matcher matcher = BLOCK_LABEL.matcher(content);
            int end = 0;
            String labelName = QuestContext.BASE_BLOCK;
            while (matcher.find()) {
                blockIndexes.add(end);
                blockContents.add(Maps.immutableEntry(labelName, content.substring(end, matcher.start())));
                labelName = matcher.group(1);
                end = matcher.end();
            }
            blockIndexes.add(end);
            blockContents.add(Maps.immutableEntry(labelName, content.substring(end)));
        }
        Map<String, Quest.Block> map = Maps.newHashMap();
        boolean fail = false;
        ListIterator<Map.Entry<String, String>> iterator = blockContents.listIterator();
        while (iterator.hasNext()) {
            int i = iterator.nextIndex();
            Set<String> data = new HashSet<>();
            Map.Entry<String, String> entry = iterator.next();
            ImmutableList.Builder<QuestAction<?>> builder = ImmutableList.builder();
            QuestResolver<CTX> resolver = QuestResolver.of(service, entry.getValue());
            try {
                while (resolver.hasNext()) {
                    int index = resolver.getIndex();
                    QuestAction<Object> action = resolver.nextAction();
                    if (action instanceof DataAction) {
                        data.clear();
                    } else if (action.isPersist()) {
                        if (data.contains(action.getDataPrefix())) {
                            data.clear();
                            int actualIndex = blockIndexes.get(i) + index;
                            UUID uuid = UUID.randomUUID();
                            dataInsertions.add(Maps.immutableEntry(actualIndex, uuid));
                            builder.add(new DataAction<>(uuid.toString()));
                        }
                        data.add(action.getDataPrefix());
                    }
                    builder.add(action);
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
        if (!dataInsertions.isEmpty()) {
            insertData(content, dataInsertions);
        }
        return new SimpleQuest(map, id);
    }

    private void insertData(String content, List<Map.Entry<Integer, UUID>> dataInsertions) throws IOException {
        try (
            OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.WRITE);
            PrintStream out = new PrintStream(outputStream, true, "utf-8")
        ) {
            int last = 0;
            for (Map.Entry<Integer, UUID> entry : dataInsertions) {
                out.print(content.substring(last, entry.getKey()));
                out.printf("# %s%s", entry.getValue(), System.lineSeparator());
                last = entry.getKey();
            }
            out.print(content.substring(last));
            out.flush();
        }
    }

    private int lineOf(String str, int index) {
        return (int) (str.chars().limit(index).filter(i -> i == '\n').count() + 1);
    }

    public static QuestLoader of(Path path) {
        return new QuestLoader(path);
    }

    public static Map<String, Quest> loadFolder(
        Path folder, QuestService<?> service, Logger logger
    ) throws IOException {
        Map<String, Quest> questMap = new HashMap<>();
        if (Files.notExists(folder)) Files.createDirectories(folder);
        Iterator<Path> iterator = Files.walk(folder).iterator();
        while (iterator.hasNext()) {
            Path path = iterator.next();
            if (!Files.isDirectory(path)) {
                try {
                    String name = folder.relativize(path).toString()
                        .replace(File.separatorChar, '.');
                    Quest load = QuestLoader.of(path).load(service, logger, name);
                    questMap.put(name, load);
                } catch (Exception e) {
                    logger.severe(service.getLocalizedText("load-error.fail", path));
                }
            }
        }
        return questMap;
    }
}
