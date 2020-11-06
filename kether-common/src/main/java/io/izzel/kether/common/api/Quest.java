package io.izzel.kether.common.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Quest {

    String getId();

    Optional<Block> getBlock(String label);

    Map<String, Block> getBlocks();

    interface Block {

        String getLabel();

        List<QuestAction<?>> getActions();

        Optional<QuestAction<?>> findAction(int address);

        int getAddress(QuestAction<?> action);
    }
}
