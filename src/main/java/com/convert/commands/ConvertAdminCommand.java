package com.convert.commands;

import com.convert.ConvertPlugin;
import com.convert.gui.ConvertGUI;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Map;

public class ConvertAdminCommand implements CommandExecutor {

    private final ConvertPlugin plugin;

    public ConvertAdminCommand(ConvertPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = plugin.prefix();

        if (!sender.hasPermission("convert.admin")) {
            sender.sendMessage(plugin.renk(prefix + "&cAdmin izni gerekli."));
            return true;
        }

        if (args.length == 0) {
            yardimGoster(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            // /convertadmin fiyat <MATERIAL> <fiyat>
            case "fiyat" -> {
                if (args.length < 3) {
                    sender.sendMessage(plugin.renk(prefix + "&cKullanım: /convertadmin fiyat <MATERIAL> <fiyat>"));
                    return true;
                }
                try {
                    Material mat = Material.valueOf(args[1].toUpperCase());
                    double fiyat = Double.parseDouble(args[2]);
                    plugin.getFiyatManager().fiyatAyarla(mat, fiyat);
                    sender.sendMessage(plugin.renk(prefix + "&a" + mat.name() + " &7fiyatı &e" + fiyat + " coin &7olarak ayarlandı."));
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.renk(prefix + "&cGeçersiz fiyat."));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(plugin.renk(prefix + "&cGeçersiz material adı: &f" + args[1]));
                }
            }

            // /convertadmin liste [sayfa]
            case "liste" -> {
                Map<Material, Double> fiyatlar = plugin.getFiyatManager().tumFiyatlar();
                int sayfaBoyutu = 15;
                int sayfa = args.length > 1 ? Math.max(1, Integer.parseInt(args[1])) : 1;
                int offset = (sayfa - 1) * sayfaBoyutu;
                int toplamSayfa = (int) Math.ceil((double) fiyatlar.size() / sayfaBoyutu);

                sender.sendMessage(plugin.renk("&8&m------------------------------"));
                sender.sendMessage(plugin.renk("  &6Convert Fiyat Listesi &8(" + sayfa + "/" + toplamSayfa + ")"));
                sender.sendMessage(plugin.renk("&8&m------------------------------"));

                fiyatlar.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .skip(offset)
                        .limit(sayfaBoyutu)
                        .forEach(e -> sender.sendMessage(plugin.renk(
                                "  &e" + e.getKey().name() + " &8» &a" + ConvertGUI.formatPara(e.getValue()) + " coin"
                        )));

                sender.sendMessage(plugin.renk("&8&m------------------------------"));
                if (toplamSayfa > 1) {
                    sender.sendMessage(plugin.renk("  &7Sonraki sayfa: &f/convertadmin liste " + (sayfa < toplamSayfa ? sayfa + 1 : 1)));
                }
            }

            // /convertadmin reload
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getFiyatManager().yukle();
                sender.sendMessage(plugin.renk(prefix + "&aConfig yeniden yüklendi! &7" +
                        plugin.getFiyatManager().tumFiyatlar().size() + " fiyat yüklendi."));
            }

            default -> yardimGoster(sender);
        }
        return true;
    }

    private void yardimGoster(CommandSender sender) {
        sender.sendMessage(plugin.renk("&8&m------------------------------"));
        sender.sendMessage(plugin.renk("  &6ConvertAdmin &7Komutları"));
        sender.sendMessage(plugin.renk("&8&m------------------------------"));
        sender.sendMessage(plugin.renk("  &e/convertadmin fiyat <MATERIAL> <fiyat> &7» Fiyat ayarla"));
        sender.sendMessage(plugin.renk("  &e/convertadmin liste &7» Tüm fiyatları listele"));
        sender.sendMessage(plugin.renk("  &e/convertadmin reload &7» Config'i yenile"));
        sender.sendMessage(plugin.renk("&8&m------------------------------"));
    }
}
