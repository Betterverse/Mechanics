package net.betterverse.mechanics.mechs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.betterverse.mechanics.Mechanic;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Sign;


public class Door extends Mechanic{

	private List<BlockFace> pos = new ArrayList<BlockFace>();

	public Door() {
		super("Door");
		pos.add(BlockFace.SOUTH);
		pos.add(BlockFace.NORTH);
		pos.add(BlockFace.WEST);
		pos.add(BlockFace.EAST);
	}

	@Override
	public boolean canCreate(Player player) {
		return player.hasPermission("mechanics.door");
	}

	@Override
	public boolean canUse(Player player) {
		return true;
	}

	@Override
	public void activate(Player player, Block block) {
		BlockFace toCheck = ((Sign) block.getState().getData()).getAttachedFace();
		Block against = block.getRelative(toCheck).getRelative(BlockFace.DOWN);
		for (BlockFace posi : pos) {
			if (posi == toCheck && posi.getOppositeFace() == toCheck) {
				continue;
			}
			Set<Block> entirePart = getStart(against, posi);
			for (Block st : entirePart) {
				switchStart(st);
			}
		}
	}

	@Override
	public String getName() {
		return "Door";
	}

	private Set<Block> getStart(Block against, BlockFace posi) {
		Set<Block> toRet = new HashSet<Block>();
		Material orig = against.getType();
		while (against.getType() == orig) {
			toRet.add(against);
			against = against.getRelative(posi);
		}
		return toRet;
	}

	private void switchStart(Block st) {
		boolean create = st.getRelative(BlockFace.UP).getType() == Material.AIR;
		Material orig = st.getType();
		if(create) {
			st = st.getRelative(BlockFace.UP);
			while(st.getType() == Material.AIR) {
				st.setType(orig);
				st = st.getRelative(BlockFace.UP);
			}
		} else {
			st = st.getRelative(BlockFace.UP);
			while(st.getType() == orig) {
				st.setType(Material.AIR);
				st = st.getRelative(BlockFace.UP);
			}
		}
	}
}
