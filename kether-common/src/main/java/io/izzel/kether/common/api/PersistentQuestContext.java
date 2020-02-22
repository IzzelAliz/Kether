package io.izzel.kether.common.api;

import io.izzel.kether.common.actions.DataAction;
import io.izzel.kether.common.util.Coerce;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class PersistentQuestContext extends AbstractQuestContext {

    private boolean doRecover = true;

    protected PersistentQuestContext(QuestService<?> service, AbstractQuestContext parent, Quest quest, String playerIdentifier, String runningBlock, int index, String dataKey, Map<String, Object> tempData, Map<String, Object> persistentData, String childKey) {
        super(service, parent, quest, playerIdentifier, runningBlock, index, dataKey, tempData, persistentData, childKey);
    }

    @Override
    public CompletableFuture<Void> runActions() {
        if (doRecover) {
            Object o = this.tempData.get("$");
            if (o instanceof Map) {
                Map<String, Object> map = ((Map<String, Object>) o);
                this.setIndex(Coerce.asInteger(map.get("index")).orElse(this.getIndex()));
                this.setRunningBlock(Coerce.asString(map.get("block")).orElse(this.getRunningBlock()));
                this.setDataKey(Coerce.asString(map.get("dataKey")).orElse(this.getDataKey()));
                try {
                    Object prefix = map.get("action");
                    if (prefix instanceof String) {
                        String dataPrefix = this.currentAction().getDataPrefix();
                        if (!prefix.equals(dataPrefix)) {
                            String dataKey = BASE_DATA_KEY;
                            Quest.Block block = this.getQuest().getBlock(this.getRunningBlock()).orElseThrow(NullPointerException::new);
                            for (int i = 0; i < block.getActions().size(); i++) {
                                QuestAction<?, QuestContext> questAction = block.getActions().get(i);
                                if (questAction instanceof DataAction) {
                                    dataKey = ((DataAction<?>) questAction).getKey();
                                }
                                if (dataKey.equals(this.getDataKey()) && prefix.equals(questAction.getDataPrefix())) {
                                    this.setIndex(i);
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception ignored) { }
            }
            doRecover = false;
        }
        return super.runActions();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void nextAction() {
        super.nextAction();
        Map<String, Object> map = (Map<String, Object>) this.tempData.computeIfAbsent("$", k -> new HashMap<>());
        map.put("index", this.getIndex());
        map.put("block", this.getRunningBlock());
        map.put("dataKey", this.getDataKey());
        try {
            map.put("action", this.currentAction().getDataPrefix());
        } catch (Exception ignored) { }
    }

    private QuestAction<?, ?> currentAction() {
        return this.getQuest().getBlock(this.getRunningBlock()).orElseThrow(NullPointerException::new)
            .getActions().get(this.getIndex());
    }
}
