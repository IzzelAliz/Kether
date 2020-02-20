package io.izzel.kether.bukkit;

import io.izzel.kether.common.api.ExitStatus;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

public class KetherListeners implements Listener {

    private final QuestService<?> service;

    public KetherListeners(QuestService<?> service) {
        this.service = service;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String identifier = KetherPlugin.instance().getKetherConfig().getIdentifierProvider().provide(event.getPlayer());
        service.getStorage().fetchContexts(identifier).thenAcceptAsync(contexts -> {
            for (QuestContext context : contexts) {
                Optional<ExitStatus> optional = context.getExitStatus();
                if (optional.isPresent()) {
                    ExitStatus exitStatus = optional.get();
                    if (exitStatus.isRunning()) {

                    }
                }
            }
        }, service.getExecutor());
    }
}
