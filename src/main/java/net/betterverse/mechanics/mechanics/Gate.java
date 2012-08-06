package net.betterverse.mechanics.mechanics;

import java.util.HashSet;
import java.util.Set;


import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Sign;

public class Gate extends Mechanic {
    private Set<Integer> allowed = new HashSet<Integer>();

    public Gate() {
        super("[Gate]");
    }

    @Override
    public boolean canCreate(Player player) {
        return player.hasPermission("mechanics.gate");
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }

    @Override
    public void activate(Player player, Block block) {
        block = block.getRelative(((Sign) block).getAttachedFace());
        boolean found = false;
        for (BlockFace posi : pos) {
            Block second = findGateStart(block.getRelative(posi));
            if (second == null) {
                found = true;
                continue;
            }
            Set<Block> partBlocks = getUpperPart(second);
            for (Block bblock : partBlocks) {
                switchDownTypes(bblock);
            }
        }
        if (found) {
            player.sendMessage(ChatColor.GREEN + "Gate toggled!");
        } else {
            player.sendMessage(ChatColor.RED + "No gate found!");
        }
    }

    @Override
    public String getName() {
        return "Gate";
    }

    private Block findGateStart(Block block) {
        Location toSearch = block.getLocation();
        int startY = block.getY() + 1;
        int maxY = block.getWorld().getMaxHeight();
        while (startY <= maxY) {
            toSearch.setY(startY + 1);
            if (canBeGate(toSearch.getBlock().getType())) {
                return toSearch.getBlock();
            } else {
                if (block.getType() != Material.AIR) {
                    return null;
                }
            }
            startY++;
        }
        return null;
    }

    private Set<Block> getUpperPart(Block second) {
        BlockFace good = null;
        for (BlockFace ch : pos) {
            if (second.getType() == second.getRelative(ch).getType()) {
                good = ch;
            }
        }
        if (good == null) {
            return null;
        }

        Set<Block> toRet = new HashSet<Block>();
        toRet.add(second);
        Block rely = second.getRelative(good);
        while (rely.getType() == second.getType()) {
            toRet.add(rely);
            rely = rely.getRelative(good);
        }
        return toRet;
    }

    private void switchDownTypes(Block block) {
        boolean open = block.getType() == block.getRelative(BlockFace.DOWN).getType();
        Material orig = block.getType();
        if (open) {
            block = block.getRelative(BlockFace.DOWN);
            while (block.getType() == Material.AIR && block.getY() >= 1) {
                block.setType(orig);
                block = block.getRelative(BlockFace.DOWN);
            }
        } else {
            block = block.getRelative(BlockFace.DOWN);
            while (block.getType() == orig && block.getY() >= 1) {
                block.setType(Material.AIR);
                block = block.getRelative(BlockFace.DOWN);
            }
        }
    }

    private boolean canBeGate(Material type) {
        return allowed.contains(type.getId());
    }
}
