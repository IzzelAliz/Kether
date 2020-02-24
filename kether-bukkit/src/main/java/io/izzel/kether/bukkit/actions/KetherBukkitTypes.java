package io.izzel.kether.bukkit.actions;

import io.izzel.kether.bukkit.actions.objectives.BlockObjective;
import io.izzel.kether.common.api.QuestRegistry;
import io.izzel.kether.common.api.QuestService;
import org.bukkit.event.block.Action;

public class KetherBukkitTypes {

    public static void registerTypes(QuestService<?> service) {
        QuestRegistry registry = service.getRegistry();
        registry.registerAction("timeout", TimeoutAction.parser());
        registry.registerAction("command", CommandAction.parser());
        registry.registerAction("message", MessageAction.parser());

        registry.registerAction("block_break", BlockObjective.blockBreak());
        registry.registerAction("block_place", BlockObjective.blockPlace());
        registry.registerAction("block_interact", BlockObjective.blockInteract(null));
        registry.registerAction("block_interact_left", BlockObjective.blockInteract(Action.LEFT_CLICK_BLOCK));
        registry.registerAction("block_interact_right", BlockObjective.blockInteract(Action.RIGHT_CLICK_BLOCK));
    }
}
