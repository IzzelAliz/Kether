package io.izzel.kether.bukkit;

import org.bukkit.Bukkit;

import java.util.concurrent.Executor;

public class BukkitSchedulerExecutor implements Executor {

    public static final BukkitSchedulerExecutor INSTANCE = new BukkitSchedulerExecutor();

    @Override
    public void execute(Runnable command) {
        if (Bukkit.isPrimaryThread()) {
            command.run();
        } else {
            Bukkit.getScheduler().runTask(KetherPlugin.getPlugin(), command);
        }
    }

}
