package io.izzel.kether.common.api;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractQuestContext<This extends AbstractQuestContext<This>> implements QuestContext {

    protected final QuestService<This> service;
    protected final Map<Integer, QuestContext> owningContexts = new HashMap<>();
    protected final AtomicInteger contextCounter = new AtomicInteger(0);
    private final QuestExecutor executor;
    protected This owner;
    private Quest quest;
    private String playerIdentifier;
    protected Quest.Block block;
    protected int index = 0;
    protected Map<String, Object> locals;
    protected ExitStatus exitStatus;
    private CompletableFuture<Object> completeFuture;

    protected Deque<AutoCloseable> closeables = new LinkedBlockingDeque<>();

    @SuppressWarnings("unchecked")
    protected AbstractQuestContext(QuestService<This> service) {
        this.service = service;
        this.owner = (This) this;
        this.executor = new QuestExecutor();
    }

    protected abstract Executor createExecutor();

    public abstract This createChild();

    protected void copy(This from) {
        this.owner = from.owner;
        this.quest = from.getQuest();
        this.playerIdentifier = from.getPlayerIdentifier();
        this.block = from.block;
        this.index = from.index;
        this.locals = from.locals;
        this.exitStatus = from.exitStatus;
        owner.owningContexts.put(owner.contextCounter.getAndIncrement(), this);
    }

    @Override
    public void terminate() {
        for (QuestContext context : this.owningContexts.values()) {
            context.terminate();
        }
        this.cleanup();
        if (completeFuture != null && !completeFuture.isDone()) {
            completeFuture.complete(null);
        }
    }

    private void cleanup() {
        while (!closeables.isEmpty()) {
            try {
                closeables.pollFirst().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public QuestService<This> getService() {
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
    public Quest.Block getBlockRunning() {
        return block;
    }

    public void setBlock(Quest.Block block) {
        this.block = block;
        this.index = 0;
    }

    @Override
    public void setExitStatus(ExitStatus exitStatus) {
        if (exitStatus == null) {
            this.exitStatus = null;
        } else if (this.exitStatus == null) {
            this.exitStatus = exitStatus;
            if (this.owner.exitStatus == null) {
                this.owner.exitStatus = this.exitStatus;
            }
            this.owner.terminate();
        }
    }

    @Override
    public Optional<ExitStatus> getExitStatus() {
        return Optional.ofNullable(exitStatus);
    }

    @Override
    public QuestExecutor getExecutor() {
        return executor;
    }

    @Override
    public CompletableFuture<Object> runActions() {
        if (completeFuture == null) {
            completeFuture = new CompletableFuture<>();
            process(null);
            return completeFuture;
        } else {
            throw new IllegalStateException("rerun context");
        }
    }

    private Optional<? extends QuestAction<?>> nextAction() {
        Optional<? extends QuestAction<?>> optional =
            this.index < this.block.getActions().size() ? Optional.of(this.block.getActions().get(this.index)) : Optional.empty();
        this.index++;
        return optional;
    }

    private void process(CompletableFuture<?> future) {
        while (exitStatus == null) {
            Optional<? extends QuestAction<?>> optional = nextAction();
            if (optional.isPresent()) {
                QuestAction<?> action = optional.get();
                CompletableFuture<?> newFuture = this.runAction(action);
                if (!newFuture.isDone()) {
                    newFuture.thenRunAsync(() -> this.process(newFuture), this.getExecutor());
                    return;
                } else {
                    future = newFuture;
                }
            } else {
                if (exitStatus == null) {
                    this.exitStatus = ExitStatus.success();
                }
                this.completeFuture.complete(future != null && future.isDone() ? future.join() : null);
                this.terminate();
                return;
            }
        }
    }

    @Override
    public <T> CompletableFuture<T> runAction(QuestAction<T> action) {
        CompletableFuture<T> future = action.process(this);
        return future.thenApplyAsync(t -> {
            this.cleanup();
            return t;
        }, getExecutor());
    }

    @Override
    public Map<String, Object> locals() {
        return this.locals;
    }

    @Override
    public void putLocal(String key, Object value) {
        this.locals.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getLocal(String key) {
        return (T) this.locals.get(key);
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
