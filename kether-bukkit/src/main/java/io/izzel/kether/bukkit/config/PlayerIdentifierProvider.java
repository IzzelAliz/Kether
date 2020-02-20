package io.izzel.kether.bukkit.config;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface PlayerIdentifierProvider {

    String provide(Player player);

    Optional<Player> get(String identifier);

    static PlayerIdentifierProvider uuid() {
        return new PlayerIdentifierProvider() {
            @Override
            public String provide(Player player) {
                return player.getUniqueId().toString();
            }

            @Override
            public Optional<Player> get(String identifier) {
                return Optional.ofNullable(Bukkit.getPlayer(UUID.fromString(identifier)));
            }

            @Override
            public String toString() {
                return "uuid";
            }
        };
    }

    static PlayerIdentifierProvider name() {
        return new PlayerIdentifierProvider() {
            @Override
            public String provide(Player player) {
                return player.getName();
            }

            @Override
            public Optional<Player> get(String identifier) {
                return Optional.ofNullable(Bukkit.getPlayer(identifier));
            }

            @Override
            public String toString() {
                return "name";
            }
        };
    }
}
