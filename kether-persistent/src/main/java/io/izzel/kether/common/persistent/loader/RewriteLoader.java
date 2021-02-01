package io.izzel.kether.common.persistent.loader;

import io.izzel.kether.common.api.Quest;
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
import java.util.List;

public class RewriteLoader extends SimpleQuestLoader {

    @Override
    protected Parser newParser(char[] content, QuestService<?> service, List<String> namespace) {
        return new RewriteParser(content, service, namespace);
    }

    protected static class RewriteParser extends SimpleQuestLoader.Parser {

        final List<Product3<Integer, Integer, String>> rewriteRules = new ArrayList<>();
        private QuestTableStore.QuestTable questTable;

        public RewriteParser(char[] content, QuestService<?> service, List<String> namespace) {
            super(content, service, namespace);
        }

        @Override
        protected SimpleReader newReader(QuestService<?> service, List<String> namespace) {
            return new DiffReader(service, this, namespace, questTable);
        }

        @Override
        public Quest parse(String id) {
            questTable = ((PersistentQuestService<?>) service).getStorage().getTableStore().getTable(id);
            return super.parse(id);
        }

        void rewrite(Path path) throws IOException {
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
