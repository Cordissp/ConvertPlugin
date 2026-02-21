package com.convert.listeners;

import com.convert.ConvertPlugin;
import com.convert.gui.ConvertGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GUIListener implements Listener {

    private final ConvertPlugin plugin;
    private final ConvertGUI gui;

    // getView().getTitle() yerine UUID seti kullanıyoruz - 1.20.1 uyumlu
    private final Set<UUID> acikGui = new HashSet<>();

    private static final Set<Integer> KORUNAN_SLOTLAR = Set.of(
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    public GUIListener(ConvertPlugin plugin, ConvertGUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    public void guiAcildi(UUID uuid) {
        acikGui.add(uuid);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player oyuncu)) return;
        if (!acikGui.contains(oyuncu.getUniqueId())) return;
        if (event.getClickedInventory() == null) return;

        int slot = event.getRawSlot();

        // Korunan alt çubuk
        if (KORUNAN_SLOTLAR.contains(slot)) {
            event.setCancelled(true);

            if (slot == ConvertGUI.ONAY_SLOT) {
                acikGui.remove(oyuncu.getUniqueId());
                Inventory inv = event.getInventory();
                oyuncu.closeInventory();
                convertYap(oyuncu, inv);
            }

            if (slot == ConvertGUI.IPTAL_SLOT) {
                acikGui.remove(oyuncu.getUniqueId());
                esyalariGeriVer(oyuncu, event.getInventory());
                oyuncu.closeInventory();
                oyuncu.sendMessage(plugin.renk(plugin.prefix() + "&7Convert iptal edildi. Eşyaların iade edildi."));
            }
            return;
        }

        // Eşya slotu - 1 tick sonra toplamı güncelle
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (oyuncu.isOnline() && acikGui.contains(oyuncu.getUniqueId())) {
                gui.toplamGuncelle(oyuncu.getOpenInventory().getTopInventory());
            }
        }, 1L);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player oyuncu)) return;
        if (!acikGui.contains(oyuncu.getUniqueId())) return;

        for (int slot : event.getRawSlots()) {
            if (KORUNAN_SLOTLAR.contains(slot)) {
                event.setCancelled(true);
                return;
            }
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (oyuncu.isOnline() && acikGui.contains(oyuncu.getUniqueId())) {
                gui.toplamGuncelle(oyuncu.getOpenInventory().getTopInventory());
            }
        }, 1L);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player oyuncu)) return;
        if (!acikGui.contains(oyuncu.getUniqueId())) return;

        acikGui.remove(oyuncu.getUniqueId());

        // Onaylamadan kapandı - eşyaları iade et
        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                esyalariGeriVer(oyuncu, event.getInventory()), 1L);
    }

    private void convertYap(Player oyuncu, Inventory inv) {
        double toplamPara = 0;
        List<String> satirlar = new ArrayList<>();
        boolean hicEsyaYok = true;
        List<ItemStack> kabulsuzEsyalar = new ArrayList<>();

        for (int slot : ConvertGUI.ESYA_SLOTLARI) {
            ItemStack item = inv.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;
            if (isDekorasyon(item.getType())) continue;

            hicEsyaYok = false;
            double birimFiyat = plugin.getFiyatManager().fiyatAl(item.getType());

            if (birimFiyat <= 0) {
                kabulsuzEsyalar.add(item.clone());
                inv.setItem(slot, null);
                continue;
            }

            double kazanilan = birimFiyat * item.getAmount();
            toplamPara += kazanilan;

            String itemIsim = guzelIsim(item.getType());
            String satirMesaj = plugin.getConfig()
                    .getString("mesaj-format", "&8[&6Convert&8] &a%amount%x &e%item% &7sattınız! &a+%para% coin")
                    .replace("%amount%", String.valueOf(item.getAmount()))
                    .replace("%item%", itemIsim)
                    .replace("%para%", ConvertGUI.formatPara(kazanilan));
            satirlar.add(satirMesaj);
            inv.setItem(slot, null);
        }

        if (hicEsyaYok) {
            oyuncu.sendMessage(plugin.renk(plugin.prefix() + "&cGUI'ye eşya koymalısın!"));
            acikGui.add(oyuncu.getUniqueId());
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                    oyuncu.openInventory(gui.guiAc(oyuncu)), 1L);
            return;
        }

        if (!kabulsuzEsyalar.isEmpty()) {
            for (ItemStack esya : kabulsuzEsyalar) {
                oyuncu.getInventory().addItem(esya);
            }
            oyuncu.sendMessage(plugin.renk(plugin.prefix() + "&cBazı eşyaların fiyatı olmadığı için iade edildi."));
        }

        if (toplamPara <= 0) {
            oyuncu.sendMessage(plugin.renk(plugin.prefix() + "&cHiçbir eşyanın fiyatı bulunamadı!"));
            return;
        }

        String verKomut = plugin.getConfig()
                .getString("ekonomi-ver-komutu", "eco give %player% %amount%")
                .replace("%player%", oyuncu.getName())
                .replace("%amount%", String.valueOf((int) toplamPara));
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), verKomut);

        for (String satir : satirlar) {
            oyuncu.sendMessage(plugin.renk(satir));
        }
        oyuncu.sendMessage(plugin.renk("&8&m----------------------------"));
        oyuncu.sendMessage(plugin.renk("  &6\uD83D\uDCB0 Toplam Kazanç: &a+" + ConvertGUI.formatPara(toplamPara) + " coin"));
        oyuncu.sendMessage(plugin.renk("&8&m----------------------------"));
    }

    private void esyalariGeriVer(Player oyuncu, Inventory inv) {
        for (int slot : ConvertGUI.ESYA_SLOTLARI) {
            ItemStack item = inv.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;
            if (isDekorasyon(item.getType())) continue;
            oyuncu.getInventory().addItem(item.clone());
            inv.setItem(slot, null);
        }
    }

    private boolean isDekorasyon(Material mat) {
        return mat == Material.LIME_STAINED_GLASS_PANE
                || mat == Material.BLACK_STAINED_GLASS_PANE
                || mat == Material.RED_STAINED_GLASS_PANE
                || mat == Material.PAPER
                || mat == Material.GOLD_INGOT;
    }

    private String guzelIsim(Material mat) {
        String isim = mat.name().replace("_", " ").toLowerCase();
        return Character.toUpperCase(isim.charAt(0)) + isim.substring(1);
    }
}
