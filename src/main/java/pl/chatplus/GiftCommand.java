package pl.chatplus;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GiftCommand implements CommandExecutor, TabCompleter {

    private final ChatPlus plugin;

    public GiftCommand(ChatPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.msg("tylko-gracze"));
            return true;
        }
        Player p = (Player) sender;
        if (args.length == 0) {
            plugin.menus().openGift(p);
        } else if (args.length == 1) {
            plugin.gifts().send(p, args[0]);
        } else {
            p.sendMessage(plugin.msg("gift-uzycie"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    out.add(p.getName());
                }
            }
        }
        return out;
    }
}
