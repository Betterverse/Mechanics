package com.bukkit.gemo.FalseBook.Core;

import com.bukkit.gemo.FalseBook.Block.Config.ConfigHandler;
import com.bukkit.gemo.FalseBook.Block.FalseBookBlockCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class FalseBookCoreWorldListener
  implements Listener
{
  private FalseBookCore plugin;

  public FalseBookCoreWorldListener(FalseBookCore instance)
  {
    this.plugin = instance;
  }
  @EventHandler
  public void onWorldLoad(WorldLoadEvent event) {
    if (this.plugin.getServer().getPluginManager().getPlugin("FalseBookBlock") != null) {
      FalseBookBlockCore blockCore = (FalseBookBlockCore)this.plugin.getServer().getPluginManager().getPlugin("FalseBookBlock");
      if (blockCore.plugin.isEnabled()) {
        ConfigHandler.getOrCreateSettings(event.getWorld().getName());
      }
    }
  }
}