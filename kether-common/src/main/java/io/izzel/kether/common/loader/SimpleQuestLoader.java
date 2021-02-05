package io.izzel.kether.common.loader;

import com.google.common.collect.Maps;
import io.izzel.kether.common.api.ActionProperties;
import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.api.data.SimpleQuest;
import io.izzel.kether.common.util.LocalizedException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimpleQuestLoader implements QuestLoader {

    @Override
    public <C extends QuestContext> Quest load(QuestService<C> service, String id, Path path, List<String> namespace) throws IOException {
        return load(service, id, Files.readAllBytes(path), namespace);
    }

    @Override
    public <CTX extends QuestContext> Quest load(QuestService<CTX> service, String id, byte[] bytes, List<String> namespace) throws LocalizedException {
        String content = new String(bytes, StandardCharsets.UTF_8);
        return newParser(content.toCharArray(), service, namespace).parse(id);
    }

    protected Parser newParser(char[] content, QuestService<?> service, List<String> namespace) {
        return new Parser(content, service, namespace);
    }

    private static int lineOf(char[] chars, int index) {
        int line = 1;
        for (int i = 0; i < index; i++) {
            if (chars[i] == '\n') line++;
        }
        return line;
    }

    public static class Parser extends AbstractStringReader {

        protected final Map<String, Quest.Block> blocks = Maps.newHashMap();
        protected final QuestService<?> service;
        protected final List<String> namespace;
        protected String currentBlock;

        public Parser(char[] content, QuestService<?> service, List<String> namespace) {
            super(content);
            this.service = service;
            this.namespace = namespace;
        }

        protected ParsedAction<?> readAnonymousAction() {
            String lastBlock = this.currentBlock;
            String name = nextAnonymousBlockName();
            this.currentBlock = name;
            List<ParsedAction<?>> actions = readActions();
            this.currentBlock = lastBlock;
            if (!actions.isEmpty()) {
                ParsedAction<?> head = actions.get(0);
                Quest.Block block = new SimpleQuest.SimpleBlock(name, actions);
                this.blocks.put(block.getLabel(), block);
                head.set(ActionProperties.BLOCK, block.getLabel());
                return head;
            } else {
                return ParsedAction.noop();
            }
        }

        protected String nextAnonymousBlockName() {
            return this.currentBlock + "_anon_" + System.nanoTime();
        }

        protected SimpleReader newReader(QuestService<?> service, List<String> namespace) {
            return new SimpleReader(service, this, namespace);
        }

        public List<ParsedAction<?>> readActions() {
            skipBlank();
            boolean batch = peek() == '{';
            if (batch) skip(1);
            SimpleReader reader = newReader(service, namespace);
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
                throw LoadError.BLOCK_ERROR.create(this.currentBlock,
                    lineOf(this.arr, reader.getMark()),
                    new String(this.arr, reader.getMark(), Math.min(this.arr.length, reader.getIndex()) - reader.getMark()).trim())
                    .then(e instanceof LocalizedException ? (LocalizedException) e : LoadError.UNHANDLED.create(e));
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
