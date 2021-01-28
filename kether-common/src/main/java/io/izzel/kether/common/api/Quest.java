package io.izzel.kether.common.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Quest {

    String getId();

    Optional<Block> getBlock(String label);

    Map<String, Block> getBlocks();

    Optional<Block> blockOf(ParsedAction<?> action);

    interface Block {

        String getLabel();

        List<ParsedAction<?>> getActions();

        int indexOf(ParsedAction<?> action);

        Optional<ParsedAction<?>> get(int i);
    }
}
