package io.izzel.kether.common.api;

import com.google.common.base.Preconditions;
import io.izzel.kether.common.api.data.ExitStatus;
import io.izzel.kether.common.api.data.QuestFuture;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class AbstractQuestContext<This extends AbstractQuestContext<This>> implements QuestContext {

    protected final QuestService<This> service;
    protected final Frame rootFrame;
    private final Quest quest;
    private final String playerIdentifier;
    private final QuestExecutor executor;
    protected ExitStatus exitStatus;
    protected CompletableFuture<Object> future;

    protected AbstractQuestContext(QuestService<This> service, Quest quest, String playerIdentifier) {
        this.service = service;
        this.quest = quest;
        this.playerIdentifier = playerIdentifier;
        this.rootFrame = createRootFrame();
        this.executor = new QuestExecutor();
    }

    protected abstract Executor createExecutor();

    protected Frame createRootFrame() {
        return new SimpleNamedFrame(null, new LinkedList<>(), new SimpleVarTable(null), QuestContext.BASE_BLOCK);
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
    public void setExitStatus(ExitStatus exitStatus) {
        this.exitStatus = exitStatus;
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
    public Frame rootFrame() {
        return rootFrame;
    }

    @Override
    public CompletableFuture<Object> runActions() {
        Preconditions.checkState(future == null, "already running");
        return future = rootFrame.run().thenApply(o -> {
            if (this.exitStatus == null) {
                this.exitStatus = ExitStatus.success();
            }
            return o;
        });
    }

    @Override
    public void terminate() {
        this.rootFrame.close();
        if (future != null) {
            future.completeExceptionally(new QuestCloseException());
            future = null;
        }
    }

    @SuppressWarnings("NullableProblems")
    private class QuestExecutor implements Executor {

        private final Executor actual = createExecutor();

        @Override
        public void execute(Runnable command) {
            if (exitStatus == null) {
                actual.execute(command);
            }
        }
    }

    protected abstract class AbstractFrame implements Frame {

        protected final Frame parent;
        protected final List<Frame> frames;
        protected final VarTable varTable;
        protected CompletableFuture<?> future;
        protected Deque<AutoCloseable> closeables = new LinkedBlockingDeque<>();

        public AbstractFrame(Frame parent, List<Frame> frames, VarTable varTable) {
            this.parent = parent;
            this.frames = frames;
            this.varTable = varTable;
        }

        @Override
        public QuestContext context() {
            return AbstractQuestContext.this;
        }

        @Override
        public List<Frame> children() {
            return this.frames;
        }

        @Override
        public Optional<Frame> parent() {
            return Optional.ofNullable(parent);
        }

        @Override
        public Frame newFrame(String name) {
            SimpleNamedFrame frame = new SimpleNamedFrame(this, new LinkedList<>(), new SimpleVarTable(this), name);
            this.frames.add(frame);
            return frame;
        }

        @Override
        public Frame newFrame(ParsedAction<?> action) {
            Frame frame;
            if (action.get(ActionProperties.REQUIRE_FRAME, false)) {
                frame = new SimpleNamedFrame(this, new LinkedList<>(), new SimpleVarTable(this), "__anon__" + System.nanoTime());
                frame.setNext(action);
            } else {
                frame = new SimpleActionFrame(this, new LinkedList<>(), new SimpleVarTable(this), action);
            }
            this.frames.add(frame);
            return frame;
        }

        @Override
        public VarTable variables() {
            return this.varTable;
        }

        @Override
        public <T extends AutoCloseable> T addClosable(T closeable) {
            this.closeables.addFirst(closeable);
            return closeable;
        }

        @Override
        public void close() {
            if (this.future == null) return;
            for (Frame frame : this.frames) {
                frame.close();
            }
            this.cleanup();
            this.future = null;
        }

        @Override
        public boolean isDone() {
            return this.future == null || this.future.isDone();
        }

        void cleanup() {
            while (!closeables.isEmpty()) {
                try {
                    closeables.pollFirst().close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected class SimpleNamedFrame extends AbstractFrame {

        private final String name;
        private Quest.Block block, next;
        private int sp = -1, np = -1;

        public SimpleNamedFrame(Frame parent, List<Frame> frames, VarTable varTable, String name) {
            super(parent, frames, varTable);
            this.name = name;
            setNext(quest.getBlock(name).orElseThrow(IllegalArgumentException::new));
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public Optional<ParsedAction<?>> currentAction() {
            if (block == null || sp == -1) {
                return Optional.empty();
            } else {
                return block.get(sp);
            }
        }

        @Override
        public void setNext(ParsedAction<?> action) {
            if (block != null) {
                np = block.indexOf(action);
                if (np == -1) next = null;
            }
            if (next == null) {
                Optional<Quest.Block> optional = quest.blockOf(action);
                if (optional.isPresent()) {
                    next = optional.get();
                    np = next.indexOf(action);
                } else {
                    throw new IllegalArgumentException(action + " is not in quest");
                }
            }
        }

        @Override
        public void setNext(Quest.Block block) {
            next = block;
            np = 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> CompletableFuture<T> run() {
            Preconditions.checkState(this.future == null, "already running");
            varTable.initialize(this);
            future = new CompletableFuture<>();
            process(future);
            return (CompletableFuture<T>) future;
        }

        @SuppressWarnings("unchecked")
        private void process(CompletableFuture<?> future) {
            while (exitStatus == null) {
                this.cleanup();
                this.frames.removeIf(Frame::isDone);
                Optional<? extends ParsedAction<?>> optional = nextAction();
                if (optional.isPresent()) {
                    ParsedAction<?> action = optional.get();
                    CompletableFuture<?> newFuture = action.process(this);
                    if (!newFuture.isDone()) {
                        newFuture.thenRunAsync(() -> this.process(newFuture), getExecutor());
                        return;
                    } else {
                        future = newFuture;
                    }
                } else {
                    ((CompletableFuture<Object>) this.future).complete(future != null && future.isDone() ? future.join() : null);
                    return;
                }
            }
        }

        private Optional<? extends ParsedAction<?>> nextAction() {
            if (next != null && np != -1) {
                return (block = next).get(sp = np++);
            } else return Optional.empty();
        }
    }

    protected class SimpleActionFrame extends AbstractFrame {

        protected final ParsedAction<?> action;

        public SimpleActionFrame(Frame parent, List<Frame> frames, VarTable varTable, ParsedAction<?> action) {
            super(parent, frames, varTable);
            this.action = action;
        }

        @Override
        public String name() {
            return this.action.toString();
        }

        @Override
        public Optional<ParsedAction<?>> currentAction() {
            return Optional.of(action);
        }

        @Override
        public void setNext(ParsedAction<?> action) {
            if (this.parent != null) {
                this.parent.setNext(action);
            }
        }

        @Override
        public void setNext(Quest.Block block) {
            if (this.parent != null) {
                this.parent.setNext(block);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> CompletableFuture<T> run() {
            Preconditions.checkState(this.future == null, "already running");
            this.varTable.initialize(this);
            return (CompletableFuture<T>) (this.future = this.action.process(this));
        }
    }

    protected static class SimpleVarTable implements VarTable {

        private final Frame parent;
        private final Map<String, Object> map;

        public SimpleVarTable(Frame parent) {
            this(parent, new HashMap<>());
        }

        public SimpleVarTable(Frame parent, Map<String, Object> map) {
            this.parent = parent;
            this.map = map;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> get(String name) throws CompletionException {
            Object o = map.get(name);
            if (o == null && parent != null) {
                return parent.variables().get(name);
            }
            if (o instanceof QuestFuture<?>) {
                o = ((QuestFuture<?>) o).getFuture().join();
            }
            return (Optional<T>) Optional.ofNullable(o);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<QuestFuture<T>> getFuture(String name) {
            Object o = map.get(name);
            if (o == null && parent != null) {
                return parent.variables().getFuture(name);
            }
            if (o instanceof QuestFuture) {
                return Optional.of((QuestFuture<T>) o);
            } else {
                return Optional.empty();
            }
        }

        @Override
        public void set(String name, Object value) {
            this.map.put(name, value);
        }

        @Override
        public <T> void set(String name, ParsedAction<T> owner, CompletableFuture<T> future) {
            this.map.put(name, new QuestFuture<>(owner, future));
        }

        @Override
        public Set<String> keys() {
            return Collections.unmodifiableSet(this.map.keySet());
        }

        @Override
        public Collection<Map.Entry<String, Object>> values() {
            return Collections.unmodifiableCollection(this.map.entrySet());
        }

        @Override
        public void initialize(Frame frame) {
            for (Object o : this.map.values()) {
                if (o instanceof QuestFuture) {
                    ((QuestFuture<?>) o).run(frame);
                }
            }
        }

        @Override
        public void close() {
            for (Object o : this.map.values()) {
                if (o instanceof QuestFuture) {
                    ((QuestFuture<?>) o).close();
                }
            }
        }
    }
}
