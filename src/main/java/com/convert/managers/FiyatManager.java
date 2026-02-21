package com.convert.managers;

import com.convert.ConvertPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FiyatManager {

    private final ConvertPlugin plugin;
    private final Map<Material, Double> fiyatlar = new HashMap<>();

    public FiyatManager(ConvertPlugin plugin) {
        this.plugin = plugin;
        yukle();
    }

    public void yukle() {
        fiyatlar.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("fiyatlar");
        if (section == null) {
            plugin.getLogger().warning("config.yml'de 'fiyatlar' bölümü bulunamadı!");
            return;
        }

        for (String key : section.getKeys(false)) {
            try {
                Material mat = Material.valueOf(key.toUpperCase());
                double fiyat = section.getDouble(key);
                fiyatlar.put(mat, fiyat);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Geçersiz material: " + key);
            }
        }

        plugin.getLogger().info(fiyatlar.size() + " eşya fiyatı yüklendi.");
    }

    public double fiyatAl(Material mat) {
        return fiyatlar.getOrDefault(mat, 0.0);
    }

    public boolean fiyatiVarMi(Material mat) {
        return fiyatlar.containsKey(mat) && fiyatlar.get(mat) > 0;
    }

    public Map<Material, Double> tumFiyatlar() {
        return Map.copyOf(fiyatlar);
    }

    public void fiyatAyarla(Material mat, double fiyat) {
        fiyatlar.put(mat, fiyat);
        plugin.getConfig().set("fiyatlar." + mat.name(), fiyat);
        plugin.saveConfig();
    }

    public Set<Material> desteklenenMaterialler() {
        return fiyatlar.keySet();
    }
}
