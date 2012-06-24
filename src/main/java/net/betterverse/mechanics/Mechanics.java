package net.betterverse.mechanics;

import java.util.HashMap;
import java.util.Map;
import net.betterverse.mechanics.mechs.Gate;
import net.betterverse.mechanics.mechs.Lift;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class Mechanics extends JavaPlugin implements Listener{
	private static Map<String, Mechanic> registered = new HashMap<String, Mechanic>();

	@Override
	public void onDisable() {
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		
		
		registered.put("[Lift]", new Lift("[Lift]"));
		registered.put("[Lift Up]", new Lift("[Lift Up]"));
		registered.put("[Lift Down]", new Lift("[Lift Down]"));
		registered.put("[Gate]", new Gate(this));
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block clicked = event.getClickedBlock();
		Mechanic reg = getMechanicFromBlock(clicked);
		if(reg == null)
			return;
		if(!reg.canUse(event.getPlayer())) {
			event.getPlayer().sendMessage(ChatColor.RED+"Could not use "+ChatColor.AQUA+reg.getName()+ChatColor.RED+"!");
			return;
		}
		reg.activate(event.getPlayer(), clicked);
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		String secondLine = event.getLine(1);
		if(secondLine == null)
			return;
		Mechanic reg = registered.get(secondLine);
		if(reg == null)
			return;
		if(reg.canCreate(event.getPlayer())) {
			event.getPlayer().sendMessage(ChatColor.GREEN+"Created "+ChatColor.AQUA+reg.getName()+ChatColor.GREEN+"!");
		} else {
			event.getPlayer().sendMessage(ChatColor.RED+"Could not create "+ChatColor.AQUA+reg.getName()+ChatColor.RED+"!");
		}
	}
	
	public static Mechanic getMechanicFromBlock(Block block) {
		if(block == null)
			return null;
		if(!(block.getType() == Material.SIGN || block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
			return null;
		}
		Sign sign = ((Sign)block.getState());
		String secondLine = sign.getLine(1);
		if(secondLine == null)
			return null;
		return registered.get(secondLine);
	}

}
