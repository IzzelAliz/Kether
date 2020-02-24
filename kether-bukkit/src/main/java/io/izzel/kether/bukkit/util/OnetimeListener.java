package io.izzel.kether.bukkit.util;

import io.izzel.kether.bukkit.KetherPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class OnetimeListener<T extends Event> implements Listener, EventExecutor {

    private final Class<T> clazz;
    private final Predicate<T> predicate;
    private final Consumer<T> consumer;

    public OnetimeListener(Class<T> clazz, Predicate<T> predicate, Consumer<T> consumer) {
        this.clazz = clazz;
        this.predicate = predicate;
        this.consumer = consumer;
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        try {
            T cast = clazz.cast(event);
            if (predicate.test(cast)) {
                consumer.accept(cast);
            }
        } catch (Exception e) {
            throw new EventException(e);
        }
    }

    public static <T extends Event> OnetimeListener<T> set(Class<T> clazz, Predicate<T> predicate, Consumer<T> consumer) {
        OnetimeListener<T> listener = new OnetimeListener<>(clazz, predicate, consumer);
        Bukkit.getPluginManager().registerEvent(clazz, listener, EventPriority.NORMAL, listener, KetherPlugin.instance());
        return listener;
    }
}
