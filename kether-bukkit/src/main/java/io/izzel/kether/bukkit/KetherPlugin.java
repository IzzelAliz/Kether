package io.izzel.kether.bukkit;

import io.izzel.kether.bukkit.config.KetherBukkitConfig;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.api.QuestStorage;
import io.izzel.taboolib.loader.Plugin;
import io.izzel.taboolib.module.dependency.Dependency;
import io.izzel.taboolib.module.locale.TLocale;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

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
    public void onActivated() {
        saveDefaultConfig();
        ketherConfig = new KetherBukkitConfig(getConfig());
        storage = ketherConfig.setupStorage(this, KetherBukkitQuestService.instance());
        try {
            storage.init();
        } catch (Exception e) {
            TLocale.sendToConsole("storage-init", e);
        }
        try {
            KetherBukkitQuestService.instance().loadAll();
        } catch (Exception e) {
            TLocale.sendToConsole("load-error.unknown-error", e);
        }
    }

    @Override
    public void onStopping() {
        try {
            TLocale.sendToConsole("waiting-save");
            KetherBukkitQuestService service = KetherBukkitQuestService.instance();
            for (BukkitQuestContext context : service.getRunningQuests().values()) {
                service.terminateQuest(context);
            }
            storage.close();
            KetherBukkitQuestService.instance().getAsyncExecutor().awaitTermination(30 , TimeUnit.SECONDS);
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
