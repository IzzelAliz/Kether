package io.izzel.kether.common.persistent.loader;

import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.loader.SimpleQuestLoader;
import io.izzel.kether.common.loader.SimpleReader;
import io.izzel.kether.common.persistent.PersistentLoadError;
import io.izzel.tools.product.Product3;

import java.util.ArrayList;
import java.util.List;

public class RewriteLoader extends SimpleQuestLoader {

    @Override
    protected Parser newParser(char[] content, QuestService<?> service, List<String> namespace) {
        return new RewriteParser(content, service, namespace);
    }

    protected static class RewriteParser extends SimpleQuestLoader.Parser {

        private final List<Product3<Integer, Integer, String>> rewriteRules = new ArrayList<>();
        private int lastBegin = -1;

        public RewriteParser(char[] content, QuestService<?> service, List<String> namespace) {
            super(content, service, namespace);
        }

        @Override
        protected SimpleReader newReader(QuestService<?> service, List<String> namespace) {
            return new DiffReader(service, this, namespace);
        }

        protected void beginRewrite() {
            if (lastBegin == -1) {
                lastBegin = index;
            } else {
                throw PersistentLoadError.RECURSIVE_REWRITE.create();
            }
        }

        protected
    }
}
