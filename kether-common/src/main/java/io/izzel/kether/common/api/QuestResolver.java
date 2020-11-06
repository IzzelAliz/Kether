package io.izzel.kether.common.api;

import com.google.common.collect.ImmutableList;
import io.izzel.kether.common.util.Coerce;
import io.izzel.kether.common.util.LocalizedException;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public interface QuestResolver {

    char peek();

    char peek(int n);

    int getIndex();

    int getMark();

    void setIndex(int index);

    boolean hasNext();

    String nextElement();

    ContextString nextContextString();

    ContextString contexted(String str);

    void mark();

    void reset();

    <T> QuestAction<T> nextAction();

    default void consume(String value) {
        String element = nextElement();
        if (!element.equals(value)) {
            throw LocalizedException.of("not-match", value, element);
        }
    }

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

    default List<QuestAction<?>> nextList() {
        String element = this.nextElement();
        if (!ImmutableList.of("[", "begin").contains(element)) {
            throw LocalizedException.of("not-match", "[ / begin", element);
        }
        ImmutableList.Builder<QuestAction<?>> builder = ImmutableList.builder();
        while (this.hasNext()) {
            this.mark();
            String end = this.nextElement();
            if ((element.equals("[") && end.equals("]")) ||
                (element.equals("begin") && end.equals("end"))) {
                break;
            } else {
                this.reset();
                builder.add(this.nextAction());
            }
        }
        return builder.build();
    }

    static QuestResolver of(QuestService<?> service, String text) {
        return new SimpleResolver<>(service, text);
    }
}
