package io.izzel.kether.common.persistent.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.persistent.PersistentQuestService;
import io.izzel.kether.common.persistent.QuestTableStore;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LocalYamlStorage extends AbstractStorage {

    private final Path playerData;
    private final QuestTableStore questTableStore = null;

    private List<QuestContext> dirtyContexts = new LinkedList<>();
    private Future<?> saveTask;

    public LocalYamlStorage(PersistentQuestService<?> service, Path baseDir) {
        super(service);
        this.playerData = baseDir.resolve("player");
        // todo this.questTableStore = new YamlQuestTableStore(baseDir.resolve("quest"));
    }

    @Override
    public void init() throws Exception {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowUnicode(true);
        KetherRepresenter representer = new KetherRepresenter(service);
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yaml = new Yaml(new KetherConstructor(service), representer, options);
        if (Files.notExists(playerData)) Files.createDirectories(playerData);
        this.saveTask = this.service.getAsyncExecutor().scheduleWithFixedDelay(this::saveDirty, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void close() throws Exception {
        this.saveTask.cancel(true);
        this.saveTask = null;
        this.saveDirty().get();
        map.clear();
    }

    public Future<Void> saveDirty() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.service.getExecutor().execute(() -> {
            Map<QuestContext, String> map = this.dirtyContexts.stream()
                .map(it -> Maps.immutableEntry(it, this.yaml.dump(it)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            this.dirtyContexts = new LinkedList<>();
            this.service.getAsyncExecutor().submit(() -> {
                try {
                    for (Map.Entry<QuestContext, String> entry : map.entrySet()) {
                        QuestContext context = entry.getKey();
                        String dump = entry.getValue();
                        Path path = this.playerData.resolve(context.getPlayerIdentifier()).resolve(context.getQuest().getId() + ".yml");
                        if (Files.notExists(path)) {
                            Files.createDirectories(path.getParent());
                            Files.createFile(path);
                        }
                        Files.copy(new ByteArrayInputStream(dump.getBytes(StandardCharsets.UTF_8)), path, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                future.complete(null);
                return null;
            });
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> updateContext(String playerIdentifier, QuestContext context) {
        this.map.computeIfAbsent(playerIdentifier, k -> new HashMap<>()).put(context.getQuest().getId(), context);
        dirtyContexts.add(context);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected Collection<String> supplyAll(String playerIdentifier) {
        try {
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            Iterator<Path> iterator = Files.walk(playerData.resolve(playerIdentifier)).iterator();
            while (iterator.hasNext()) {
                Path next = iterator.next();
                if (next.toString().endsWith(".yml")) {
                    byte[] bytes = Files.readAllBytes(next);
                    builder.add(new String(bytes, StandardCharsets.UTF_8));
                }
            }
            return builder.build();
        } catch (IOException e) {
            return ImmutableList.of();
        }
    }

    @Override
    public QuestTableStore getTableStore() {
        // todo return this.questTableStore;
        return id -> block -> new QuestTableStore.QuestTable.BlockTable() {
            @Override
            public void pushAction() {
            }

            @Override
            public void transferTo(ParsedAction<?> before, ParsedAction<?> after) {
            }

            @Override
            public void drop(ParsedAction<?> action) {
            }

            @Override
            public void insert(ParsedAction<?> action) {
            }

            @Override
            public void assign(ParsedAction<?> action) {
            }
        };
    }
}
