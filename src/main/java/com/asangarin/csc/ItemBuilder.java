package com.asangarin.csc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class ItemBuilder {
	public static ItemStack build(ConfigurationSection config) {
		boolean errored = false;
		ItemStack item;
		List<String> lore = new ArrayList<String>();
		
		if(Material.valueOf(config.getString("material")) != null)
			item = new ItemStack(Material.valueOf(config.getString("material")));
		else {
			item = new ItemStack(Material.BARRIER);
			errored = true;
			lore.add("&cMaterial '&e" + config.getString("material") + "&c' is invalid.");
		}
		
		ItemMeta meta = item.getItemMeta();
		if(config.contains("name"))
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("name")));
		if(config.contains("model"))
			meta.setCustomModelData(config.getInt("model"));
		if(config.getBoolean("hide-flags", false))
			meta.addItemFlags(ItemFlag.values());
		if(config.getBoolean("unbreakable", false))
			meta.setUnbreakable(true);
		if(config.contains("damage")) {
			if(meta instanceof Damageable)
				((Damageable) meta).setDamage(config.getInt("damage"));
			else { 
				errored = true;
				lore.add("&cCouldn't add damage value.");
				lore.add("&cDoes this item have durability?");
			}
		}
		if(config.contains("skull-owner-uuid") || config.contains("skull-texture-value")) {
			if(meta instanceof SkullMeta) {
				if(config.contains("skull-owner-uuid"))
					((SkullMeta) meta).setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(config.getString("skull-owner-uuid"))));
				else if(config.contains("skull-texture-value")) {
					try {
						Field profileField = meta.getClass().getDeclaredField("profile");
						profileField.setAccessible(true);
						GameProfile gp = new GameProfile(UUID.randomUUID(), null);
						gp.getProperties().put("textures", new Property("textures", config.getString("skull-texture-value")));
						profileField.set(meta, gp);
					} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
						errored = true;
						lore.add("&cCouldn't add head texture.");
						lore.add("&cIs your Spigot version supported?");
						e.printStackTrace();
					}
				}
			}
			else {
				errored = true;
				lore.add("&cCouldn't add head texture.");
				lore.add("&cIs the Material '&ePLAYER_HEAD'&c?");
			}
		}
		
		if(errored) {
			item = new ItemStack(Material.BARRIER);
			meta = item.getItemMeta();
			lore.add("");
			lore.add("&cConfig: '&e" + config.getName() + "&c'");
			lore.add("&c(&e" + config.getCurrentPath() + "&c)");
			
			for(ListIterator<String> i = lore.listIterator(); i.hasNext();) {
				String line = i.next();
				i.set(ChatColor.translateAlternateColorCodes('&', line));
			}
			
			meta.setDisplayName(ChatColor.DARK_RED + "ERROR!");
			meta.setLore(lore);
			item.setItemMeta(meta);
		}
		else
			if(config.contains("lore")) {
				for(String line : config.getStringList("lore"))
					lore.add(ChatColor.translateAlternateColorCodes('&', line));
				meta.setLore(lore);
			}
			
		item.setItemMeta(meta);
		return item;
	}
}
