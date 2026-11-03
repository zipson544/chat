package pl.chatplus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements Listener {

    private final ChatPlus plugin;

    public JoinListener(ChatPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!p.isOnline()) {
                return;
            }
            int n = plugin.gifts().pending(p.getUniqueId()).size();
            if (n > 0 && plugin.data().get(p.getUniqueId(), Setting.GIFT)) {
                Notifier.show(plugin, p,
                        plugin.raw("gift-bossbar-oczekujace").replace("%ilosc%", String.valueOf(n)),
                        plugin.getConfig().getInt("gift.czas-bossbara-ticki", 100));
            }
        }, 60L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.giftPrompts().remove(e.getPlayer().getUniqueId());
    }
}
