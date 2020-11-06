package io.izzel.kether.common.api;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimpleQuest implements Quest {

    private final String id;
    private final Map<String, Block> map = Maps.newHashMap();

    public SimpleQuest(Map<String, Block> map, String id) {
        this.id = id;
        this.map.putAll(map);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Optional<Block> getBlock(String label) {
        return Optional.ofNullable(map.get(label));
    }

    @Override
    public Map<String, Block> getBlocks() {
        return Collections.unmodifiableMap(this.map);
    }

    @Override
    public String toString() {
        return "SimpleQuest{" +
            "id='" + id + '\'' +
            ", map=" + map +
            '}';
    }

    public static class SimpleBlock implements Block {

        private final String label;
        private final List<QuestAction<?>> actions;

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <CTX extends QuestContext> SimpleBlock(String label, List<QuestAction<?>> actions) {
            this.label = label;
            this.actions = (List) actions;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public List<QuestAction<?>> getActions() {
            return this.actions;
        }

        @Override
        public Optional<QuestAction<?>> findAction(int address) {
            return Optional.empty();
        }

        @Override
        public int getAddress(QuestAction<?> action) {
            return 0;
        }

        @Override
        public String toString() {
            return "SimpleBlock{" +
                "label='" + label + '\'' +
                ", actions=" + actions +
                '}';
        }
    }

}
