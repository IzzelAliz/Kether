package io.izzel.kether.common.loader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.api.data.SimpleQuest;
import io.izzel.kether.common.util.LocalizedException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleQuestLoader implements QuestLoader {

    private static final Pattern BLOCK_LABEL = Pattern.compile("(?m)^\\s*(\\w+):\\s*$");

    @Override
    public <CTX extends QuestContext> Quest load(QuestService<CTX> service, Logger logger, String id, byte[] bytes) throws LocalizedException {
        String content = new String(bytes, StandardCharsets.UTF_8);
        List<Map.Entry<String, String>> blockContents = Lists.newArrayList();
        {
            Matcher matcher = BLOCK_LABEL.matcher(content);
            int end = 0;
            String labelName = QuestContext.BASE_BLOCK;
            while (matcher.find()) {
                blockContents.add(Maps.immutableEntry(labelName, content.substring(end, matcher.start())));
                labelName = matcher.group(1);
                end = matcher.end();
            }
            blockContents.add(Maps.immutableEntry(labelName, content.substring(end)));
        }
        Map<String, Quest.Block> map = Maps.newHashMap();
        boolean fail = false;
        for (Map.Entry<String, String> entry : blockContents) {
            ImmutableList.Builder<QuestAction<?>> builder = ImmutableList.builder();
            QuestReader reader = new SimpleReader(service, entry.getValue());
            try {
                while (reader.hasNext()) {
                    QuestAction<Object> action = reader.nextAction();
                    builder.add(action);
                    reader.mark();
                }
                map.put(entry.getKey(), new SimpleQuest.SimpleBlock(entry.getKey(), builder.build()));
            } catch (Exception e) {
                fail = true;
                logger.warning(service.getLocalizedText("load-error.block-exception",
                    entry.getKey(),
                    lineOf(entry.getValue(), reader.getMark()),
                    e instanceof LocalizedException
                        ? service.getLocalizedText(((LocalizedException) e).getNode(), ((LocalizedException) e).getParams())
                        : e.toString()
                ));
                logger.warning(entry.getValue().substring(reader.getMark(), reader.getIndex()).trim());
            }
        }
        if (fail) throw new RuntimeException();
        return new SimpleQuest(map, id);
    }

    private int lineOf(String str, int index) {
        return (int) (str.chars().limit(index).filter(i -> i == '\n').count() + 1);
    }
}
