package io.izzel.kether.bukkit.actions.objectives;

import io.izzel.kether.bukkit.BukkitQuestContext;
import io.izzel.kether.bukkit.util.Closables;
import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.concurrent.CompletableFuture;

public abstract class BlockObjective extends QuestAction<Void> {

    public static QuestActionParser blockBreak() {
        return QuestActionParser.<Void, BukkitQuestContext>of(
            resolver -> new BlockBreak(Material.matchMaterial(resolver.nextToken())),
            KetherCompleters.enumValue(Material.class)
        );
    }

    public static QuestActionParser blockPlace() {
        return QuestActionParser.<Void, BukkitQuestContext>of(
            resolver -> new BlockPlace(Material.matchMaterial(resolver.nextToken())),
            KetherCompleters.enumValue(Material.class)
        );
    }

    public static QuestActionParser blockInteract(Action action) {
        return QuestActionParser.<Void, BukkitQuestContext>of(
            resolver -> new BlockInteract(Material.matchMaterial(resolver.nextToken()), action),
            KetherCompleters.enumValue(Material.class)
        );
    }

    protected final Material material;

    protected BlockObjective(Material material) {
        this.material = material;
    }

    public static class BlockInteract extends BlockObjective {

        private final Action action;

        protected BlockInteract(Material material, Action action) {
            super(material);
            this.action = action;
        }

        @Override
        public CompletableFuture<Void> process(BukkitQuestContext context) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            Player player = context.getPlayer();
            context.addClosable(Closables.listening(
                PlayerInteractEvent.class,
                event -> event.getPlayer().equals(player)
                    && event.getClickedBlock().getType().equals(material)
                    && (action == null || event.getAction() == action),
                event -> future.complete(null)
            ));
            return future;
        }

        @Override
        public String toString() {
            return "BlockInteract{" +
                "material=" + material +
                ", action=" + action +
                '}';
        }
    }

    public static class BlockPlace extends BlockObjective {

        protected BlockPlace(Material material) {
            super(material);
        }

        @Override
        public CompletableFuture<Void> process(BukkitQuestContext context) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            Player player = context.getPlayer();
            context.addClosable(Closables.listening(
                BlockPlaceEvent.class,
                event -> event.getPlayer().equals(player)
                    && event.getBlock().getType().equals(material),
                event -> future.complete(null)
            ));
            return future;
        }

        @Override
        public String toString() {
            return "BlockPlace{" +
                "material=" + material +
                '}';
        }
    }

    public static class BlockBreak extends BlockObjective {

        protected BlockBreak(Material material) {
            super(material);
        }

        @Override
        public CompletableFuture<Void> process(BukkitQuestContext context) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            Player player = context.getPlayer();
            context.addClosable(Closables.listening(
                BlockBreakEvent.class,
                event -> event.getPlayer().equals(player)
                    && event.getBlock().getType().equals(material),
                event -> future.complete(null)
            ));
            return future;
        }

        @Override
        public String toString() {
            return "BlockBreak{" +
                "material=" + material +
                '}';
        }
    }
}
