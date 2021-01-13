package io.izzel.kether.common.api.data;

import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestContext;

import java.util.concurrent.CompletableFuture;

public class IndexedAction<T> extends QuestAction<T> {

    private final QuestAction<T> action;
    private final Quest quest;
    private final Quest.Block block;
    private final int index;

    public IndexedAction(QuestAction<T> action, Quest quest, Quest.Block block, int index) {
        this.action = action;
        this.quest = quest;
        this.block = block;
        this.index = index;
    }

    @Override
    public CompletableFuture<T> process(QuestContext.Frame frame) {
        return action.process(frame);
    }

    public QuestAction<T> getAction() {
        return action;
    }

    public Quest getQuest() {
        return quest;
    }

    public Quest.Block getBlock() {
        return block;
    }

    public int getIndex() {
        return index;
    }
}
