package pl.chatplus;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatPlus extends JavaPlugin {

    private DataManager data;
    private GiftManager gifts;
    private Menus menus;
    private BukkitTask autoTask;
    private int autoIndex = 0;

    /** gracze, ktorzy wpisuja na czacie nick odbiorcy gifta (uuid -> do kiedy) */
    private final Map<UUID, Long> giftPrompts = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        data = new DataManager(this);
        data.load();
        gifts = new GiftManager(this);
        gifts.load();
        menus = new Menus(this);

        MsgCommand msgCommand = new MsgCommand(this);
        register("msg", msgCommand, msgCommand);
        register("r", msgCommand, msgCommand);

        SettingsCommand settings = new SettingsCommand(this);
        register("ustawienia", settings, null);

        GiftCommand gift = new GiftCommand(this);
        register("gift", gift, gift);

        AdminCommand admin = new AdminCommand(this);
        register("chatplus", admin, admin);

        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);

        startAutoMessages();
        Bukkit.getScheduler().runTaskTimer(this, () -> gifts.tick(), 1200L, 1200L);
    }

    @Override
    public void onDisable() {
        if (data != null) {
            data.save();
        }
        if (gifts != null) {
            gifts.save();
        }
    }

    private void register(String name, org.bukkit.command.CommandExecutor ex, org.bukkit.command.TabCompleter tc) {
        PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            return;
        }
        cmd.setExecutor(ex);
        if (tc != null) {
            cmd.setTabCompleter(tc);
        }
    }

    public void reload() {
        reloadConfig();
        startAutoMessages();
    }

    private void startAutoMessages() {
        if (autoTask != null) {
            autoTask.cancel();
            autoTask = null;
        }
        if (!getConfig().getBoolean("automatyczne-wiadomosci.wlaczone", true)) {
            return;
        }
        long period = Math.max(10L, getConfig().getLong("automatyczne-wiadomosci.interwal-sekundy", 300)) * 20L;
        autoTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            List<String> list = getConfig().getStringList("automatyczne-wiadomosci.lista");
            if (list.isEmpty()) {
                return;
            }
            if (autoIndex >= list.size()) {
                autoIndex = 0;
            }
            String prefix = getConfig().getString("automatyczne-wiadomosci.prefix", "");
            Component c = Util.color(prefix + list.get(autoIndex));
            autoIndex++;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (data.get(p.getUniqueId(), Setting.AUTO)) {
                    p.sendMessage(c);
                }
            }
        }, period, period);
    }

    // ---------------------------------------------------------------- helpery

    public String raw(String key) {
        return getConfig().getString("wiadomosci." + key, "&cBrak wiadomości: " + key);
    }

    public Component msg(String key, String... replacements) {
        String s = raw(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            s = s.replace(replacements[i], replacements[i + 1]);
        }
        return Util.color(s);
    }

    public DataManager data() {
        return data;
    }

    public GiftManager gifts() {
        return gifts;
    }

    public Menus menus() {
        return menus;
    }

    public Map<UUID, Long> giftPrompts() {
        return giftPrompts;
    }
}
