package io.izzel.kether.common.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ParsedAction<A> {

    private final QuestAction<A> action;
    private final Map<String, Object> properties = new HashMap<>();

    public ParsedAction(QuestAction<A> action) {
        this.action = action;
    }

    public CompletableFuture<A> process(QuestContext.Frame frame) {
        return this.action.process(frame);
    }

    public QuestAction<?> getAction() {
        return this.action;
    }

    public <T> boolean has(ActionProperty<T> key) {
        return this.properties.containsKey(key.id);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ActionProperty<T> key) throws NullPointerException {
        return Objects.requireNonNull((T) this.properties.get(key.id), key.id);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ActionProperty<T> key, T defaultValue) {
        T value = (T) this.properties.get(key.id);
        return value == null ? defaultValue : value;
    }

    public <T> void set(ActionProperty<T> key, T value) {
        Objects.requireNonNull(value);
        this.properties.put(key.id, value);
    }

    @Override
    public String toString() {
        return "Parsed[" + this.action + ", " + this.properties + "]";
    }

    public static <T> ParsedAction<T> noop() {
        return new ParsedAction<>(QuestAction.noop());
    }

    public static final class ActionProperty<T> {

        private final String id;

        private ActionProperty(String id) {
            Objects.requireNonNull(id);
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ActionProperty<?> that = (ActionProperty<?>) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return "ActionProperty{" +
                "id='" + id + '\'' +
                '}';
        }

        public static <T> ActionProperty<T> of(String id) {
            return new ActionProperty<>(id);
        }
    }
}
