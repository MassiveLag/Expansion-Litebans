package nl.nopermission.litebansplaceholders;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import litebans.api.Database;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.getServer;

public class LiteBansExpansion extends PlaceholderExpansion implements Cacheable, Configurable {

    private Cache<String, Object> cache;
    private final Database database = Database.get();
    @Override
    public @NotNull String getIdentifier() {
        return "litebans";
    }

    @Override
    public @NotNull String getAuthor() {
        return "NoPermission";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.1";
    }

    public final List<String> types = new ArrayList<>();

    @Override
    public boolean canRegister() {
        int refresh = 25;
        Optional<ConfigurationSection> configSection = Optional.ofNullable(getConfigSection());
        if (configSection.isPresent())
            refresh = getConfigSection().getInt("cacheRefresh");

        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(refresh, TimeUnit.SECONDS)
                .build();

        types.add("bans");
        types.add("mutes");
        types.add("warnings");
        types.add("kicks");

        return hasPlugin("LiteBans");
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean register() {
        return super.register();
    }

    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if (player == null || !player.isOnline()) {
            return "Error: Player not found!";
        }

        if (identifier.startsWith("stats_")) {
            boolean own = identifier.endsWith("own");
            UUID uniqueId = player.getUniqueId();
            String keyToSave = own ? player.getUniqueId() + "-" + identifier : identifier;
            String[] split = identifier.split("_");
            String type = split[1].toLowerCase();

            if (identifier.equalsIgnoreCase("stats_total" + (own ? "_own" : ""))) {
                Optional<Object> cacheObject = fromCache(keyToSave);
                if (cacheObject.isPresent())
                    return "" + getAsLong(cacheObject.get());
                else {
                    getPlaceholderAPI().getServer().getScheduler().runTaskAsynchronously(getPlaceholderAPI(), () -> {
                        String query = "SELECT SUM(total.count) FROM (SELECT COUNT(*) count FROM {bans} UNION ALL SELECT COUNT( * ) AS count FROM {mutes} UNION ALL SELECT COUNT( * ) AS count FROM {kicks} UNION ALL SELECT COUNT( * ) AS count FROM {warnings}) total;";
                        try (PreparedStatement st = database.prepareStatement(query);
                             ResultSet rs = st.executeQuery()) {
                            if (rs.next()) {
                                cache(keyToSave, rs.getLong(1));
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    return "0";
                }
            }

            if (types.stream().filter(s -> s.equalsIgnoreCase(type)).findAny().isEmpty())
                return "Type is not correct!";

            if (identifier.equalsIgnoreCase("stats_" + type + (own ? "_own" : ""))) {
                Optional<Object> cacheObject = fromCache(keyToSave);
                if (cacheObject.isPresent())
                    return "" + getAsLong(cacheObject.get());
                else {
                        getPlaceholderAPI().getServer().getScheduler().runTaskAsynchronously(getPlaceholderAPI(), () -> {
                            String query = "SELECT COUNT(*) FROM {"+ type +"}" + (own ? " WHERE uuid=?" : "") + ";";
                            try (PreparedStatement st = database.prepareStatement(query)) {
                                if (own)
                                    st.setString(1, uniqueId.toString());
                                try (ResultSet rs = st.executeQuery()) {
                                    if (rs.next())
                                        cache(keyToSave, rs.getLong(1));
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                    });
                    return "0";
                }
            }

            if (identifier.equalsIgnoreCase("stats_" + type + "_active" + (own ? "_own" : ""))) {
                Optional<Object> cacheObject = fromCache(keyToSave);
                if (cacheObject.isPresent())
                    return "" + getAsLong(cacheObject.get());
                else {
                    getPlaceholderAPI().getServer().getScheduler().runTaskAsynchronously(getPlaceholderAPI(), () -> {
                        String query = "SELECT COUNT(*) FROM {" + type + "} WHERE active=1" + (own ? " WHERE uuid=?" : "") + ";";
                        try (PreparedStatement st = database.prepareStatement(query)) {
                            if (own)
                                st.setString(1, uniqueId.toString());
                            try (ResultSet rs = st.executeQuery()) {
                                if (rs.next()) {
                                    cache(keyToSave, rs.getLong(1));
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    return "0";
                }
            }
            if (identifier.equalsIgnoreCase("stats_" + type + "_inactive" + (own ? "_own" : ""))) {
                Optional<Object> cacheObject = fromCache(keyToSave);
                if (cacheObject.isPresent())
                    return "" + getAsLong(cacheObject.get());
                else {
                    getPlaceholderAPI().getServer().getScheduler().runTaskAsynchronously(getPlaceholderAPI(), () -> {
                        String query = "SELECT COUNT(*) FROM {" + type + "} WHERE active=0" + (own ? " WHERE uuid=?" : "") + ";";
                        try (PreparedStatement st = database.prepareStatement(query)) {
                            if (own)
                                st.setString(1, uniqueId.toString());
                            try (ResultSet rs = st.executeQuery()) {
                                if (rs.next()) {
                                   cache(keyToSave, rs.getLong(1));
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    return "0";
                }
            }
        }

        if (identifier.equalsIgnoreCase("test"))
            getPlaceholderAPI().getServer().getScheduler().runTaskAsynchronously(getPlaceholderAPI(), () -> {
                String query = "SELECT SUM(total.count) FROM (SELECT COUNT(*) count FROM {bans} UNION ALL SELECT COUNT( * ) AS count FROM {mutes} UNION ALL SELECT COUNT( * ) AS count FROM {kicks} UNION ALL SELECT COUNT( * ) AS count FROM {warnings}) total;";
                try (PreparedStatement st = database.prepareStatement(query);
                     ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        System.out.println(rs.getLong(1));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

        return "ERROR";
    }


    @Override
    public Map<String, Object> getDefaults() {
        return ImmutableMap.<String, Object>builder()
                .put("cacheRefresh", 25).build();
    }

    private boolean hasPlugin(String plugin) {
        return getServer().getPluginManager().getPlugin(plugin) != null;
    }

    @Override
    public void clear() {
        cache.cleanUp();
    }

    private Optional<Object> fromCache(Object key) {
        return Optional.ofNullable(cache.asMap().get(key));
    }


    private void cache(String key, Object object) {
        if (!cache.asMap().containsKey(key))
            cache.put(key, object);
    }

    private boolean isCached(String key) {
        return cache.asMap().containsKey(key);
    }

    private int getAsInteger(Object o) {
        return (int) o;
    }

    private long getAsLong(Object o) {
        return (long) o;
    }
}
