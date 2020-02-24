package io.izzel.kether.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Closables {

    public static AutoCloseable bukkitTask(int taskId) {
        return () -> Bukkit.getScheduler().cancelTask(taskId);
    }

    public static <T extends Event> AutoCloseable listening(Class<T> clazz, Predicate<T> predicate, Consumer<T> consumer) {
        OnetimeListener<T> listener = OnetimeListener.set(clazz, predicate, consumer);
        return () -> HandlerList.unregisterAll(listener);
    }
}
