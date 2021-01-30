package com.asangarin.csc;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class CraftSlotItemsListener implements Listener {
	private ItemStack i1, i2, i3, i4;
	
	public CraftSlotItemsListener(FileConfiguration config) {
		i1 = ItemBuilder.build(config.getConfigurationSection("slot-item.1"));
		i2 = ItemBuilder.build(config.getConfigurationSection("slot-item.2"));
		i3 = ItemBuilder.build(config.getConfigurationSection("slot-item.3"));
		i4 = ItemBuilder.build(config.getConfigurationSection("slot-item.4"));
	}
	
	@EventHandler
	public void inventoryClose(InventoryCloseEvent e) {
		if(e.getInventory() instanceof CraftingInventory && e.getInventory().getSize() == 5)
			removeItems(e.getView());
		
		Bukkit.getScheduler().runTaskLater(CraftSlotCommands.plugin, new Runnable() {
			@Override
			public void run() {
				if(e.getPlayer().getOpenInventory().getTopInventory() instanceof CraftingInventory &&
					e.getPlayer().getOpenInventory().getTopInventory().getSize() == 5)
					addItems(e.getPlayer().getOpenInventory());
			}
		}, 10);
	}
	
	@EventHandler
	public void playerJoin(PlayerJoinEvent e) {
		addItems(e.getPlayer().getOpenInventory());
	}
	@EventHandler
	public void playerLeave(PlayerQuitEvent e)
	{ playerLeaveFunction(e.getPlayer()); }
	public void playerLeaveFunction(Player p)
	{ removeItems(p.getOpenInventory()); }
	
	@EventHandler
	public void playerDeath(PlayerDeathEvent e) {
		e.getDrops().remove(i1); e.getDrops().remove(i2);
		e.getDrops().remove(i3); e.getDrops().remove(i4);
		e.getEntity().closeInventory(); playerLeaveFunction(e.getEntity());
	}
	
	@EventHandler
	public void playerMove(PlayerMoveEvent e) {
		if(!CraftSlotCommands.plugin.getConfig().getBoolean("experimental-pickup")) return;
		if(e.getFrom().toVector() == e.getTo().toVector()) return;
		Inventory inv = e.getPlayer().getInventory();
			for(Entity nearby : e.getPlayer().getNearbyEntities(0.5, 0.5, 0.5)) {
				if(!(nearby instanceof Item)) continue;
				Item item = (Item) nearby;
				if(item.getTicksLived() < 40) return;
				if(inv.firstEmpty() != -1) {
					pickup(item, e.getPlayer());
				}
				else if(inv.contains(item.getItemStack())) {
					if(!(inv.getItem(inv.first(item.getItemStack().getType())).getAmount() + item.getItemStack().getAmount() > 64)) {
						pickup(item, e.getPlayer());
					}
				}
			}
	}
	
	private void pickup(Item item, Player p) {
		item.setTicksLived(1);
		p.getInventory().addItem(item.getItemStack());
		p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2f, 1.4f);
		item.setVelocity(p.getLocation().toVector().subtract(item.getLocation().toVector()).normalize().add(new Vector(0, 0.1, 0)).multiply(0.5));
		Bukkit.getScheduler().runTaskLater(CraftSlotCommands.plugin, new Runnable() {
			@Override public void run() { item.remove(); }
		}, 8);
	}
	
	private void addItems(InventoryView inventory) {
		Inventory inv = inventory.getInventory(0);
		inv.setItem(1, i1); inv.setItem(2, i2); inv.setItem(3, i3); inv.setItem(4, i4);
		inventory.getPlayer().setCanPickupItems(true);
	}
	private void removeItems(InventoryView inventory) {
		Inventory inv = inventory.getInventory(0);
		inv.setItem(1, null); inv.setItem(2, null); inv.setItem(3, null); inv.setItem(4, null);
	}
	
	public void reload(FileConfiguration config) {
		i1 = ItemBuilder.build(config.getConfigurationSection("slot-item.1"));
		i2 = ItemBuilder.build(config.getConfigurationSection("slot-item.2"));
		i3 = ItemBuilder.build(config.getConfigurationSection("slot-item.3"));
		i4 = ItemBuilder.build(config.getConfigurationSection("slot-item.4"));
	}
}
