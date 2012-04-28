package net.betterverse.mechanics;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;



public class LiftListener implements Listener{

	public LiftListener(Mechanics aThis) {
		
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if(event.getLine(1).equals("[Lift") && !event.getPlayer().hasPermission("mechanics.lifts")) {
			event.getPlayer().sendMessage(ChatColor.RED+"You are not allowed to create lifts!");
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getClickedBlock() == null)
			return;
		if(!(event.getClickedBlock().getType() == Material.SIGN)) {
			return;
		}
		Sign sign = (Sign) event.getClickedBlock().getState();
		String secondLine = sign.getLine(1);
		int direction = 1;
		if(secondLine.equals("[Lift Down]"))
			direction = -1;
		else if(!(secondLine.equals("[Lift Up]"))) {
			return;
		}
		if(secondLine.contains("[Lift ")) {
			Location location = event.getClickedBlock().getLocation();
			for(int y = location.getBlockY() ; event.getClickedBlock().getWorld().getBlockAt(event.getClickedBlock().getX(), y, event.getClickedBlock().getZ()).getType()==Material.SIGN; y+=direction) {
				Sign ss = (Sign) event.getClickedBlock().getWorld().getBlockAt(event.getClickedBlock().getX(), y, event.getClickedBlock().getZ()).getState();
				if(ss.getLine(1).contains("[Lift")) {
					Location player = event.getPlayer().getLocation();
					player.setY(y);
					event.getPlayer().teleport(player);
					event.getPlayer().sendMessage(ChatColor.GREEN+"Lift used!");
					break;
				}
				if(y>=event.getClickedBlock().getWorld().getMaxHeight() || y==-1) {
					event.getPlayer().sendMessage(ChatColor.RED+"No valid lift found!");
					break;
				}
			}
		}
	}

}
