package io.izzel.kether.common;

import com.google.common.collect.Maps;
import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestContext;

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
        private final List<QuestAction<?, QuestContext>> actions;

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <CTX extends QuestContext> SimpleBlock(String label, List<QuestAction<?, CTX>> actions) {
            this.label = label;
            this.actions = (List) actions;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public <C extends QuestContext> List<QuestAction<?, C>> getActions() {
            return (List) actions;
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
