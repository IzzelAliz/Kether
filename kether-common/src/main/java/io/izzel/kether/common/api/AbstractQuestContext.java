package io.izzel.kether.common.api;

import java.util.Deque;
import java.util.Map;
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

    private boolean doJump;
    private String jumpBlock;
    private int jumpIndex;

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
            child.terminate();
        }
        while (!closeables.isEmpty()) {
            try {
                closeables.pollFirst().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (parent != null) {
            parent.getTempData().remove(childKey);
            if (exitStatus != null) {
                parent.setExitStatus(exitStatus);
            }
        }
    }

    protected void nextAction() {
        if (this.doJump) {
            setRunningBlock(jumpBlock);
            setIndex(jumpIndex);
            this.doJump = false;
        } else {
            setIndex(getIndex() + 1);
        }
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
        this.exitStatus = exitStatus;
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
        CompletableFuture<Void> future = new CompletableFuture<>();
        process(future);
        return future;
    }

    private void process(CompletableFuture<Void> future) {
        while (true) {
            if (exitStatus != null) {
                this.terminate();
                return;
            }
            Optional<? extends QuestAction<?, QuestContext>> optional = quest.getBlock(getRunningBlock()).map(block -> block.getActions().get(getIndex()));
            if (optional.isPresent()) {
                QuestAction<?, QuestContext> action = optional.get();
                if (action.isAsync()) {
                    CompletableFuture<?> ret = runAction(action.getDataPrefix(), action);
                    ret.thenRunAsync(() -> {
                        nextAction();
                        process(future);
                    }, getExecutor());
                    return;
                } else {
                    runAction(action.getDataPrefix(), action).join();
                    nextAction();
                }
            } else {
                if (exitStatus == null && parent == null) {
                    exitStatus = ExitStatus.success();
                }
                this.terminate();
                future.complete(null);
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

    @Override
    public Map<String, Object> getTempData() {
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
