package net.betterverse.mechanics.mechs;

import net.betterverse.mechanics.Mechanic;
import net.betterverse.mechanics.Mechanics;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Lift extends Mechanic{
	
	public Lift(String text) {
		super(text);
	}

	@Override
	public boolean canCreate(Player player) {
		return player.hasPermission("mechanics.lift");
	}

	@Override
	public boolean canUse(Player player) {
		return true;
	}
	
	
	@Override
	public void activate(Player player, Block block) {
		int direction = 0;
		if(getText().equals("[Lift]")) {
			player.sendMessage(ChatColor.RED+"You can't directly use this type of lift!");
			return;
		} else if(getText().equals("[Lift Up]")) {
			direction = 1;
		} else if(getText().equals("[Lift Down]")) {
			direction = -1;
		}
			int y = block.getY()+direction;
			int max = block.getWorld().getMaxHeight();
			Location loc = block.getLocation();
			while(y<=max && y>=1) {
				loc.setY(y);
				Mechanic mec = Mechanics.getMechanicFromBlock(block);
				if(mec!=null&&mec instanceof Lift) {
					Location a = player.getLocation().subtract(block.getLocation());
					player.teleport(loc.add(a));
					player.sendMessage(ChatColor.GREEN+"Lift used!");
					return;
				}
				y+=direction;
			}
	}

	@Override
	public String getName() {
		return "Lift";
	}

}
