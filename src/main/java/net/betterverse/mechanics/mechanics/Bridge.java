package net.betterverse.mechanics.mechanics;

import java.util.HashSet;
import java.util.Set;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Sign;

public class Bridge extends Mechanic {

    public Bridge() {
        super("Bridge");
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
        for (BlockFace posi : pos) {
            if (posi == toCheck && posi.getOppositeFace() == toCheck) {
                continue;
            }
            Set<Block> entirePart = getStart(block.getRelative(posi), posi);
            for (Block st : entirePart) {
                switchStart(st, posi);
            }
        }
        player.sendMessage(ChatColor.GREEN + "Switched bridge!");
    }

    @Override
    public String getName() {
        return "Bridge";
    }

    private Set<Block> getStart(Block relative, BlockFace posi) {
        Set<Block> toRet = new HashSet<Block>();
        Material orig = relative.getType();
        while (relative.getType() == orig) {
            toRet.add(relative);
            relative = relative.getRelative(posi);
        }
        return toRet;
    }

    private void switchStart(Block st, BlockFace posi) {
        boolean create = st.getRelative(posi).getType() == Material.AIR;
        Material toSet = st.getType();
        st = st.getRelative(posi);
        if (create) {
            while (st.getType() == Material.AIR) {
                st.setType(toSet);
                st = st.getRelative(posi);
            }
        } else {
            while (st.getType() == toSet) {
                st.setType(Material.AIR);
                st = st.getRelative(posi);
            }
        }
    }
}
