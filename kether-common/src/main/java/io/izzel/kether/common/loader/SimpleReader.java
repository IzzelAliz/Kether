package io.izzel.kether.common.loader;

import com.google.common.collect.ImmutableMap;
import io.izzel.kether.common.api.ActionProperties;
import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.api.data.VarString;
import io.izzel.kether.common.util.LocalizedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class SimpleReader extends AbstractStringReader implements QuestReader {

    private final QuestService<?> service;
    private final SimpleQuestLoader.Parser parser;

    public SimpleReader(QuestService<?> service, SimpleQuestLoader.Parser parser) {
        super(parser.arr);
        this.service = service;
        this.parser = parser;
        this.index = parser.index;
    }

    @Override
    public String nextToken() {
        if (hasNext() && peek() == '"') {
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
            if (met < cnt) throw LocalizedException.of("string-not-close");
            String ret = new String(arr, index, i - cnt - index);
            index = i;
            return ret;
        } else {
            return super.nextToken();
        }
    }

    @Override
    public VarString nextString() {
        String str = nextToken();
        if (!Character.isWhitespace(peek())) {
            String[] ids = nextToken().split(",");
            Map<String, BiFunction<QuestContext.Frame, String, String>> map = new HashMap<>();
            for (String id : ids) {
                Optional<BiFunction<QuestContext.Frame, String, String>> optional = service.getRegistry().getContextStringProcessor(id);
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
        if (hasNext() && peek() == '{') {
            parser.index = this.index;
            ParsedAction<?> action = nextAnonAction();
            this.index = parser.index;
            return (ParsedAction<T>) action;
        } else {
            String element = nextToken();
            Optional<QuestActionParser> optional = service.getRegistry().getParser(element);
            if (optional.isPresent()) {
                QuestAction<T> action = optional.get().resolve(this);
                return this.wrap(action);
            } else {
                throw LocalizedException.of("unknown-action", element);
            }
        }
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
