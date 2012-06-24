package net.betterverse.mechanics;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public abstract class Mechanic {
	private String text;
	
	public Mechanic(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public abstract boolean canCreate(Player player);
	
	public abstract boolean canUse(Player player);
	
	public abstract void activate(Player player, Block block);
	
	public abstract String getName();
	
	public void shutdown() {}
}
