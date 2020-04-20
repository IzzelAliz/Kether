package io.izzel.kether.common.api;

import java.util.Map;
import java.util.function.BiFunction;

public class ContextString {

    private final String origin;
    private final Map<String, BiFunction<QuestContext, String, String>> map;

    public ContextString(String origin, Map<String, BiFunction<QuestContext, String, String>> map) {
        this.origin = origin;
        this.map = map;
    }

    public String get(QuestContext context) {
        String ret = origin;
        for (Map.Entry<String, BiFunction<QuestContext, String, String>> entry : map.entrySet()) {
            try {
                ret = entry.getValue().apply(context, ret);
            } catch (Throwable t) {
                return String.format("Exception processing context string %s: %s", entry.getKey(), t.toString());
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        return "\"\"\"" + origin + "\"\"\"" + String.join(",", map.keySet());
    }
}