package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;

import java.util.concurrent.CompletableFuture;

final class SetAction {

    private static class ForConstant extends QuestAction<Void> {

        private final String key;
        private final String value;

        public ForConstant(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public CompletableFuture<Void> process(QuestContext.Frame frame) {
            if (value == null || value.equals("null")) {
                frame.variables().set(key, null);
            } else {
                frame.variables().set(key, value);
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public String toString() {
            return "SetAction{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
        }
    }

    private static class ForAction extends QuestAction<Void> {

        private final String key;
        private final ParsedAction<?> action;

        private ForAction(String key, ParsedAction<?> action) {
            this.key = key;
            this.action = action;
        }

        @Override
        public CompletableFuture<Void> process(QuestContext.Frame frame) {
            frame.variables().set(key, action, frame.newFrame(action).run());
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public String toString() {
            return "SetAction{" +
                "key='" + key + '\'' +
                ", action=" + action +
                '}';
        }
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(
            resolver -> {
                String name = resolver.nextToken();
                if (resolver.hasNext() && resolver.peek() != '"') {
                    String token = resolver.nextToken();
                    if (token.equals("to")) {
                        return new ForAction(name, resolver.nextAction());
                    } else {
                        return new ForConstant(name, token);
                    }
                }
                return new ForConstant(name, resolver.nextToken());
            },
            KetherCompleters.seq(
                KetherCompleters.consume(),
                KetherCompleters.firstParsing(
                    KetherCompleters.seq(
                        KetherCompleters.constant("to"),
                        KetherCompleters.action(service)
                    ),
                    KetherCompleters.consume()
                )
            )
        );
    }
}
