package io.izzel.kether.common.api.persistent;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface KetherSerializer<T> {

    Map<String, Object> serialize(T instance);

    Optional<T> deserialize(Map<String, Object> map);

    static <T> KetherSerializer<T> gson(Class<T> cl) {
        return new KetherGsonSerializer<>(cl);
    }

    static <T> KetherSerializer<T> of(
        Function<T, Map<String, Object>> serializer,
        Function<Map<String, Object>, T> deserializer
    ) {
        return new KetherSerializer<T>() {
            @Override
            public Map<String, Object> serialize(T instance) {
                return serializer.apply(instance);
            }

            @Override
            public Optional<T> deserialize(Map<String, Object> map) {
                return Optional.ofNullable(deserializer.apply(map));
            }
        };
    }

}
