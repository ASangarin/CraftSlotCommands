package com.asangarin.csc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class CraftSlotCommands extends JavaPlugin implements Listener
{
	public static CraftSlotCommands plugin;
	private CraftSlotItemsListener csil;
	
	@Override
    public void onEnable() {
		plugin = this;
		saveDefaultConfig();
		CSCCommand command = new CSCCommand();
		getCommand("craftslotcommands").setExecutor(command);
		getCommand("craftslotcommands").setTabCompleter(command);;
        getServer().getPluginManager().registerEvents(this, this);
        if(getConfig().getBoolean("items-enabled"))
        	getServer().getPluginManager().registerEvents(csil = new CraftSlotItemsListener(getConfig()), this);
    }
	
	@Override
	public void onDisable() {
		for(Player p : Bukkit.getOnlinePlayers())
			csil.playerLeaveFunction(p);
	}

	private void reload() {
		reloadConfig();
		csil.reload(getConfig());
	}
	
	@EventHandler
    public void inventoryClick(InventoryClickEvent e) {
    	if(!(e.getInventory() instanceof CraftingInventory) || e.getInventory().getSize() != 5 || e.getSlot() > 5 || e.getInventory().getType() != InventoryType.CRAFTING || e.getSlotType() == SlotType.ARMOR || e.getSlotType() == SlotType.CONTAINER || e.getSlotType() == SlotType.FUEL || e.getSlotType() == SlotType.OUTSIDE || e.getSlotType() == SlotType.QUICKBAR) return;
    	e.setCancelled(true);
    	
    	String cmd = getConfig().getString("crafting-slot." + e.getSlot());
    	if(cmd == null || cmd.isEmpty()) return;
    	if(e.getWhoClicked() == null) return;
    	
    	if(cmd.startsWith("*")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.substring(1));
    	else Bukkit.dispatchCommand(e.getWhoClicked(), cmd);
    }
	
	public class CSCCommand implements CommandExecutor, TabCompleter {
		@Override
	    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if(sender.hasPermission("csc.admin")) {
				if(args.length > 0 && args[0].equalsIgnoreCase("reload")) {
					CraftSlotCommands.plugin.reload();
					sender.sendMessage(ChatColor.AQUA + "[" + ChatColor.BLUE + "CraftSlotCommands" + ChatColor.AQUA + "] " + ChatColor.GREEN + "Successfully reloaded.");
				}
				else sender.sendMessage(ChatColor.AQUA + "[" + ChatColor.BLUE + "CraftSlotCommands" + ChatColor.AQUA + "] " + ChatColor.GREEN + "Version 2.0");
			}
			else sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
				
	        return true;
	    }

		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
			List<String> list = new ArrayList<String>();
			if(args.length == 1) list.add("reload");
			return list;
		}
	}
}
