package pl.chatplus;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final ChatPlus plugin;

    public AdminCommand(ChatPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("chatplus.reload")) {
            sender.sendMessage(Component.text("Brak uprawnień."));
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            sender.sendMessage(Util.color("&aPrzeładowano konfigurację ChatPlus."));
        } else {
            sender.sendMessage(Util.color("&cUżycie: &e/chatplus reload"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        return args.length == 1 ? List.of("reload") : List.of();
    }
}
