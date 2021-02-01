package io.izzel.kether.common.persistent.loader;

import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.loader.SimpleReader;

import java.util.List;

public class DiffReader extends SimpleReader {

    public DiffReader(QuestService<?> service, RewriteLoader.RewriteParser parser, List<String> namespace) {
        super(service, parser, namespace);
    }

    protected RewriteLoader.RewriteParser parser() {
        return (RewriteLoader.RewriteParser) parser;
    }

    @Override
    public <T> ParsedAction<T> nextAction() {
        return super.nextAction();
    }
}
