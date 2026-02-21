package com.convert.commands;

import com.convert.ConvertPlugin;
import com.convert.gui.ConvertGUI;
import com.convert.listeners.GUIListener;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ConvertCommand implements CommandExecutor {

    private final ConvertPlugin plugin;
    private final ConvertGUI gui;
    private final GUIListener guiListener;

    public ConvertCommand(ConvertPlugin plugin, ConvertGUI gui, GUIListener guiListener) {
        this.plugin = plugin;
        this.gui = gui;
        this.guiListener = guiListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player oyuncu)) {
            sender.sendMessage(plugin.renk(plugin.prefix() + "&cSadece oyuncular kullanabilir."));
            return true;
        }

        if (!oyuncu.hasPermission("convert.kullan")) {
            oyuncu.sendMessage(plugin.renk(plugin.prefix() + "&cBu komutu kullanma izniniz yok."));
            return true;
        }

        Inventory inv = gui.guiAc(oyuncu);
        guiListener.guiAcildi(oyuncu.getUniqueId()); // GUI takibine ekle
        oyuncu.openInventory(inv);
        return true;
    }
}
