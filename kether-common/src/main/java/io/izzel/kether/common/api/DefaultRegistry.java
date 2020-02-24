package io.izzel.kether.common.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultRegistry implements QuestRegistry {

    private final Map<String, QuestActionParser> parsers = new HashMap<>();
    private final Map<String, KetherSerializer<?>> serializersById = new HashMap<>();
    private final Map<Class<?>, KetherSerializer<?>> serializersByClass = new HashMap<>();
    private final Map<String, Class<?>> idToClass = new HashMap<>();

    @Override
    public void registerAction(String id, QuestActionParser parser) {
        parsers.put(id, parser);
    }

    @Override
    public <T> void registerPersistentDataType(String id, Class<T> clazz, KetherSerializer<T> serializer) {
        serializersById.put(id, serializer);
        serializersByClass.put(clazz, serializer);
        idToClass.put(id, clazz);
    }

    @Override
    public Collection<String> getRegisteredActions() {
        return Collections.unmodifiableCollection(parsers.keySet());
    }

    @Override
    public Optional<QuestActionParser> getParser(String id) {
        return Optional.ofNullable(parsers.get(id));
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
}