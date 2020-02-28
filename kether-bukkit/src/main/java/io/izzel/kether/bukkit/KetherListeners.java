package io.izzel.kether.bukkit;

import com.google.common.collect.ImmutableList;
import io.izzel.kether.common.api.ExitStatus;
import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.util.Coerce;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class KetherListeners implements Listener {

    private final KetherBukkitQuestService service;

    public KetherListeners(KetherBukkitQuestService service) {
        this.service = service;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String identifier = KetherPlugin.instance().getKetherConfig().getIdentifierProvider().provide(event.getPlayer());
        service.getStorage().fetchContexts(identifier)
            .thenApplyAsync(t -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return t;
            })
            .thenAcceptAsync(contexts -> {
                Set<String> set = new HashSet<>();
                for (QuestContext context : contexts) {
                    set.add(context.getQuest().getId());
                    Optional<ExitStatus> optional = context.getExitStatus();
                    if (optional.isPresent()) {
                        ExitStatus exitStatus = optional.get();
                        if (exitStatus.isRunning() && context instanceof BukkitQuestContext) {
                            context.setExitStatus(null);
                            service.startQuest((BukkitQuestContext) context);
                        }
                    }
                }
                for (Map.Entry<String, Quest> entry : service.getQuests().entrySet()) {
                    String id = entry.getKey();
                    Quest quest = entry.getValue();
                    if (!set.contains(id)) {
                        Map<String, Object> settings = service.getQuestSettings(id);
                        boolean autoStart = Coerce.toBoolean(settings.get("autostart"));
                        if (autoStart) {
                            BukkitQuestContext context = BukkitQuestContext.createClean(event.getPlayer(), quest);
                            service.startQuest(context);
                        }
                    }
                }
            }, service.getExecutor());
    }

    @EventHandler
    public void onExit(PlayerQuitEvent event) {
        String id = KetherPlugin.instance().getKetherConfig().getIdentifierProvider().provide(event.getPlayer());
        for (BukkitQuestContext context : ImmutableList.copyOf(service.getRunningQuests(id))) {
            service.terminateQuest(context);
        }
    }
}
