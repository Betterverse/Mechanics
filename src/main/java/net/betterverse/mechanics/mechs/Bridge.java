package net.betterverse.mechanics.mechs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.betterverse.mechanics.Mechanic;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Sign;


public class Bridge extends Mechanic{
	private List<BlockFace> pos = new ArrayList<BlockFace>();
	
	public Bridge() {
		super("Bridge");
		pos.add(BlockFace.SOUTH);
		pos.add(BlockFace.NORTH);
		pos.add(BlockFace.WEST);
		pos.add(BlockFace.EAST);
	}

	@Override
	public boolean canCreate(Player player) {
		return player.hasPermission("mechanics.bridge");
	}

	@Override
	public boolean canUse(Player player) {
		return true;
	}

	@Override
	public void activate(Player player, Block block) {
		BlockFace toCheck = ((Sign) block.getState().getData()).getAttachedFace();
		block = block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
		block = block.getRelative(toCheck);
		for(BlockFace posi : pos) {
			if(posi == toCheck && posi.getOppositeFace() == toCheck) continue;
			Set<Block> entirePart = getStart(block.getRelative(posi), posi);
			for(Block st : entirePart) {
				switchStart(st, posi);
			}
		}
		player.sendMessage(ChatColor.GREEN+"Switched bridge!");
	}

	@Override
	public String getName() {
		return "Bridge";
	}

	private Set<Block> getStart(Block relative, BlockFace posi) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void switchStart(Block st, BlockFace posi) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
