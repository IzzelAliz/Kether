package io.izzel.kether.common.api;

import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

public interface QuestService<CTX extends QuestContext> {

    QuestRegistry getRegistry();

    Optional<Quest> getQuest(String id);

    void startQuest(CTX context);

    void terminateQuest(CTX context);

    Multimap<String, CTX> getRunningQuests();

    List<CTX> getRunningQuests(String playerIdentifier);

    Executor getExecutor();

    ScheduledExecutorService getAsyncExecutor();

    String getLocalizedText(String node, Object... params);

    QuestStorage getStorage();
}
