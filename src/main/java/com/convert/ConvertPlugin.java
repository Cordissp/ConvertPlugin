package com.convert;

import com.convert.commands.*;
import com.convert.gui.ConvertGUI;
import com.convert.listeners.GUIListener;
import com.convert.managers.FiyatManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class ConvertPlugin extends JavaPlugin {

    private FiyatManager fiyatManager;
    private ConvertGUI gui;
    private GUIListener guiListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        fiyatManager = new FiyatManager(this);
        gui = new ConvertGUI(this);
        guiListener = new GUIListener(this, gui);

        // Listener'ı kaydet
        getServer().getPluginManager().registerEvents(guiListener, this);

        // Komutlar - guiListener'ı geçir
        getCommand("convert").setExecutor(new ConvertCommand(this, gui, guiListener));
        getCommand("convertadmin").setExecutor(new ConvertAdminCommand(this));

        getLogger().info("ConvertPlugin v" + getDescription().getVersion() + " yüklendi! " +
                fiyatManager.tumFiyatlar().size() + " eşya fiyatı aktif.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ConvertPlugin kapatıldı.");
    }

    public String renk(String mesaj) {
        return ChatColor.translateAlternateColorCodes('&', mesaj);
    }

    public String prefix() {
        return getConfig().getString("prefix", "&8[&6Convert&8] &r");
    }

    public FiyatManager getFiyatManager() { return fiyatManager; }
    public ConvertGUI getGui() { return gui; }
    public GUIListener getGuiListener() { return guiListener; }
}
