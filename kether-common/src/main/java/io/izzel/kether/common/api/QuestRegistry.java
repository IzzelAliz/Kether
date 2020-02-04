package io.izzel.kether.common.api;

public interface QuestRegistry {

    void registerAction(String id, QuestActionParser parser);

    <T> void registerPersistentDataType(Class<T> clazz, KetherSerializer<T> serializer);
}
