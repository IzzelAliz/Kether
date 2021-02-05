package io.izzel.kether.common.loader;

import com.google.common.collect.ImmutableMap;
import io.izzel.kether.common.actions.GetAction;
import io.izzel.kether.common.actions.LiteralAction;
import io.izzel.kether.common.api.ActionProperties;
import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.api.data.VarString;

import java.util.*;
import java.util.function.BiFunction;

public class SimpleReader extends AbstractStringReader implements QuestReader {

    protected final List<String> namespace;
    protected final QuestService<?> service;
    protected final SimpleQuestLoader.Parser parser;

    public SimpleReader(QuestService<?> service, SimpleQuestLoader.Parser parser, List<String> namespace) {
        super(parser.arr);
        this.service = service;
        this.parser = parser;
        this.index = parser.index;
        this.namespace = new ArrayList<>(namespace);
        this.namespace.add("kether");
    }

    @Override
    public String nextToken() {
        skipBlank();
        switch (peek()) {
            case '"': {
                int cnt = 0;
                while (hasNext() && peek() == '"') {
                    cnt++;
                    skip(1);
                }
                int met = 0;
                int i;
                for (i = index; i < arr.length; ++i) {
                    if (arr[i] == '"') met++;
                    else {
                        if (met >= cnt) break;
                        else met = 0;
                    }
                }
                if (met < cnt) throw LoadError.STRING_NOT_CLOSE.create();
                String ret = new String(arr, index, i - cnt - index);
                index = i;
                return ret;
            }
            case '\'': {
                skip(1);
                int i = index;
                while (peek() != '\'') {
                    skip(1);
                }
                String ret = new String(arr, i, index - i);
                skip(1);
                return ret;
            }
            default: {
                return super.nextToken();
            }
        }
    }

    @Override
    public VarString nextString() {
        String str = nextToken();
        if (!Character.isWhitespace(peek())) {
            String[] ids = nextToken().split(",");
            Map<String, BiFunction<QuestContext.Frame, String, String>> map = new HashMap<>();
            for (String id : ids) {
                Optional<BiFunction<QuestContext.Frame, String, String>> optional = service.getRegistry().getStringProcessor(id);
                optional.ifPresent(f -> map.put(id, f));
            }
            return new VarString(str, map);
        }
        return new VarString(str, ImmutableMap.of());
    }

    protected ParsedAction<?> nextAnonAction() {
        ParsedAction<?> parsedAction = parser.readAnonymousAction();
        parsedAction.set(ActionProperties.REQUIRE_FRAME, true);
        return parsedAction;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParsedAction<T> nextAction() {
        skipBlank();
        switch (peek()) {
            case '{': {
                parser.index = this.index;
                ParsedAction<?> action = nextAnonAction();
                this.index = parser.index;
                return (ParsedAction<T>) action;
            }
            case '&': {
                skip(1);
                beforeParse();
                return wrap(new GetAction<>(nextToken()));
            }
            case '*': {
                skip(1);
                beforeParse();
                return wrap(new LiteralAction<>(nextToken()));
            }
            default: {
                String element = nextToken();
                Optional<QuestActionParser> optional = service.getRegistry().getParser(element, namespace);
                if (optional.isPresent()) {
                    beforeParse();
                    return wrap(optional.get().resolve(this));
                }
                throw LoadError.UNKNOWN_ACTION.create(element);
            }
        }
    }

    protected void beforeParse() {
    }

    protected <T> ParsedAction<T> wrap(QuestAction<T> action) {
        return new ParsedAction<>(action);
    }

    @Override
    public void expect(String value) {
        super.expect(value);
    }

    @Override
    public boolean flag(String name) {
        skipBlank();
        if (peek() == '-') {
            mark();
            String s = nextToken();
            if (s.substring(1).equals(name)) {
                return true;
            } else {
                reset();
                return false;
            }
        }
        return false;
    }

    @Override
    public <T> Optional<T> optionalFlag(String name, ArgType<T> flagType) {
        skipBlank();
        if (peek() == '-') {
            mark();
            String s = nextToken();
            if (s.substring(1).equals(name)) {
                return Optional.of(next(flagType));
            } else {
                reset();
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
