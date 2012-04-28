package net.betterverse.mechanics;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;



public class Mechanics extends JavaPlugin{
	private GateListener gl;

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		gl = new GateListener(this);
		pm.registerEvents(gl, this);
		pm.registerEvents(new LiftListener(this), this);
	}

	@Override
	public void onDisable() {
		gl.shutdown();
	}

}
