package io.izzel.kether.common.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SettingsContext implements QuestContext {

    private final QuestService<?> service;
    private final Quest quest;
    private int index = 0;
    private ExitStatus exitStatus;
    private final Map<String, Object> map = new LinkedHashMap<>();

    public SettingsContext(QuestService<?> service, Quest quest) {
        this.service = service;
        this.quest = quest;
    }

    @Override
    public Quest getQuest() {
        return quest;
    }

    @Override
    public String getPlayerIdentifier() {
        throw new IllegalStateException("player not available in settings");
    }

    @Override
    public String getRunningBlock() {
        return "settings";
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setExitStatus(ExitStatus exitStatus) {
        this.exitStatus = exitStatus;
    }

    @Override
    public Optional<ExitStatus> getExitStatus() {
        return Optional.ofNullable(exitStatus);
    }

    @Override
    public void setJump(String block, int index) {
        throw new IllegalStateException("jump is not available in settings");
    }

    @Override
    public <C extends QuestContext> C createChild(String key, boolean anonymous) {
        throw new IllegalStateException("call is not available in settings");
    }

    @Override
    public CompletableFuture<Void> runActions() {
        Quest.Block settings = quest.getBlocks().get("settings");
        if (settings != null) {
            for (QuestAction<?, QuestContext> action : settings.getActions()) {
                action.process(this);
                index++;
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Executor getExecutor() {
        return service.getExecutor();
    }

    @Override
    public void setDataKey(String key) {

    }

    @Override
    public String getDataKey() {
        return "default";
    }

    @Override
    public <T, C extends QuestContext> CompletableFuture<T> runAction(String key, QuestAction<T, C> action) {
        throw new IllegalStateException("call is not available in settings");
    }

    @Override
    public Map<String, Object> getTempData() {
        return map;
    }

    @Override
    public Map<String, Object> getPersistentData() {
        return map;
    }

    @Override
    public <T extends AutoCloseable> T addClosable(T closeable) {
        throw new IllegalStateException();
    }

    @Override
    public void terminate() {

    }

    @Override
    public boolean isDirty() {
        return false;
    }
}
