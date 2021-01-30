package io.izzel.kether.common.api;

import io.izzel.kether.common.api.persistent.KetherSerializer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class DefaultRegistry implements QuestRegistry {

    private final Map<String, Map<String, QuestActionParser>> parsers = new HashMap<>();
    private final Map<String, KetherSerializer<?>> serializersById = new HashMap<>();
    private final Map<Class<?>, KetherSerializer<?>> serializersByClass = new HashMap<>();
    private final Map<String, Class<?>> idToClass = new HashMap<>();
    private final Map<String, BiFunction<QuestContext.Frame, String, String>> processors = new HashMap<>();

    @Override
    public void registerAction(String namespace, String id, QuestActionParser parser) {
        parsers.computeIfAbsent(namespace, i -> new HashMap<>()).put(id, parser);
    }

    @Override
    public void registerAction(String id, QuestActionParser parser) {
        registerAction("kether", id, parser);
    }

    @Override
    public <T> void registerPersistentDataType(String id, Class<T> clazz, KetherSerializer<T> serializer) {
        serializersById.put(id, serializer);
        serializersByClass.put(clazz, serializer);
        idToClass.put(id, clazz);
    }

    @Override
    public void registerContextStringProcessor(String id, BiFunction<QuestContext.Frame, String, String> processor) {
        processors.put(id, processor);
    }

    @Override
    public Collection<String> getRegisteredActions(String namespace) {
        Map<String, QuestActionParser> map = parsers.get(namespace);
        return map == null ? Collections.emptyList() : Collections.unmodifiableCollection(map.keySet());
    }

    @Override
    public Collection<String> getRegisteredActions() {
        return getRegisteredActions("kether");
    }

    @Override
    public Optional<QuestActionParser> getParser(String namespace, String id) {
        Map<String, QuestActionParser> map = parsers.get(namespace);
        return map == null ? Optional.empty() : Optional.ofNullable(map.get(id));
    }

    @Override
    public Optional<QuestActionParser> getParser(String id) {
        return getParser("kether", id);
    }

    @Override
    public Map<String, KetherSerializer<?>> getIdSerializers() {
        return Collections.unmodifiableMap(serializersById);
    }

    @Override
    public Optional<Class<?>> getSerializedClass(String id) {
        return Optional.ofNullable(idToClass.get(id));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<KetherSerializer<T>> getPersistentDataSerializer(Class<T> cl) {
        return Optional.ofNullable((KetherSerializer<T>) serializersByClass.get(cl));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<KetherSerializer<T>> getPersistentDataSerializer(String id) {
        return Optional.ofNullable((KetherSerializer<T>) serializersById.get(id));
    }

    @Override
    public Optional<BiFunction<QuestContext.Frame, String, String>> getContextStringProcessor(String id) {
        return Optional.ofNullable(processors.get(id));
    }
}
