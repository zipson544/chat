package pl.chatplus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Ustawienia graczy (data.yml). Domyslnie wszystko wlaczone. */
public class DataManager {

    private final ChatPlus plugin;
    private final File file;
    private final Map<String, Boolean> cache = new ConcurrentHashMap<>();

    public DataManager(ChatPlus plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    public void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection players = y.getConfigurationSection("players");
        if (players == null) {
            return;
        }
        for (String uuid : players.getKeys(false)) {
            ConfigurationSection s = players.getConfigurationSection(uuid);
            if (s == null) {
                continue;
            }
            for (Setting setting : Setting.values()) {
                if (s.contains(setting.key)) {
                    cache.put(uuid + ":" + setting.key, s.getBoolean(setting.key));
                }
            }
        }
    }

    public synchronized void save() {
        YamlConfiguration y = new YamlConfiguration();
        for (Map.Entry<String, Boolean> e : cache.entrySet()) {
            String[] parts = e.getKey().split(":", 2);
            y.set("players." + parts[0] + "." + parts[1], e.getValue());
        }
        try {
            plugin.getDataFolder().mkdirs();
            y.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("Nie udało się zapisać data.yml: " + ex.getMessage());
        }
    }

    public boolean get(UUID id, Setting s) {
        return cache.getOrDefault(id + ":" + s.key, true);
    }

    public boolean toggle(UUID id, Setting s) {
        boolean now = !get(id, s);
        cache.put(id + ":" + s.key, now);
        save();
        return now;
    }
}
