package io.izzel.kether.common.persistent.loader;

import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.loader.SimpleQuestLoader;
import io.izzel.kether.common.loader.SimpleReader;

import java.util.List;
import java.util.logging.Logger;

public class RewriteLoader extends SimpleQuestLoader {

    @Override
    protected Parser newParser(char[] content, QuestService<?> service, Logger logger, List<String> namespace) {
        return new RewriteParser(content, service, logger, namespace);
    }

    protected static class RewriteParser extends SimpleQuestLoader.Parser {

        private int lastBegin = -1;

        public RewriteParser(char[] content, QuestService<?> service, Logger logger, List<String> namespace) {
            super(content, service, namespace);
        }

        @Override
        protected SimpleReader newReader(QuestService<?> service, List<String> namespace) {
            return new DiffReader(service, this, namespace);
        }

        protected void beginRewrite() {

        }
    }
}
