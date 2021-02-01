package io.izzel.kether.common.persistent;

import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;

public interface PersistentQuestService<CTX extends QuestContext> extends QuestService<CTX> {

    QuestStorage getStorage();

    @Override
    PersistentRegistry getRegistry();

    static <C extends QuestContext> PersistentQuestService<C> instance() {
        return (PersistentQuestService<C>) QuestService.instance();
    }
}
