package io.izzel.kether.common.loader;

import com.google.common.collect.ImmutableMap;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.api.data.ContextString;
import io.izzel.kether.common.util.LocalizedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class SimpleReader implements QuestReader {

    private final QuestService<?> service;
    private final char[] arr;
    private int index = 0;
    private int mark = 0;

    public SimpleReader(QuestService<?> service, String text) {
        this.service = service;
        this.arr = text.toCharArray();
    }

    @Override
    public char peek() {
        return arr[index];
    }

    @Override
    public char peek(int n) {
        return arr[index + n];
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getMark() {
        return mark;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean hasNext() {
        skipBlank();
        return index < arr.length;
    }

    @Override
    public String nextToken() {
        skipBlank();
        if (arr.length - index >= 6 && peek() == '"'
            && peek(1) == '"' && peek(2) == '"') {
            index += 3;
            int cnt = 0;
            int i;
            for (i = index; i < arr.length; ++i) {
                if (arr[i] == '"') cnt++;
                else {
                    if (cnt >= 3) break;
                    else cnt = 0;
                }
            }
            if (cnt < 3) throw LocalizedException.of("string-not-close");
            String ret = new String(arr, index, i - 3 - index);
            index = i;
            return ret;
        } else {
            int begin = index;
            while (index < arr.length && !Character.isWhitespace(arr[index])) {
                index++;
            }
            return new String(arr, begin, index - begin);
        }
    }

    @Override
    public ContextString nextString() {
        String str = nextToken();
        if (!Character.isWhitespace(peek())) {
            String[] ids = nextToken().split(",");
            Map<String, BiFunction<QuestContext.Frame, String, String>> map = new HashMap<>();
            for (String id : ids) {
                Optional<BiFunction<QuestContext.Frame, String, String>> optional = service.getRegistry().getContextStringProcessor(id);
                optional.ifPresent(f -> map.put(id, f));
            }
            return new ContextString(str, map);
        }
        return new ContextString(str, ImmutableMap.of());
    }

    @Override
    public void mark() {
        this.mark = index;
    }

    @Override
    public void reset() {
        this.index = mark;
    }

    @Override
    public <T> QuestAction<T> nextAction() {
        skipBlank();
        String element = nextToken();
        Optional<QuestActionParser> optional = service.getRegistry().getParser(element);
        if (optional.isPresent()) {
            return optional.get().resolve(this);
        } else {
            throw LocalizedException.of("unknown-action", element);
        }
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

    private void skipBlank() {
        while (index < arr.length) {
            if (Character.isWhitespace(arr[index])) {
                index++;
            } else if (index + 1 < arr.length && arr[index] == '/' && arr[index + 1] == '/') {
                while (index < arr.length && arr[index] != '\n' && arr[index] != '\r') {
                    index++;
                }
            } else {
                break;
            }
        }
    }
}
