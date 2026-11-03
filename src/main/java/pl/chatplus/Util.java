package pl.chatplus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Util {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder().character('&').hexColors().build();

    private Util() {
    }

    public static Component color(String s) {
        return LEGACY.deserialize(s);
    }

    /** Komponent do itemow (bez domyslnej kursywy). */
    public static Component item(String s) {
        return color(s).decoration(TextDecoration.ITALIC, false);
    }

    public static ItemStack make(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(item(name));
        List<Component> l = new ArrayList<>();
        for (String line : lore) {
            l.add(item(line));
        }
        meta.lore(l);
        stack.setItemMeta(meta);
        return stack;
    }

    public static String status(boolean on) {
        return on ? "&aWŁĄCZONE" : "&cWYŁĄCZONE";
    }

    public static String date(long millis) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(millis));
    }

    public static String duration(long millis) {
        if (millis < 0) {
            millis = 0;
        }
        long totalMinutes = millis / 60000L;
        long h = totalMinutes / 60;
        long m = totalMinutes % 60;
        return h + "h " + m + "m";
    }

    /** Sprawdza czy caly stack zmiesci sie w ekwipunku gracza. */
    public static boolean canFit(Player p, ItemStack item) {
        Inventory inv = p.getInventory();
        int space = 0;
        int max = item.getMaxStackSize();
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getItem(i);
            if (s == null || s.getType().isAir()) {
                space += max;
            } else if (s.isSimilar(item)) {
                space += Math.max(0, max - s.getAmount());
            }
        }
        return space >= item.getAmount();
    }
}
