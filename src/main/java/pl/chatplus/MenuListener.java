package pl.chatplus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class MenuListener implements Listener {

    private final ChatPlus plugin;

    public MenuListener(ChatPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder() instanceof Menus.MenuHolder)) {
            return;
        }
        Menus.MenuHolder holder = (Menus.MenuHolder) top.getHolder();
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        if (e.getClickedInventory() == null || e.getClickedInventory() != top) {
            return;
        }
        plugin.menus().handleClick((Player) e.getWhoClicked(), holder, e.getSlot(), e.getClick());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof Menus.MenuHolder) {
            e.setCancelled(true);
        }
    }
}
