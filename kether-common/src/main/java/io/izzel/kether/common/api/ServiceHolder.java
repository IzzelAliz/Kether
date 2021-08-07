package io.izzel.kether.common.api;

public class ServiceHolder {

    private static QuestService<?> questServiceInstance;

    public static void setQuestServiceInstance(QuestService<?> service) {
        ServiceHolder.questServiceInstance = service;
    }

    public static QuestService<?> getQuestServiceInstance() {
        return questServiceInstance;
    }
}