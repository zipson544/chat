package pl.chatplus;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class Notifier {

    private Notifier() {
    }

    /** Zielony boss bar, ktory sam maleje i znika. */
    public static void show(ChatPlus plugin, Player p, String text, int ticks) {
        final BossBar bar = BossBar.bossBar(Util.color(text), 1f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
        p.showBossBar(bar);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.5f);
        new BukkitRunnable() {
            int left = ticks;

            @Override
            public void run() {
                if (!p.isOnline()) {
                    cancel();
                    return;
                }
                left -= 2;
                if (left <= 0) {
                    p.hideBossBar(bar);
                    cancel();
                    return;
                }
                bar.progress(Math.max(0f, Math.min(1f, (float) left / (float) ticks)));
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }
}
