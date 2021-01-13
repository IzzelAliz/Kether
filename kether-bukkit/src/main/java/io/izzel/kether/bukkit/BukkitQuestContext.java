package io.izzel.kether.bukkit;

import io.izzel.kether.common.api.AbstractQuestContext;
import io.izzel.kether.common.api.data.ExitStatus;
import io.izzel.kether.common.api.KetherSerializer;
import io.izzel.kether.common.api.persistent.PersistentQuestContext;
import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

public class BukkitQuestContext extends PersistentQuestContext {

    private Player player;

    protected BukkitQuestContext(QuestService<?> service, AbstractQuestContext parent, Quest quest, String playerIdentifier, String runningBlock, int index, String dataKey, Map<String, Object> tempData, Map<String, Object> persistentData, String childKey, boolean anonymous) {
        super(service, parent, quest, playerIdentifier, runningBlock, index, dataKey, tempData, persistentData, childKey, anonymous);
    }

    public Player getPlayer() {
        if (player == null) {
            player = KetherPlugin.instance().getKetherConfig().getIdentifierProvider()
                .get(this.getPlayerIdentifier()).orElseThrow(IllegalArgumentException::new);
        }
        return player;
    }

    @Override
    protected Executor createExecutor() {
        return BukkitSchedulerExecutor.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends QuestContext> C createChild(String key, boolean anonymous) {
        BukkitQuestContext context = new BukkitQuestContext(getService(), this, getQuest(), getPlayerIdentifier(), getBlockRunning(), getIndex(), QuestContext.BASE_DATA_KEY, null, getPersistentData(), key, anonymous);
        this.children.addLast(context);
        return (C) context;
    }

    public static BukkitQuestContext createClean(Player player, Quest quest) {
        return new BukkitQuestContext(
            KetherBukkitQuestService.instance(),
            null,
            quest,
            KetherPlugin.instance().getKetherConfig().getIdentifierProvider().provide(player),
            QuestContext.BASE_BLOCK,
            0,
            QuestContext.BASE_DATA_KEY,
            null,
            new HashMap<>(),
            null,
            false
        );
    }

    public static class Serializer implements KetherSerializer<BukkitQuestContext> {

        @Override
        public Map<String, Object> serialize(BukkitQuestContext instance) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("tmp", instance.getTempData());
            map.put("persist", instance.getPersistentData());
            map.put("exit", instance.getExitStatus().orElse(null));
            map.put("quest", instance.getQuest().getId());
            map.put("identifier", instance.getPlayerIdentifier());
            return map;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Optional<BukkitQuestContext> deserialize(Map<String, Object> map) {
            Object tmp = map.get("tmp");
            Object persist = map.get("persist");
            Object exit = map.get("exit");
            Object quest = map.get("quest");
            Object identifier = map.get("identifier");
            try {
                Quest theQuest = KetherBukkitQuestService.instance().getQuest(quest.toString()).orElseThrow(NullPointerException::new);
                String playerIdentifier = identifier.toString();
                ExitStatus exitStatus = ((ExitStatus) exit);
                BukkitQuestContext context = new BukkitQuestContext(
                    KetherBukkitQuestService.instance(),
                    null,
                    theQuest,
                    playerIdentifier,
                    QuestContext.BASE_BLOCK,
                    0,
                    QuestContext.BASE_DATA_KEY,
                    ((Map<String, Object>) tmp),
                    Optional.ofNullable((Map<String, Object>) persist).orElse(new HashMap<>()),
                    null,
                    false
                );
                context.exitStatus = exitStatus;
                return Optional.of(context);
            } catch (Throwable t) {
                return Optional.empty();
            }
        }
    }
}
