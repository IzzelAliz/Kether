package io.izzel.kether.bukkit.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.izzel.kether.bukkit.KetherPlugin;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.api.QuestStorage;
import io.izzel.kether.common.api.storage.LocalYamlStorage;
import io.izzel.kether.common.api.storage.MariaDbStorage;
import io.izzel.taboolib.module.locale.TLocale;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;

public class KetherBukkitConfig {

    private final FileConfiguration configuration;

    private PlayerIdentifierProvider identifierProvider;
    private Path questFolder;

    public KetherBukkitConfig(FileConfiguration configuration) {
        this.configuration = configuration;
        this.setup();
    }

    private void setup() {
        String identifier = configuration.getString("player_identifier", "uuid");
        if (identifier.equalsIgnoreCase("uuid")) {
            identifierProvider = PlayerIdentifierProvider.uuid();
        } else if (identifier.equalsIgnoreCase("name")) {
            identifierProvider = PlayerIdentifierProvider.name();
        } else {
            identifierProvider = PlayerIdentifierProvider.uuid();
        }
        KetherPlugin.instance().getLogger().info(TLocale.asString("config.identifier_provider", identifierProvider.toString()));
        questFolder = KetherPlugin.instance().getDataFolder().toPath()
            .resolve(configuration.getString("quest.folder", "quests"));
    }

    public QuestStorage setupStorage(Plugin plugin, QuestService<?> service) {
        ConfigurationSection section = configuration.getConfigurationSection("storage");
        try {
            String type = section.getString("type");
            if (type.equals("local_yaml")) {
                Path baseDir = plugin.getDataFolder().toPath().resolve(section.getString("folder", "."));
                return new LocalYamlStorage(service, baseDir);
            } else if (type.equals("mariadb")) {
                String jdbcUrl = section.getString("jdbcUrl");
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(jdbcUrl);
                config.setDriverClassName("com.mysql.jdbc.Driver");
                return new MariaDbStorage(service, () -> new HikariDataSource(config));
            } else {
                section.set("type", "local_yaml");
                return new LocalYamlStorage(service, plugin.getDataFolder().toPath().resolve("data"));
            }
        } finally {
            plugin.getLogger().info(TLocale.asString("config.storage", section.getString("type")));
        }
    }

    public PlayerIdentifierProvider getIdentifierProvider() {
        return identifierProvider;
    }

    public Path getQuestFolder() {
        return questFolder;
    }
}
