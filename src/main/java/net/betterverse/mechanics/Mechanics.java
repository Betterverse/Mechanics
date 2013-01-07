package net.betterverse.mechanics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.betterverse.mechanics.mechanics.Bridge;
import net.betterverse.mechanics.mechanics.Door;
import net.betterverse.mechanics.mechanics.Gate;
import net.betterverse.mechanics.mechanics.Lift;
import net.betterverse.mechanics.mechanics.Mechanic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Mechanics extends JavaPlugin implements Listener {
    private final Map<String, Mechanic> mechanics = new HashMap<String, Mechanic>();
    int turn = 0;

    @Override
    public void onDisable() {
        log(toString() + " disabled.");
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        mechanics.put("[Bridge]", new Bridge("[Bridge]"));
        mechanics.put("[Bridge End]", new Bridge("[Bridge End]"));
        mechanics.put("[Door Up]", new Door("[Door Up]"));
        mechanics.put("[Door Down]", new Door("[Door Down]"));
        mechanics.put("[Door End]", new Door("[Door End]"));
        mechanics.put("[Lift]", new Lift("[Lift]"));
        mechanics.put("[Lift Up]", new Lift("[Lift Up]"));
        mechanics.put("[Lift Down]", new Lift("[Lift Down]"));
        mechanics.put("[Gate]", new Gate());

        log(toString() + " enabled.");
    }

    //Prints some of the plugin info to the server
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getDescription().getName() + " v" + getDescription().getVersion() + " [Written by: ");
        List<String> authors = getDescription().getAuthors();
        for (int i = 0; i < authors.size(); i++) {
            builder.append(authors.get(i) + (i + 1 != authors.size() ? ", " : ""));
        }
        builder.append("]");

        return builder.toString();
    }
    
    //This handles calling the correct mechanic based in the sing for redstone
    @EventHandler
    public void onRedStoneEvent( BlockRedstoneEvent event){
        Block clicked = event.getBlock();
    	if( clicked.isBlockPowered() && turn == 0){
		    Mechanic reg = getMechanicFromBlock(clicked);
		    if (reg == null) {
		        return;
		    }
		    Bukkit.getServer().broadcastMessage("Block: " + clicked);
		    Bukkit.getServer().broadcastMessage("Reg: " + reg);
		    reg.activateRedstone(clicked);
		    turn = 1;
    	}
    	else if( clicked.isBlockPowered() && turn == 1)
    	{
    		turn = 0;
    	}
    	else{
    		Mechanic reg = getMechanicFromBlock(clicked);
		    if (reg == null) {
		        return;
		    }
		    reg.activateRedstone(clicked);
    	}
    	
    }
    //This handles calling the correct mechanic based on the sign for player clicks
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        Mechanic reg = getMechanicFromBlock(clicked);
        if (reg == null) {
            return;
        }
        if (reg.canUse(event.getPlayer())) {
            reg.activate(event.getPlayer(), clicked);
        } else {
            event.getPlayer().sendMessage(ChatColor.RED + "Could not use " + ChatColor.AQUA + reg.getName() + ChatColor.RED + "!");
        }
    }

    //This handles when the player makes a sign trying to make a mechanic
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String secondLine = event.getLine(1);
        if (secondLine == null) {
            return;
        }
        Mechanic mechanic = mechanics.get(secondLine);
        if (mechanic == null) {
            return;
        }
        if (mechanic.canCreate(event.getPlayer())) {
            event.getPlayer().sendMessage(ChatColor.GREEN + "Created " + ChatColor.AQUA + mechanic.getName() + ChatColor.GREEN + "!");
        } else {
            event.getPlayer().sendMessage(ChatColor.RED + "Could not create " + ChatColor.AQUA + mechanic.getName() + ChatColor.RED + "!");
        }
    }
    
    //Pulls the mechanics based on the sign
    public Mechanic getMechanicFromBlock(Block block) {
        if (block == null) {
            return null;
        }
        if (!(block.getType() == Material.SIGN || block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
            return null;
        }
        Sign sign = ((Sign) block.getState());
        String secondLine = sign.getLine(1);
        if (secondLine == null) {
            return null;
        }
        return mechanics.get(secondLine);
    }

    //Logs messages from the mechanics
    private void log(String message) {
        getServer().getLogger().log(Level.INFO, "[Mechanics] " + message);
    }
    
    
    //These all deal with their respective events, in accordance with the gates being uninteractable when on.
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
    	Mechanic gate = mechanics.get("[Gate]");
    	boolean canBreak = ((Gate) gate).canBreak(event.getBlock());
    	if(!canBreak){
    		event.getPlayer().sendMessage(ChatColor.RED + "You cannot remove that block!");
    		event.setCancelled(true);
    	}
    }
    @EventHandler
    public void onBlockPiston(BlockPistonRetractEvent event){
    	Mechanic gate = mechanics.get("[Gate]");
    	getServer().broadcastMessage("Block: " + event.getRetractLocation());
    	boolean canBreak = ((Gate) gate).canPistonRetract(event.getRetractLocation());
    	if(!canBreak){
    		event.setCancelled(true);
    	}
    }
    @EventHandler
    public void onBlockPiston(BlockPistonExtendEvent event){
    	Mechanic gate = mechanics.get("[Gate]");
    	List<Block> blocks = event.getBlocks();
    	for( int i=0; i < blocks.size(); i++){
    		boolean canBreak = ((Gate) gate).canBreak(blocks.get(i));
    		if(!canBreak){
    			event.setCancelled(true);
    			return;
    		}
    	}
    }
    public void onTnTExplosion(EntityExplodeEvent event){
    	Mechanic gate = mechanics.get("[Gate]");
    	List<Block> blocks = event.blockList();
    	for( int i=0; i < blocks.size(); i++){
    		boolean canBreak = ((Gate) gate).canBreak(blocks.get(i));
    		if(!canBreak){
    			event.setCancelled(true);

    		}
    	}
    }
    
}
