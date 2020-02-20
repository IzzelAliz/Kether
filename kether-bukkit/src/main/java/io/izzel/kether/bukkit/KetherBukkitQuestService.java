package io.izzel.kether.bukkit;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import io.izzel.kether.common.QuestLoader;
import io.izzel.kether.common.api.DefaultRegistry;
import io.izzel.kether.common.api.ExitStatus;
import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestRegistry;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.api.QuestStorage;
import io.izzel.taboolib.module.locale.TLocale;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class KetherBukkitQuestService implements QuestService<BukkitQuestContext> {

    private final QuestRegistry registry = new DefaultRegistry();
    private final Executor syncExecutor = new BukkitSchedulerExecutor();
    private final ScheduledExecutorService asyncExecutor = Executors.newScheduledThreadPool(2);

    private final ListMultimap<String, BukkitQuestContext> runningQuests = MultimapBuilder.hashKeys().arrayListValues().build();

    private Map<String, Quest> questMap;

    public static KetherBukkitQuestService instance() {
        return (KetherBukkitQuestService) Bukkit.getServicesManager().load(QuestService.class);
    }

    void loadAll() throws Exception {
        questMap = QuestLoader.loadFolder(KetherPlugin.instance().getKetherConfig().getQuestFolder(),
            this, KetherPlugin.instance().getLogger());
        TLocale.sendToConsole("quest-load", questMap.size());
    }

    @Override
    public QuestRegistry getRegistry() {
        return registry;
    }

    @Override
    public Optional<Quest> getQuest(String id) {
        return Optional.ofNullable(questMap.get(id));
    }

    @Override
    public void startQuest(BukkitQuestContext context) {
        runningQuests.put(context.getPlayerIdentifier(), context);
        context.runActions().thenRunAsync(() -> {
            this.runningQuests.remove(context.getPlayerIdentifier(), context);
            this.getStorage().updateContext(context.getPlayerIdentifier(), context);
        }, this.getExecutor());
    }

    @Override
    public void terminateQuest(BukkitQuestContext context) {
        if (!context.getExitStatus().isPresent()) {
            context.setExitStatus(ExitStatus.paused());
        }
        context.terminate();
        runningQuests.remove(context.getPlayerIdentifier(), context);
        getStorage().updateContext(context.getPlayerIdentifier(), context);
    }

    @Override
    public Multimap<String, BukkitQuestContext> getRunningQuests() {
        return runningQuests;
    }

    @Override
    public List<BukkitQuestContext> getRunningQuests(String playerIdentifier) {
        return Collections.unmodifiableList(runningQuests.get(playerIdentifier));
    }

    @Override
    public Executor getExecutor() {
        return syncExecutor;
    }

    @Override
    public ScheduledExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    @Override
    public String getLocalizedText(String node, Object... params) {
        return TLocale.asString(node, params);
    }

    @Override
    public QuestStorage getStorage() {
        return KetherPlugin.instance().getStorage();
    }
}
