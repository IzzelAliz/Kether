package io.izzel.kether.common.persistent;

import io.izzel.kether.common.api.ParsedAction;

public interface QuestTableStore {

    QuestTable getTable(String id);

    interface QuestTable {

        BlockTable inBlock(String block);

        interface BlockTable {

            void pushAction();

            void transferTo(ParsedAction<?> before, ParsedAction<?> after);

            void drop(ParsedAction<?> action);

            void insert(ParsedAction<?> action);

            void assign(ParsedAction<?> action);
        }
    }
}
