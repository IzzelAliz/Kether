package io.izzel.kether.bukkit.util;

import org.bukkit.Bukkit;

public class Closables {

    public static AutoCloseable bukkitTask(int taskId) {
        return () -> Bukkit.getScheduler().cancelTask(taskId);
    }
}
