package io.izzel.kether.common.persistent.loader;

import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.loader.SimpleQuestLoader;
import io.izzel.kether.common.loader.SimpleReader;
import io.izzel.kether.common.persistent.PersistentQuestService;
import io.izzel.kether.common.persistent.QuestTableStore;
import io.izzel.tools.product.Product3;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RewriteLoader extends SimpleQuestLoader {

    @Override
    protected RewriteParser newParser(char[] content, QuestService<?> service, List<String> namespace) {
        return new RewriteParser(content, service, namespace);
    }

    @Override
    public <C extends QuestContext> Quest load(QuestService<C> service, String id, Path path, List<String> namespace) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        RewriteParser parser = newParser(new String(bytes, StandardCharsets.UTF_8).toCharArray(), service, namespace);
        parser.rewrite(path);
        return parser.parse(id);
    }

    protected static class RewriteParser extends SimpleQuestLoader.Parser {

        final ArrayList<Product3<Integer, Integer, String>> rewriteRules = new ArrayList<>();
        private QuestTableStore.QuestTable questTable;

        public RewriteParser(char[] content, QuestService<?> service, List<String> namespace) {
            super(content, service, namespace);
        }

        @Override
        protected SimpleReader newReader(QuestService<?> service, List<String> namespace) {
            return new DiffReader(service, this, namespace, questTable.inBlock(this.currentBlock));
        }

        @Override
        protected String nextAnonymousBlockName() {
            return super.nextAnonymousBlockName();
        }

        @Override
        public Quest parse(String id) {
            questTable = ((PersistentQuestService<?>) service).getStorage().getTableStore().getTable(id);
            return super.parse(id);
        }

        void rewrite(Path path) throws IOException {
            this.rewriteRules.sort(Comparator.comparingInt(it -> it._1));
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                int index = 0;
                for (Product3<Integer, Integer, String> rule : this.rewriteRules) {
                    writer.write(this.arr, index, rule._2 - rule._1);
                    writer.write(rule._3);
                    index = rule._2;
                }
                writer.write(this.arr, index, this.arr.length - index);
            }
        }
    }
}
