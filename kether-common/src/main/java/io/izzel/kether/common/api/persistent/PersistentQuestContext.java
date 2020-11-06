package io.izzel.kether.common.api.persistent;

import io.izzel.kether.common.api.AbstractQuestContext;
import io.izzel.kether.common.api.QuestService;

public abstract class PersistentQuestContext<This extends PersistentQuestContext<This>> extends AbstractQuestContext<This> {

    protected PersistentQuestContext(QuestService<This> service) {
        super(service);
    }

    public abstract This createChild();

    protected void copy(This from) {
        super.copy(from);
    }

}
