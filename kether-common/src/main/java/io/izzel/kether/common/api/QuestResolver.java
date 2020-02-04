package io.izzel.kether.common.api;

import io.izzel.kether.common.SimpleResolver;
import io.izzel.kether.common.util.Coerce;
import io.izzel.kether.common.util.LocalizedException;

import java.time.Duration;
import java.util.Locale;

public interface QuestResolver<CTX extends QuestContext> {

    char peek();

    char peek(int n);

    int getIndex();

    int getMark();

    void setIndex(int index);

    boolean hasNext();

    String nextElement();

    void mark();

    void reset();

    <T> QuestAction<T, CTX> nextAction();

    default int nextInt() {
        String element = nextElement();
        return Coerce.asInteger(element).orElseThrow(LocalizedException.supply("not-integer", element));
    }

    default long nextLong() {
        String element = nextElement();
        return Coerce.asLong(element).orElseThrow(LocalizedException.supply("not-long", element));
    }

    default double nextDouble() {
        String element = nextElement();
        return Coerce.asDouble(element).orElseThrow(LocalizedException.supply("not-double", element));
    }

    default long nextDuration() {
        String s = nextElement().toUpperCase(Locale.ENGLISH);
        if (!s.contains("T")) {
            if (s.contains("D")) {
                if (s.contains("H") || s.contains("M") || s.contains("S")) {
                    s = s.replace("D", "DT");
                }
            } else {
                if (s.startsWith("P")) {
                    s = "PT" + s.substring(1);
                } else {
                    s = "T" + s;
                }
            }
        }
        if (!s.startsWith("P")) {
            s = "P" + s;
        }
        try {
            return Duration.parse(s).toMillis();
        } catch (Exception e) {
            throw LocalizedException.of("not-duration", s);
        }
    }

    static <C extends QuestContext> QuestResolver<C> of(QuestService<C> service, String text) {
        return new SimpleResolver<>(service, text);
    }
}
