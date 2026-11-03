package pl.chatplus;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Menus {

    public enum Type {SETTINGS, GIFT, CLAIM, HISTORY}

    public static class MenuHolder implements InventoryHolder {
        public final Type type;
        public final Map<Integer, UUID> gifts = new HashMap<>();
        private Inventory inventory;

        public MenuHolder(Type type) {
            this.type = type;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private static final Map<Integer, Setting> SETTING_SLOTS = Map.of(
            11, Setting.CHAT,
            12, Setting.PRIVATE,
            14, Setting.DEATH,
            15, Setting.AUTO
    );

    private static final String CLICK_RIGHT = "&8» &2Kliknij &aPRAWYM&2, aby przełączyć!";
    private static final String CLICK_LEFT = "&8» &2Kliknij &alewym&2, aby przejść!";

    private final ChatPlus plugin;

    public Menus(ChatPlus plugin) {
        this.plugin = plugin;
    }

    private void click(Player p) {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1f);
    }

    private ItemStack closeItem() {
        return Util.make(Material.BARRIER, "&cZamknij", List.of());
    }

    private ItemStack backItem() {
        return Util.make(Material.ARROW, "&7« Wróć", List.of());
    }

    // =====================================================================
    //  /ustawienia
    // =====================================================================

    public void openSettings(Player p) {
        MenuHolder h = new MenuHolder(Type.SETTINGS);
        Inventory inv = Bukkit.createInventory(h, 54, Util.color("&8Ustawienia"));
        h.setInventory(inv);
        fillSettings(inv, p);
        p.openInventory(inv);
    }

    private void fillSettings(Inventory inv, Player p) {
        inv.clear();
        for (Map.Entry<Integer, Setting> e : SETTING_SLOTS.entrySet()) {
            boolean on = plugin.data().get(p.getUniqueId(), e.getValue());
            inv.setItem(e.getKey(), settingItem(e.getValue(), on));
            inv.setItem(e.getKey() + 9, indicator(on));
        }
        inv.setItem(49, closeItem());
    }

    private ItemStack indicator(boolean on) {
        if (on) {
            return Util.make(Material.LIME_DYE, "&aWłączone", List.of("&7Kliknij &aPRAWYM&7, aby wyłączyć"));
        }
        return Util.make(Material.RED_DYE, "&cWyłączone", List.of("&7Kliknij &aPRAWYM&7, aby włączyć"));
    }

    private ItemStack settingItem(Setting s, boolean on) {
        String status = "&8» &7Status: " + Util.status(on);
        switch (s) {
            case CHAT:
                return Util.make(Material.OAK_SIGN, "&6Wiadomości na czacie",
                        List.of(status, "", CLICK_RIGHT));
            case PRIVATE:
                return Util.make(Material.WARPED_SIGN, "&bPrywatne wiadomości",
                        List.of(status, "",
                                "&cUwaga!",
                                "&cWyłączenie tego ustawienia zablokuje",
                                "&cotrzymywanie prywatnych wiadomości (&e/msg&c).",
                                "", CLICK_RIGHT));
            case DEATH:
                return Util.make(Material.WITHER_SKELETON_SKULL, "&cWiadomości o śmierci na czacie",
                        List.of(status, "", CLICK_RIGHT));
            case AUTO:
            default:
                return Util.make(Material.PAPER, "&eAutomatyczne wiadomości",
                        List.of(status, "",
                                "&cUwaga!",
                                "&cUstawienie to pozwala na otrzymywanie",
                                "&cautomatycznych wiadomości na czacie, które",
                                "&cposiadają cenne wskazówki!",
                                "", CLICK_RIGHT));
        }
    }

    // =====================================================================
    //  /gift
    // =====================================================================

    public void openGift(Player p) {
        MenuHolder h = new MenuHolder(Type.GIFT);
        Inventory inv = Bukkit.createInventory(h, 36, Util.color("&8Gift"));
        h.setInventory(inv);
        fillGift(inv, p);
        p.openInventory(inv);
    }

    private void fillGift(Inventory inv, Player p) {
        inv.clear();
        long hours = plugin.getConfig().getLong("gift.wygasa-po-godzinach", 72);
        int pending = plugin.gifts().pending(p.getUniqueId()).size();

        inv.setItem(11, Util.make(Material.PAPER, "&aWyślij gifta offline", List.of(
                "&8» &cUwaga: &fprzesyłka wygasa po " + hours + " godzinach",
                "&8» &fco będzie się wiązać z utratą przedmiotu na zawsze!",
                "&8» &fWysłaną przesyłkę możesz wycofać, dopóki nie zostanie odebrana",
                "",
                "&8» &aKliknij, aby wysłać przedmiot z ręki!")));

        inv.setItem(13, Util.make(Material.CHEST, "&6Odbierz przedmioty", List.of(
                "&8» &7Oczekujące przedmioty: &e" + pending,
                "",
                CLICK_LEFT)));

        inv.setItem(16, Util.make(Material.BOOK, "&bHistoria giftów", List.of(
                "&8» &7Podgląd historii wysyłek i odbiorów",
                CLICK_LEFT)));

        boolean notify = plugin.data().get(p.getUniqueId(), Setting.GIFT);
        inv.setItem(32, Util.make(Material.BELL, "&ePowiadomienia o prezentach", List.of(
                "&8» &7Status: " + Util.status(notify),
                "",
                "&8» &fWłącza/wyłącza powiadomienia",
                "&8» &fo nowych prezentach (/gift).",
                "",
                CLICK_RIGHT)));

        inv.setItem(31, closeItem());
    }

    // =====================================================================
    //  Gift - Odbior
    // =====================================================================

    public void openClaim(Player p) {
        MenuHolder h = new MenuHolder(Type.CLAIM);
        Inventory inv = Bukkit.createInventory(h, 54, Util.color("&8Gift - Odbiór"));
        h.setInventory(inv);
        fillClaim(h, p);
        p.openInventory(inv);
    }

    private void fillClaim(MenuHolder h, Player p) {
        Inventory inv = h.getInventory();
        inv.clear();
        h.gifts.clear();
        long expireMs = plugin.getConfig().getLong("gift.wygasa-po-godzinach", 72) * 3600_000L;
        long now = System.currentTimeMillis();

        List<GiftManager.Gift> list = plugin.gifts().pending(p.getUniqueId());
        for (int i = 0; i < list.size() && i < 45; i++) {
            GiftManager.Gift g = list.get(i);
            long left = g.sentAt + expireMs - now;
            inv.setItem(i, display(g, List.of(
                    "",
                    "&8» &7Od: &e" + g.fromName,
                    "&8» &7Wygasa za: &c" + Util.duration(left),
                    "",
                    "&8» &2Kliknij &alewym&2, aby odebrać!")));
            h.gifts.put(i, g.id);
        }
        inv.setItem(48, backItem());
        inv.setItem(49, closeItem());
    }

    // =====================================================================
    //  Gift - Historia
    // =====================================================================

    public void openHistory(Player p) {
        MenuHolder h = new MenuHolder(Type.HISTORY);
        Inventory inv = Bukkit.createInventory(h, 54, Util.color("&8Gift - Historia"));
        h.setInventory(inv);
        fillHistory(h, p);
        p.openInventory(inv);
    }

    private void fillHistory(MenuHolder h, Player p) {
        Inventory inv = h.getInventory();
        inv.clear();
        h.gifts.clear();

        List<GiftManager.Gift> list = plugin.gifts().history(p.getUniqueId());
        for (int i = 0; i < list.size() && i < 45; i++) {
            GiftManager.Gift g = list.get(i);
            boolean sentByMe = g.from.equals(p.getUniqueId());
            List<String> extra = new ArrayList<>();
            extra.add("");
            extra.add(sentByMe ? "&8» &7Wysłano do: &e" + g.toName : "&8» &7Otrzymano od: &e" + g.fromName);
            extra.add("&8» &7Data: &f" + Util.date(g.sentAt));
            extra.add("&8» &7Status: " + statusLabel(g.status));
            if (sentByMe && g.status == GiftManager.Status.PENDING) {
                extra.add("");
                extra.add("&8» &2Kliknij &alewym&2, aby wycofać!");
                h.gifts.put(i, g.id);
            }
            inv.setItem(i, display(g, extra));
        }
        inv.setItem(48, backItem());
        inv.setItem(49, closeItem());
    }

    private String statusLabel(GiftManager.Status s) {
        switch (s) {
            case PENDING:
                return "&eOczekuje";
            case CLAIMED:
                return "&aOdebrano";
            case WITHDRAWN:
                return "&7Wycofano";
            default:
                return "&cWygasło";
        }
    }

    /** Kopia przedmiotu z gifta z dopisanym lorem. */
    private ItemStack display(GiftManager.Gift g, List<String> extra) {
        ItemStack d = g.item == null ? new ItemStack(Material.BARRIER) : g.item.clone();
        ItemMeta meta = d.getItemMeta();
        List<Component> lore = new ArrayList<>();
        if (meta.lore() != null) {
            lore.addAll(meta.lore());
        }
        for (String line : extra) {
            lore.add(Util.item(line));
        }
        meta.lore(lore);
        d.setItemMeta(meta);
        return d;
    }

    // =====================================================================
    //  Klikniecia
    // =====================================================================

    public void handleClick(Player p, MenuHolder h, int slot, ClickType click) {
        switch (h.type) {
            case SETTINGS: {
                if (slot == 49) {
                    p.closeInventory();
                    return;
                }
                Setting s = SETTING_SLOTS.get(slot);
                if (s == null) {
                    s = SETTING_SLOTS.get(slot - 9);
                }
                if (s != null && click.isRightClick()) {
                    plugin.data().toggle(p.getUniqueId(), s);
                    fillSettings(h.getInventory(), p);
                    click(p);
                }
                break;
            }
            case GIFT: {
                if (slot == 31) {
                    p.closeInventory();
                } else if (slot == 11 && click.isLeftClick()) {
                    click(p);
                    startPrompt(p);
                } else if (slot == 13 && click.isLeftClick()) {
                    click(p);
                    openClaim(p);
                } else if (slot == 16 && click.isLeftClick()) {
                    click(p);
                    openHistory(p);
                } else if (slot == 32 && click.isRightClick()) {
                    plugin.data().toggle(p.getUniqueId(), Setting.GIFT);
                    fillGift(h.getInventory(), p);
                    click(p);
                }
                break;
            }
            case CLAIM: {
                if (slot == 49) {
                    p.closeInventory();
                } else if (slot == 48) {
                    click(p);
                    openGift(p);
                } else if (click.isLeftClick() && h.gifts.containsKey(slot)) {
                    plugin.gifts().claim(p, h.gifts.get(slot));
                    fillClaim(h, p);
                    click(p);
                }
                break;
            }
            case HISTORY: {
                if (slot == 49) {
                    p.closeInventory();
                } else if (slot == 48) {
                    click(p);
                    openGift(p);
                } else if (click.isLeftClick() && h.gifts.containsKey(slot)) {
                    plugin.gifts().withdraw(p, h.gifts.get(slot));
                    fillHistory(h, p);
                    click(p);
                }
                break;
            }
            default:
                break;
        }
    }

    private void startPrompt(Player p) {
        p.closeInventory();
        if (p.getInventory().getItemInMainHand().getType().isAir()) {
            p.sendMessage(plugin.msg("gift-brak-przedmiotu"));
            return;
        }
        plugin.giftPrompts().put(p.getUniqueId(), System.currentTimeMillis() + 30_000L);
        p.sendMessage(plugin.msg("gift-prompt"));
    }
}
