package io.izzel.kether.bukkit.actions;

import io.izzel.kether.common.api.QuestRegistry;
import io.izzel.kether.common.api.QuestService;

public class KetherBukkitTypes {

    public static void registerTypes(QuestService<?> service) {
        QuestRegistry registry = service.getRegistry();
        registry.registerAction("timeout", TimeoutAction.parser());
    }
}
