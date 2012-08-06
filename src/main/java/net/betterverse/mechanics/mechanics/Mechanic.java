package net.betterverse.mechanics.mechanics;

import net.betterverse.mechanics.Mechanics;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public abstract class Mechanic {
    protected static final BlockFace[] pos = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };

    protected final Mechanics plugin;
    protected final String text;

    public Mechanic(String text) {
        plugin = (Mechanics) Bukkit.getServer().getPluginManager().getPlugin("Mechanics");
        this.text = text;
    }

    public abstract boolean canCreate(Player player);

    public abstract boolean canUse(Player player);

    public abstract void activate(Player player, Block block);

    public abstract String getName();
}
