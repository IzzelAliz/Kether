package io.izzel.kether.common.api;

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
    }
}
