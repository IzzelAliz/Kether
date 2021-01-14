package io.izzel.kether.common.loader.types;

import io.izzel.kether.common.loader.ArgType;
import io.izzel.kether.common.loader.QuestReader;
import io.izzel.kether.common.util.LocalizedException;

import java.time.Duration;
import java.util.Locale;

final class DurationType implements ArgType<Duration> {

    @Override
    public Duration read(QuestReader reader) throws LocalizedException {
        String s = reader.nextToken().toUpperCase(Locale.ENGLISH);
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
            return Duration.parse(s);
        } catch (Exception e) {
            throw LocalizedException.of("not-duration", s);
        }
    }
}
