package io.izzel.kether.common.persistent;

import io.izzel.kether.common.api.AbstractQuestContext;
import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestService;

public abstract class PersistentQuestContext<This extends PersistentQuestContext<This>> extends AbstractQuestContext<This> {

    protected PersistentQuestContext(QuestService<This> service, Quest quest, String playerIdentifier) {
        super(service, quest, playerIdentifier);
    }

    @Override
    protected Frame createRootFrame() {
        return super.createRootFrame();
    }
}
