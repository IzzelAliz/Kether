package io.izzel.kether.bukkit;

import io.izzel.kether.bukkit.config.KetherBukkitConfig;
import io.izzel.kether.common.actions.KetherTypes;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.api.QuestStorage;
import io.izzel.taboolib.loader.Plugin;
import io.izzel.taboolib.module.dependency.Dependency;
import io.izzel.taboolib.module.locale.TLocale;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Dependency(maven = "com.zaxxer:HikariCP:3.1.0")
public class KetherPlugin extends Plugin {

    private KetherBukkitConfig ketherConfig;
    private QuestStorage storage;

    @Override
    public void onLoading() {
        getServer().getServicesManager().register(
            QuestService.class,
            new KetherBukkitQuestService(),
            this,
            ServicePriority.Normal
        );
    }

    @Override
    public void onStarting() {
        KetherTypes.registerInternals(
            KetherBukkitQuestService.instance().getRegistry(),
            KetherBukkitQuestService.instance()
        );
    }

    @Override
    public void onActivated() {
        saveDefaultConfig();
        ketherConfig = new KetherBukkitConfig(getConfig());
        storage = ketherConfig.setupStorage(this, KetherBukkitQuestService.instance());
        try {
            storage.init();
        } catch (Exception e) {
            getLogger().info(TLocale.asString("storage-init", e));
        }
        try {
            KetherBukkitQuestService.instance().loadAll();
        } catch (Exception e) {
            getLogger().info(TLocale.asString("load-error.unknown-error", e));
        }
    }

    @Override
    public void onStopping() {
        try {
            getLogger().info(TLocale.asString("waiting-save"));
            KetherBukkitQuestService service = KetherBukkitQuestService.instance();
            for (BukkitQuestContext context : service.getRunningQuests().values()) {
                service.terminateQuest(context);
            }
            storage.close();
            ScheduledExecutorService asyncExecutor = KetherBukkitQuestService.instance().getAsyncExecutor();
            asyncExecutor.shutdown();
            asyncExecutor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public KetherBukkitConfig getKetherConfig() {
        return ketherConfig;
    }

    public QuestStorage getStorage() {
        return storage;
    }

    public static KetherPlugin instance() {
        return JavaPlugin.getPlugin(KetherPlugin.class);
    }
}
