package net.redm1ne.visibilitytogglered.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack is;

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(ItemStack is) {
        this.is = is;
    }

    public ItemBuilder(Material material, int amount) {
        is = new ItemStack(material, amount);
    }

    public ItemBuilder(String materialName) {
        this(materialName, 1);
    }

    public ItemBuilder(String materialName, int amount) {
        Material mat = resolveMaterial(materialName);
        is = new ItemStack(mat != null ? mat : Material.STONE, amount);
    }

    private static Material resolveMaterial(String name) {
        if (name == null || name.isEmpty()) {
            return Material.STONE;
        }
        String upper = name.toUpperCase();

        // Try direct match first (1.13+ modern names)
        Material mat = Material.matchMaterial(upper);
        if (mat != null) {
            return mat;
        }

        // Try legacy match (1.12 and older names)
        mat = Material.matchMaterial("LEGACY_" + upper);
        if (mat != null) {
            return mat;
        }

        // Fallback mapping for common legacy names with data values used by this plugin
        switch (upper) {
            case "INK_SACK":
            case "DYE":
                return Material.valueOf("LIME_DYE");
            default:
                return Material.STONE;
        }
    }

    public ItemBuilder clone() {
        return new ItemBuilder(is.clone());
    }

    public ItemBuilder setDurability(short dur) {
        is.setDurability(dur);
        return this;
    }

    public ItemBuilder setName(String name) {
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', name));
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        ItemMeta im = is.getItemMeta();
        for (int i = 0; i < lore.length; i++) {
            lore[i] = net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', lore[i]);
        }
        im.setLore(Arrays.asList(lore));
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        ItemMeta im = is.getItemMeta();
        List<String> l = new ArrayList<>();
        for (String s : lore) {
            l.add(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', s));
        }
        im.setLore(l);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder addLoreLine(String line) {
        ItemMeta im = is.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (im.hasLore()) lore = new ArrayList<>(im.getLore());
        lore.add(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', line));
        im.setLore(lore);
        is.setItemMeta(im);
        return this;
    }

    public Material getMaterial() {
        return is.getType();
    }

    public ItemStack toItemStack() {
        return is;
    }
}
