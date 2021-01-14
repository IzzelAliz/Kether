package io.izzel.kether.common.api.persistent;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.util.Map;
import java.util.Optional;

final class KetherGsonSerializer<T> implements KetherSerializer<T> {

    private static final TypeToken<Map<String, Object>> TOKEN = new TypeToken<Map<String, Object>>() {};

    private final Class<T> cl;
    private final Gson gson = new Gson();

    KetherGsonSerializer(Class<T> cl) {
        this.cl = cl;
    }

    @Override
    public Map<String, Object> serialize(T instance) {
        JsonElement jsonElement = gson.toJsonTree(instance);
        return gson.fromJson(jsonElement, TOKEN.getType());
    }

    @Override
    public Optional<T> deserialize(Map<String, Object> map) {
        JsonElement jsonElement = gson.toJsonTree(map);
        return Optional.ofNullable(gson.fromJson(jsonElement, cl));
    }
}
