package io.izzel.kether.common.persistent;

import io.izzel.kether.common.api.QuestRegistry;
import io.izzel.kether.common.persistent.serializer.KetherSerializer;

import java.util.Map;
import java.util.Optional;

public interface PersistentRegistry extends QuestRegistry {

    <T> void registerPersistentDataType(String id, Class<T> clazz, KetherSerializer<T> serializer);

    Map<String, KetherSerializer<?>> getIdSerializers();

    Optional<Class<?>> getSerializedClass(String id);

    <T> Optional<KetherSerializer<T>> getPersistentDataSerializer(Class<T> cl);

    <T> Optional<KetherSerializer<T>> getPersistentDataSerializer(String id);
}
