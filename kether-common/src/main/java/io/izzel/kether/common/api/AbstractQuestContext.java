package io.izzel.kether.common.api;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class AbstractQuestContext implements QuestContext {

    private final QuestService<?> service;
    protected final AbstractQuestContext parent;
    protected final String childKey;
    private final Quest quest;
    private final String playerIdentifier;
    private final QuestExecutor executor;
    protected String runningBlock;
    protected int index;
    protected String dataKey;
    protected Map<String, Object> tempData;
    protected Map<String, Object> persistentData;
    private ExitStatus exitStatus;
    private CompletableFuture<Void> completeFuture;

    private boolean doJump;
    private String jumpBlock;
    private int jumpIndex;
    private boolean modified = false;

    protected Deque<AutoCloseable> closeables = new LinkedBlockingDeque<>();
    protected Deque<AbstractQuestContext> children = new LinkedBlockingDeque<>();

    protected AbstractQuestContext(QuestService<?> service, AbstractQuestContext parent, Quest quest, String playerIdentifier, String runningBlock, int index, String dataKey, Map<String, Object> tempData, Map<String, Object> persistentData, String childKey) {
        this.service = service;
        this.parent = parent;
        this.quest = quest;
        this.playerIdentifier = playerIdentifier;
        this.runningBlock = runningBlock;
        this.index = index;
        this.tempData = tempData;
        this.persistentData = persistentData;
        this.childKey = childKey;
        this.dataKey = dataKey;
        this.executor = new QuestExecutor();
    }

    protected abstract Executor createExecutor();

    public abstract <C extends QuestContext> C createChild(String key);

    @Override
    public void terminate() {
        for (AbstractQuestContext child : getChildren()) {
            child.exitStatus = this.exitStatus;
            child.terminate();
        }
        while (!closeables.isEmpty()) {
            try {
                closeables.pollFirst().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (exitStatus != null && !exitStatus.equals(ExitStatus.paused())) {
            this.tempData.clear();
        }
        if (completeFuture != null) {
            completeFuture.complete(null);
        }
    }

    protected void nextAction() {
        this.tempData.clear();
        if (this.doJump) {
            setRunningBlock(jumpBlock);
            setIndex(jumpIndex);
            this.doJump = false;
        } else {
            setIndex(getIndex() + 1);
        }
        modified = true;
    }

    protected Deque<AbstractQuestContext> getChildren() {
        return children;
    }

    public QuestService<?> getService() {
        return service;
    }

    @Override
    public Quest getQuest() {
        return quest;
    }

    @Override
    public String getPlayerIdentifier() {
        return playerIdentifier;
    }

    @Override
    public String getRunningBlock() {
        return runningBlock;
    }

    public void setRunningBlock(String runningBlock) {
        this.runningBlock = runningBlock;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public void setDataKey(String key) {
        this.dataKey = key;
    }

    @Override
    public String getDataKey() {
        return dataKey;
    }

    @Override
    public void setJump(String block, int index) {
        doJump = true;
        jumpBlock = block;
        jumpIndex = index;
    }

    @Override
    public void setExitStatus(ExitStatus exitStatus) {
        if (this.exitStatus == null) {
            this.exitStatus = exitStatus;
            AbstractQuestContext root = this;
            while (root.parent != null) root = root.parent;
            if (root.exitStatus == null) {
                root.exitStatus = this.exitStatus;
                root.terminate();
            }
        } else if (exitStatus == null) {
            this.exitStatus = null;
        }
    }

    @Override
    public Optional<ExitStatus> getExitStatus() {
        return Optional.ofNullable(exitStatus);
    }

    @Override
    public Executor getExecutor() {
        if (parent != null) return parent.getExecutor();
        return executor;
    }

    @Override
    public CompletableFuture<Void> runActions() {
        if (completeFuture == null) {
            completeFuture = new CompletableFuture<>();
            process();
            return completeFuture;
        } else {
            throw new IllegalStateException("rerun context");
        }
    }

    private void process() {
        while (exitStatus == null) {
            Optional<? extends QuestAction<?, QuestContext>> optional = quest.getBlock(getRunningBlock()).map(block -> block.getActions().get(getIndex()));
            if (optional.isPresent()) {
                QuestAction<?, QuestContext> action = optional.get();
                if (action.isAsync()) {
                    CompletableFuture<?> ret = runAction(action.getDataPrefix(), action);
                    ret.thenRunAsync(() -> {
                        nextAction();
                        process();
                    }, getExecutor());
                    return;
                } else {
                    runAction(action.getDataPrefix(), action).join();
                    nextAction();
                }
            } else {
                if (exitStatus == null) {
                    this.exitStatus = ExitStatus.success();
                }
                this.terminate();
                return;
            }
        }
    }

    @Override
    public <T, C extends QuestContext> CompletableFuture<T> runAction(String key, QuestAction<T, C> action) {
        C child = this.createChild(key);
        CompletableFuture<T> future = action.process(child);
        return future.thenApplyAsync(t -> {
            child.terminate();
            return t;
        }, getExecutor());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getTempData() {
        if (tempData == null) { // lazy load
            if (parent != null) {
                tempData = (Map<String, Object>) parent.getTempData().computeIfAbsent(childKey, k -> new HashMap<>());
            } else {
                tempData = new HashMap<>();
            }
        }
        return tempData;
    }

    @Override
    public Map<String, Object> getPersistentData() {
        if (parent != null) return parent.getPersistentData();
        else return persistentData;
    }

    @Override
    public <T extends AutoCloseable> T addClosable(T closeable) {
        this.closeables.addFirst(closeable);
        return closeable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractQuestContext that = (AbstractQuestContext) o;
        return getDataKey().equals(that.getDataKey()) &&
            Objects.equals(getTempData(), that.getTempData()) &&
            getPersistentData().equals(that.getPersistentData()) &&
            Objects.equals(getExitStatus(), that.getExitStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDataKey(), getTempData(), getPersistentData(), getExitStatus());
    }

    @Override
    public boolean compareChange(QuestContext context) {
        if (context == null) {
            return !modified && tempData.isEmpty() && persistentData.isEmpty();
        } else {
            return this.equals(context);
        }
    }

    @SuppressWarnings("NullableProblems")
    private class QuestExecutor implements Executor {

        private final Executor actual = createExecutor();

        @Override
        public void execute(Runnable command) {
            if (!getExitStatus().isPresent()) {
                actual.execute(command);
            }
        }
    }
}
