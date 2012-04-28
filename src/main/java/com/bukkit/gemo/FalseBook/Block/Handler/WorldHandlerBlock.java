package com.bukkit.gemo.FalseBook.Block.Handler;

import com.bukkit.gemo.FalseBook.Block.World.FBWorldBlock;
import com.bukkit.gemo.FalseBook.World.FBWorld;
import com.bukkit.gemo.FalseBook.World.WorldHandler;
import java.util.HashMap;

public class WorldHandlerBlock extends WorldHandler
{
  protected FBWorld addWorld(String worldName)
  {
    removeWorld(worldName);
    FBWorldBlock thisWorld = new FBWorldBlock(worldName);
    thisWorld.loadSettings();
    this.worldList.put(worldName, thisWorld);
    return thisWorld;
  }

  public FBWorld getWorld(String worldName)
  {
    if (super.hasWorld(worldName)) {
      return (FBWorld)this.worldList.get(worldName);
    }
    return addWorld(worldName);
  }
}