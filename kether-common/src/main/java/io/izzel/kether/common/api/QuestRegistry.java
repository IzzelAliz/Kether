package io.izzel.kether.common.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface QuestRegistry {

    void registerAction(String id, QuestActionParser parser);

    <T> void registerPersistentDataType(String id, Class<T> clazz, KetherSerializer<T> serializer);

    Collection<String> getRegisteredActions();

    Optional<QuestActionParser> getParser(String id);

    Map<String, KetherSerializer<?>> getIdSerializers();

    Optional<Class<?>> getSerializedClass(String id);

    <T> Optional<KetherSerializer<T>> getPersistentDataSerializer(Class<T> cl);

    <T> Optional<KetherSerializer<T>> getPersistentDataSerializer(String id);
}
