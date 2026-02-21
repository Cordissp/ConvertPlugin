package com.convert.gui;

import com.convert.ConvertPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ConvertGUI {

    private final ConvertPlugin plugin;

    // GUI layout:
    // [0-35] = Eşya koyma slotları (4 satır)
    // [36-44] = Alt çubuk: bilgi + onay + iptal

    public static final int[] ESYA_SLOTLARI = {
        0,1,2,3,4,5,6,7,8,
        9,10,11,12,13,14,15,16,17,
        18,19,20,21,22,23,24,25,26,
        27,28,29,30,31,32,33,34,35
    };

    public static final int ONAY_SLOT = 40;
    public static final int IPTAL_SLOT = 36;
    public static final int BILGI_SLOT = 38;
    public static final int TOPLAM_SLOT = 42;

    public ConvertGUI(ConvertPlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory guiAc(Player oyuncu) {
        Inventory inv = Bukkit.createInventory(null, 45,
                plugin.renk("&6💱 Convert &8- &7Eşya Sat"));

        // Alt çubuk ayraç
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, camItem(Material.BLACK_STAINED_GLASS_PANE, "&8"));
        }

        // İptal butonu
        inv.setItem(IPTAL_SLOT, butonItem(Material.RED_STAINED_GLASS_PANE,
                "&cİptal",
                List.of("&7Convert işlemini iptal et.", "&7Eşyalar iade edilir.")));

        // Bilgi
        inv.setItem(BILGI_SLOT, butonItem(Material.PAPER,
                "&eNasıl kullanılır?",
                List.of(
                        "&7Satmak istediğin eşyaları",
                        "&7yukarıdaki slotlara koy.",
                        "&7Sonra &a✔ Onayla &7butonuna bas.",
                        "",
                        "&7Sadece config'de fiyatı olan",
                        "&7eşyalar kabul edilir."
                )));

        // Onay butonu
        inv.setItem(ONAY_SLOT, butonItem(Material.LIME_STAINED_GLASS_PANE,
                "&a✔ Onayla",
                List.of("&7Eşyaları sat ve parayı al!")));

        // Toplam
        inv.setItem(TOPLAM_SLOT, butonItem(Material.GOLD_INGOT,
                "&6Toplam: &e0 coin",
                List.of("&7Eşya koydukça güncellenir.")));

        return inv;
    }

    // Toplam fiyatı hesapla - GUI'deki tüm eşyalara bak
    public double toplamHesapla(Inventory inv) {
        double toplam = 0;
        for (int slot : ESYA_SLOTLARI) {
            ItemStack item = inv.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;

            double birimFiyat = plugin.getFiyatManager().fiyatAl(item.getType());
            if (birimFiyat <= 0) continue;

            toplam += birimFiyat * item.getAmount();
        }
        return toplam;
    }

    // Toplam slot'u güncelle
    public void toplamGuncelle(Inventory inv) {
        double toplam = toplamHesapla(inv);
        ItemStack toplamItem = butonItem(Material.GOLD_INGOT,
                "&6Toplam: &e" + formatPara(toplam) + " coin",
                List.of(
                        "&7Konan eşyaların toplam değeri.",
                        "",
                        "&a✔ Onayla &7butonuna bas!"
                ));
        inv.setItem(TOPLAM_SLOT, toplamItem);
    }

    // Para formatla (1000 -> 1.000)
    public static String formatPara(double para) {
        if (para >= 1_000_000) {
            return String.format("%.1fM", para / 1_000_000);
        } else if (para >= 1_000) {
            return String.format("%,.0f", para);
        }
        return String.format("%.0f", para);
    }

    private ItemStack camItem(Material mat, String isim) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.renk(isim));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack butonItem(Material mat, String isim, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.renk(isim));
        if (lore != null) meta.setLore(lore.stream().map(plugin::renk).toList());
        item.setItemMeta(meta);
        return item;
    }

    public static boolean convertGuiMi(String baslik) {
        return baslik.contains("Convert") && baslik.contains("Eşya Sat");
    }
}
