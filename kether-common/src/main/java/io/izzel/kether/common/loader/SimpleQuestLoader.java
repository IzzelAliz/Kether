package io.izzel.kether.common.loader;

import com.google.common.collect.Maps;
import io.izzel.kether.common.api.ActionProperties;
import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.api.data.SimpleQuest;
import io.izzel.kether.common.util.LocalizedException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SimpleQuestLoader implements QuestLoader {

    @Override
    public <CTX extends QuestContext> Quest load(QuestService<CTX> service, Logger logger, String id, byte[] bytes) throws LocalizedException {
        String content = new String(bytes, StandardCharsets.UTF_8);
        try {
            return new Parser(content.toCharArray(), service, logger).parse(id);
        } catch (Exception e) {
            throw LocalizedException.of("load-error.fail", id);
        }
    }

    private static int lineOf(char[] chars, int index) {
        int line = 1;
        for (int i = 0; i < index; i++) {
            if (chars[i] == '\n') line++;
        }
        return line;
    }

    protected static class Parser extends AbstractStringReader {

        private final Map<String, Quest.Block> blocks = Maps.newHashMap();
        private final QuestService<?> service;
        private final Logger logger;
        private String currentBlock;

        public Parser(char[] content, QuestService<?> service, Logger logger) {
            super(content);
            this.service = service;
            this.logger = logger;
        }

        protected ParsedAction<?> readAnonymousAction() {
            List<ParsedAction<?>> actions = readActions();
            if (!actions.isEmpty()) {
                ParsedAction<?> head = actions.get(0);
                Quest.Block block = new SimpleQuest.SimpleBlock("anon_" + System.nanoTime(), actions);
                this.blocks.put(block.getLabel(), block);
                head.set(ActionProperties.BLOCK, block.getLabel());
                return head;
            } else {
                return ParsedAction.noop();
            }
        }

        public List<ParsedAction<?>> readActions() {
            skipBlank();
            boolean batch = peek() == '{';
            if (batch) skip(1);
            SimpleReader reader = new SimpleReader(service, this);
            try {
                ArrayList<ParsedAction<?>> list = new ArrayList<>();
                while ((batch && reader.hasNext()) || list.isEmpty()) {
                    if (batch && reader.peek() == '}') {
                        reader.skip(1);
                        this.index = reader.index;
                        list.trimToSize();
                        return list;
                    }
                    list.add(reader.nextAction());
                    reader.mark();
                }
                return list;
            } catch (Exception e) {
                logger.warning(service.getLocalizedText("load-error.block-exception",
                    this.currentBlock,
                    lineOf(this.arr, reader.getMark()),
                    e instanceof LocalizedException
                        ? service.getLocalizedText(((LocalizedException) e).getNode(), ((LocalizedException) e).getParams())
                        : e.toString()
                ));
                logger.warning(new String(this.arr, reader.getMark(), reader.getIndex()).trim());
                throw e;
            }
        }

        public void readBlock() {
            expect("def");
            String name = nextToken();
            expect("=");
            this.currentBlock = name;
            List<ParsedAction<?>> actions = readActions();
            SimpleQuest.SimpleBlock block = new SimpleQuest.SimpleBlock(name, actions);
            this.processActions(block, actions);
            this.blocks.put(name, block);
        }

        protected void processActions(Quest.Block block, List<ParsedAction<?>> actions) {
            if (!actions.isEmpty()) {
                actions.get(0).set(ActionProperties.BLOCK, block.getLabel());
            }
        }

        public Quest parse(String id) {
            while (hasNext()) {
                readBlock();
            }
            return new SimpleQuest(blocks, id);
        }
    }
}
