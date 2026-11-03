package pl.chatplus;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatListener implements Listener {

    private final ChatPlus plugin;
    private final Map<UUID, Long> lastChat = new ConcurrentHashMap<>();

    public ChatListener(ChatPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChatEarly(AsyncChatEvent e) {
        Player p = e.getPlayer();
        UUID id = p.getUniqueId();

        // 1) wpisywanie nicku odbiorcy gifta
        Long until = plugin.giftPrompts().remove(id);
        if (until != null) {
            e.setCancelled(true);
            if (System.currentTimeMillis() > until) {
                p.sendMessage(plugin.msg("gift-prompt-wygaslo"));
                return;
            }
            String text = PlainTextComponentSerializer.plainText().serialize(e.message()).trim();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (text.equalsIgnoreCase("anuluj")) {
                    p.sendMessage(plugin.msg("gift-anulowano"));
                } else {
                    plugin.gifts().send(p, text);
                }
            });
            return;
        }

        // 2) cooldown czatu
        if (p.hasPermission("chatplus.bypass.cooldown")) {
            return;
        }
        long cooldownSec = plugin.getConfig().getLong("czat.cooldown-sekundy", 5);
        long cooldownMs = cooldownSec * 1000L;
        long now = System.currentTimeMillis();
        Long last = lastChat.get(id);
        if (last != null && now - last < cooldownMs) {
            e.setCancelled(true);
            long left = (long) Math.ceil((cooldownMs - (now - last)) / 1000.0);
            p.sendMessage(plugin.msg("czat-cooldown",
                    "%cooldown%", String.valueOf(cooldownSec),
                    "%czas%", String.valueOf(left)));
            return;
        }
        lastChat.put(id, now);
    }

    /** Gracze z wylaczonymi wiadomosciami na czacie nie widza cudzych wiadomosci. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChatLate(AsyncChatEvent e) {
        Player sender = e.getPlayer();
        e.viewers().removeIf((Audience a) -> a instanceof Player v
                && !v.getUniqueId().equals(sender.getUniqueId())
                && !plugin.data().get(v.getUniqueId(), Setting.CHAT));
    }

    /** Wiadomosci o smierci - wysylane tylko tym, co je maja wlaczone. */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        Component msg = e.deathMessage();
        if (msg == null) {
            return;
        }
        e.deathMessage(null);
        Bukkit.getConsoleSender().sendMessage(msg);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.data().get(p.getUniqueId(), Setting.DEATH)) {
                p.sendMessage(msg);
            }
        }
    }
}
