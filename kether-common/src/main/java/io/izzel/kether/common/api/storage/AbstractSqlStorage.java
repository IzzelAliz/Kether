package io.izzel.kether.common.api.storage;

import com.google.common.collect.ImmutableList;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractSqlStorage extends AbstractStorage {

    private final Supplier<DataSource> dataSourceSupplier;

    private DataSource dataSource;

    public AbstractSqlStorage(QuestService<?> service, Supplier<DataSource> dataSourceSupplier) {
        super(service);
        this.dataSourceSupplier = dataSourceSupplier;
    }

    @Override
    public void init() throws Exception {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        options.setAllowUnicode(true);
        KetherRepresenter representer = new KetherRepresenter(service);
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        this.yaml = new Yaml(new KetherConstructor(service), representer, options);
        this.dataSource = dataSourceSupplier.get();
    }

    @Override
    public void close() throws Exception {
    }

    protected abstract Map.Entry<String, Consumer<PreparedStatement>> fetchContextsStatement(String playerIdentifier);

    protected abstract List<Map.Entry<String, Consumer<PreparedStatement>>> updateContextStatement(String playerIdentifier, String questId, String str);

    @Override
    public CompletableFuture<Void> updateContext(String playerIdentifier, QuestContext context) {
        String questId = context.getQuest().getId();
        return CompletableFuture.supplyAsync(
            () -> {
                this.map.computeIfAbsent(playerIdentifier, k -> new HashMap<>()).put(questId, context);
                return this.yaml.dump(context);
            },
            service.getExecutor()
        ).thenAcceptAsync(
            str -> {
                if (str == null) return;
                try {
                    List<Map.Entry<String, Consumer<PreparedStatement>>> entries = updateContextStatement(playerIdentifier, questId, str);
                    try (Connection connection = dataSource.getConnection()) {
                        for (Map.Entry<String, Consumer<PreparedStatement>> entry : entries) {
                            try (PreparedStatement statement = connection.prepareStatement(entry.getKey())) {
                                entry.getValue().accept(statement);
                                statement.executeUpdate();
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            },
            service.getAsyncExecutor()
        );
    }

    @Override
    protected Collection<String> supplyAll(String playerIdentifier) {
        try {
            Map.Entry<String, Consumer<PreparedStatement>> statement = fetchContextsStatement(playerIdentifier);
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(statement.getKey())) {
                statement.getValue().accept(preparedStatement);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    ImmutableList.Builder<String> builder = ImmutableList.builder();
                    while (resultSet.next()) {
                        builder.add(resultSet.getString("context"));
                    }
                    return builder.build();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ImmutableList.of();
        }
    }
}
