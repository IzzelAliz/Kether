package io.izzel.kether.common.api;

import io.izzel.kether.common.api.persistent.KetherSerializer;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public interface QuestRegistry {

    void registerAction(String id, QuestActionParser parser);

    <T> void registerPersistentDataType(String id, Class<T> clazz, KetherSerializer<T> serializer);

    void registerContextStringProcessor(String id, BiFunction<QuestContext.Frame, String, String> processor);

    Collection<String> getRegisteredActions();

    Optional<QuestActionParser> getParser(String id);

    Map<String, KetherSerializer<?>> getIdSerializers();

    Optional<Class<?>> getSerializedClass(String id);

    <T> Optional<KetherSerializer<T>> getPersistentDataSerializer(Class<T> cl);

    <T> Optional<KetherSerializer<T>> getPersistentDataSerializer(String id);

    Optional<BiFunction<QuestContext.Frame, String, String>> getContextStringProcessor(String id);
}
