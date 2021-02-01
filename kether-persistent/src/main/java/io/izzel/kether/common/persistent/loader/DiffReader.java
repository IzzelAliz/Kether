package io.izzel.kether.common.persistent.loader;

import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.loader.SimpleReader;
import io.izzel.kether.common.persistent.PersistentLoadError;
import io.izzel.kether.common.persistent.QuestTableStore;
import io.izzel.tools.product.Product;

import java.util.List;

public class DiffReader extends SimpleReader {

    private final QuestTableStore.QuestTable questTable;
    private int lastBegin = -1;

    public DiffReader(QuestService<?> service, RewriteLoader.RewriteParser parser, List<String> namespace, QuestTableStore.QuestTable questTable) {
        super(service, parser, namespace);
        this.questTable = questTable;
    }

    protected RewriteLoader.RewriteParser parser() {
        return (RewriteLoader.RewriteParser) parser;
    }

    @Override
    public <T> ParsedAction<T> nextAction() {
        skipBlank();
        switch (peek()) {
            case '-': {
                beginRewrite();
                skip(1);
                ParsedAction<?> before = nextAction();
                boolean transfer = false;
                if (hasNext() && peek() == '+') {
                    skip(1);
                    transfer = true;
                }
                endRewrite();
                ParsedAction<T> action = nextAction();
                if (transfer) {
                    this.questTable.transferTo(before, action);
                } else {
                    this.questTable.drop(before);
                }
                return action;
            } case '+': {
                beginRewrite();
                skip(1);
                endRewrite();
                ParsedAction<T> action = nextAction();
                this.questTable.insert(action);
                return action;
            }
            default: {
                return super.nextAction();
            }
        }
    }

    @Override
    protected <T> ParsedAction<T> wrap(QuestAction<T> action) {
        ParsedAction<T> wrap = super.wrap(action);
        this.questTable.assign(wrap);
        return wrap;
    }

    protected void beginRewrite() {
        if (lastBegin == -1) {
            lastBegin = index;
        } else {
            throw PersistentLoadError.RECURSIVE_REWRITE.create();
        }
    }

    protected void endRewrite() {
        if (lastBegin == -1) {
            throw PersistentLoadError.UNEXPECTED_REWRITE_END.create();
        } else {
            parser().rewriteRules.add(Product.of(lastBegin, index, ""));
            lastBegin = -1;
        }
    }
}
