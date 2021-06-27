package io.izzel.kether.common.api;

import java.util.ServiceLoader;

public class ServiceHolder {

    static final QuestService<?> INSTANCE = ServiceLoader.load(QuestService.class, ServiceHolder.class.getClassLoader()).iterator().next();
}