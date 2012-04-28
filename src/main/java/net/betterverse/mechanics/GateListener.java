package net.betterverse.mechanics;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class GateListener implements Listener {

	private Mechanics plugin;
	private Set<Block> leStuff = new HashSet<Block>();

	public GateListener(Mechanics plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPiston(BlockPistonExtendEvent event) {
		for(Block block : event.getBlocks()) {
			if(leStuff.contains(block))
				event.setCancelled(true);
		}
	}
	@EventHandler
	public void onExplode(EntityExplodeEvent event) {
		for(Block block : event.blockList()) {
			if(leStuff.contains(block))
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (event.getLine(0).equals("[Gate]") && !event.getPlayer().hasPermission("mechanics.gate")) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to create gates!");
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getClickedBlock() == null) {
			return;
		}
		if (!(event.getClickedBlock().getType() == Material.SIGN)) {
			return;
		}
		Sign sign = (Sign) event.getClickedBlock().getState();
		String secondLine = sign.getLine(1);
		if (!(secondLine.equals("[Gate]"))) {
			return;
		}
		org.bukkit.material.Sign ss = (org.bukkit.material.Sign) sign.getData();

		Block block = event.getClickedBlock();
		Material gateMat = block.getRelative(ss.getAttachedFace()).getType();
		Set<Block> base = new HashSet<Block>();
		while (block.getRelative(ss.getAttachedFace()).getType() == gateMat) {
			base.add(block);
			block = block.getRelative(ss.getAttachedFace());
		}
		boolean open = true;
		for (Block bblock : base) {
			if (bblock.getRelative(BlockFace.DOWN).getType() == gateMat) {
				open = false;
				break;
			}
		}
		if (open) {
			for (Block bblock : base) {
				Block below = bblock.getRelative(BlockFace.DOWN);
				while (below.getType() == Material.AIR) {
					below.setType(gateMat);
					below = below.getRelative(BlockFace.DOWN);
					leStuff.add(below);
				}
			}
		} else {
			for (Block bblock : base) {
				Block below = bblock.getRelative(BlockFace.DOWN);
				while (below.getType() == gateMat) {
					below.setType(Material.AIR);
					below = below.getRelative(BlockFace.DOWN);
					leStuff.remove(below);
				}
			}
		}
	}
	
	public void shutdown() {
		for(Block block : leStuff) {
			block.setType(Material.AIR);
		}
	}
}
