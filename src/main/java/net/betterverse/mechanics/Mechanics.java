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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Mechanics extends JavaPlugin implements Listener {
    private final Map<String, Mechanic> mechanics = new HashMap<String, Mechanic>();

    @Override
    public void onDisable() {
        log(toString() + " disabled.");
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        mechanics.put("[Bridge]", new Bridge());
        mechanics.put("[Door]", new Door());
        mechanics.put("[Lift]", new Lift("[Lift]"));
        mechanics.put("[Lift Up]", new Lift("[Lift Up]"));
        mechanics.put("[Lift Down]", new Lift("[Lift Down]"));
        mechanics.put("[Gate]", new Gate());

        log(toString() + " enabled.");
    }

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

    private void log(String message) {
        getServer().getLogger().log(Level.INFO, "[Mechanics] " + message);
    }
}
