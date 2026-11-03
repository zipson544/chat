package pl.chatplus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MsgCommand implements CommandExecutor, TabCompleter {

    private final ChatPlus plugin;
    private final Map<UUID, UUID> lastPartner = new ConcurrentHashMap<>();

    public MsgCommand(ChatPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.msg("tylko-gracze"));
            return true;
        }
        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("r")) {
            if (args.length < 1) {
                p.sendMessage(plugin.msg("r-uzycie"));
                return true;
            }
            UUID partner = lastPartner.get(p.getUniqueId());
            Player target = partner == null ? null : Bukkit.getPlayer(partner);
            if (target == null) {
                p.sendMessage(plugin.msg("brak-odpowiedzi"));
                return true;
            }
            send(p, target, String.join(" ", args));
            return true;
        }

        if (args.length < 2) {
            p.sendMessage(plugin.msg("msg-uzycie"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            p.sendMessage(plugin.msg("brak-gracza"));
            return true;
        }
        if (target.getUniqueId().equals(p.getUniqueId())) {
            p.sendMessage(plugin.msg("msg-do-siebie"));
            return true;
        }
        String text = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        send(p, target, text);
        return true;
    }

    private void send(Player from, Player to, String text) {
        if (!plugin.data().get(to.getUniqueId(), Setting.PRIVATE)
                && !from.hasPermission("chatplus.bypass.privacy")) {
            from.sendMessage(plugin.msg("msg-wylaczone"));
            return;
        }
        from.sendMessage(format("msg-wyslana", to.getName(), text));
        to.sendMessage(format("msg-odebrana", from.getName(), text));
        to.playSound(to.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.2f);

        lastPartner.put(from.getUniqueId(), to.getUniqueId());
        lastPartner.put(to.getUniqueId(), from.getUniqueId());
    }

    private Component format(String key, String player, String text) {
        String f = plugin.raw(key).replace("%player%", player);
        return Util.color(f).replaceText(TextReplacementConfig.builder()
                .matchLiteral("%message%")
                .replacement(Component.text(text))
                .build());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (cmd.getName().equalsIgnoreCase("msg") && args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    out.add(p.getName());
                }
            }
        }
        return out;
    }
}
