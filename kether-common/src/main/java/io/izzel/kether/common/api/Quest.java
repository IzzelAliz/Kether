package io.izzel.kether.common.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Quest {

    String getId();

    Optional<Block> getBlock(String label);

    Map<String, Block> getBlocks();

    Optional<Block> blockOf(QuestAction<?> action);

    interface Block {

        String getLabel();

        List<QuestAction<?>> getActions();

        int indexOf(QuestAction<?> action);

        Optional<QuestAction<?>> get(int i);
    }
}
