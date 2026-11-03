package pl.chatplus;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GiftManager {

    public enum Status {PENDING, CLAIMED, WITHDRAWN, EXPIRED}

    public static class Gift {
        public UUID id;
        public UUID from;
        public UUID to;
        public String fromName;
        public String toName;
        public ItemStack item;
        public long sentAt;
        public long closedAt;
        public Status status = Status.PENDING;
    }

    private final ChatPlus plugin;
    private final File file;
    private final Map<UUID, Gift> gifts = new LinkedHashMap<>();

    public GiftManager(ChatPlus plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "gifts.yml");
    }

    // ------------------------------------------------------------ zapis/odczyt

    public void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = y.getConfigurationSection("gifts");
        if (sec == null) {
            return;
        }
        for (String key : sec.getKeys(false)) {
            ConfigurationSection s = sec.getConfigurationSection(key);
            if (s == null) {
                continue;
            }
            try {
                Gift g = new Gift();
                g.id = UUID.fromString(key);
                g.from = UUID.fromString(s.getString("from", ""));
                g.to = UUID.fromString(s.getString("to", ""));
                g.fromName = s.getString("fromName", "?");
                g.toName = s.getString("toName", "?");
                g.item = s.getItemStack("item");
                g.sentAt = s.getLong("sentAt");
                g.closedAt = s.getLong("closedAt");
                g.status = Status.valueOf(s.getString("status", "PENDING"));
                gifts.put(g.id, g);
            } catch (Exception ex) {
                plugin.getLogger().warning("Pominięto uszkodzony gift " + key + ": " + ex.getMessage());
            }
        }
    }

    public void save() {
        YamlConfiguration y = new YamlConfiguration();
        for (Gift g : gifts.values()) {
            String p = "gifts." + g.id + ".";
            y.set(p + "from", g.from.toString());
            y.set(p + "to", g.to.toString());
            y.set(p + "fromName", g.fromName);
            y.set(p + "toName", g.toName);
            y.set(p + "item", g.item);
            y.set(p + "sentAt", g.sentAt);
            y.set(p + "closedAt", g.closedAt);
            y.set(p + "status", g.status.name());
        }
        try {
            plugin.getDataFolder().mkdirs();
            y.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("Nie udało się zapisać gifts.yml: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------ zapytania

    public Gift get(UUID id) {
        return gifts.get(id);
    }

    public List<Gift> pending(UUID to) {
        List<Gift> list = new ArrayList<>();
        for (Gift g : gifts.values()) {
            if (g.status == Status.PENDING && g.to.equals(to)) {
                list.add(g);
            }
        }
        list.sort(Comparator.comparingLong(g -> g.sentAt));
        return list;
    }

    public List<Gift> history(UUID player) {
        List<Gift> list = new ArrayList<>();
        for (Gift g : gifts.values()) {
            if (g.from.equals(player) || g.to.equals(player)) {
                list.add(g);
            }
        }
        list.sort((a, b) -> Long.compare(b.sentAt, a.sentAt));
        if (list.size() > 45) {
            return new ArrayList<>(list.subList(0, 45));
        }
        return list;
    }

    // ------------------------------------------------------------ akcje

    /** Wysyla przedmiot z reki gracza do (takze offline) gracza o podanym nicku. */
    public void send(Player from, String targetName) {
        ItemStack hand = from.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) {
            from.sendMessage(plugin.msg("gift-brak-przedmiotu"));
            return;
        }

        OfflinePlayer target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            target = Bukkit.getOfflinePlayerIfCached(targetName);
        }
        if (target == null) {
            from.sendMessage(plugin.msg("gift-brak-gracza", "%player%", targetName));
            return;
        }
        if (target.getUniqueId().equals(from.getUniqueId())) {
            from.sendMessage(plugin.msg("gift-do-siebie"));
            return;
        }
        int max = plugin.getConfig().getInt("gift.maks-oczekujacych-na-gracza", 45);
        if (pending(target.getUniqueId()).size() >= max) {
            from.sendMessage(plugin.msg("gift-limit"));
            return;
        }

        String tName = target.getName() != null ? target.getName() : targetName;

        Gift g = new Gift();
        g.id = UUID.randomUUID();
        g.from = from.getUniqueId();
        g.fromName = from.getName();
        g.to = target.getUniqueId();
        g.toName = tName;
        g.item = hand.clone();
        g.sentAt = System.currentTimeMillis();
        g.status = Status.PENDING;

        from.getInventory().setItemInMainHand(null);
        gifts.put(g.id, g);
        save();

        from.sendMessage(plugin.msg("gift-wyslano", "%player%", tName));

        Player online = target.getPlayer();
        if (online != null && plugin.data().get(online.getUniqueId(), Setting.GIFT)) {
            Notifier.show(plugin, online,
                    plugin.raw("gift-bossbar").replace("%player%", from.getName()),
                    plugin.getConfig().getInt("gift.czas-bossbara-ticki", 100));
        }
    }

    /** Odbior gifta przez adresata. */
    public boolean claim(Player p, UUID id) {
        Gift g = gifts.get(id);
        if (g == null || g.status != Status.PENDING || !g.to.equals(p.getUniqueId()) || g.item == null) {
            p.sendMessage(plugin.msg("gift-juz-nie-ma"));
            return false;
        }
        if (!Util.canFit(p, g.item)) {
            p.sendMessage(plugin.msg("gift-brak-miejsca"));
            return false;
        }
        p.getInventory().addItem(g.item.clone());
        g.status = Status.CLAIMED;
        g.closedAt = System.currentTimeMillis();
        save();

        p.sendMessage(plugin.msg("gift-odebrano", "%player%", g.fromName));
        Player sender = Bukkit.getPlayer(g.from);
        if (sender != null) {
            sender.sendMessage(plugin.msg("gift-odebrano-sender", "%player%", p.getName()));
        }
        return true;
    }

    /** Wycofanie gifta przez nadawce (dopoki nie zostal odebrany). */
    public boolean withdraw(Player p, UUID id) {
        Gift g = gifts.get(id);
        if (g == null || g.status != Status.PENDING || !g.from.equals(p.getUniqueId()) || g.item == null) {
            p.sendMessage(plugin.msg("gift-juz-nie-ma"));
            return false;
        }
        if (!Util.canFit(p, g.item)) {
            p.sendMessage(plugin.msg("gift-brak-miejsca"));
            return false;
        }
        p.getInventory().addItem(g.item.clone());
        g.status = Status.WITHDRAWN;
        g.closedAt = System.currentTimeMillis();
        save();
        p.sendMessage(plugin.msg("gift-wycofano", "%player%", g.toName));
        return true;
    }

    /** Wywolywane co minute: wygaszanie starych giftow i czyszczenie historii. */
    public void tick() {
        long now = System.currentTimeMillis();
        long expireMs = plugin.getConfig().getLong("gift.wygasa-po-godzinach", 72) * 3600_000L;
        long keepMs = 14L * 24L * 3600_000L;
        boolean changed = false;

        Iterator<Gift> it = gifts.values().iterator();
        while (it.hasNext()) {
            Gift g = it.next();
            if (g.status == Status.PENDING && now - g.sentAt >= expireMs) {
                g.status = Status.EXPIRED;
                g.closedAt = now;
                changed = true;
                Player sender = Bukkit.getPlayer(g.from);
                if (sender != null) {
                    sender.sendMessage(plugin.msg("gift-wygasl", "%player%", g.toName));
                }
            } else if (g.status != Status.PENDING && now - g.closedAt >= keepMs) {
                it.remove();
                changed = true;
            }
        }
        if (changed) {
            save();
        }
    }
}
