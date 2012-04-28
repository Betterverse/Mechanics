package com.bukkit.gemo.FalseBook.World;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.World;

public class WorldHandler
{
  protected HashMap<String, FBWorld> worldList = new HashMap();

  protected FBWorld addWorld(String worldName) {
    removeWorld(worldName);
    FBWorld thisWorld = new FBWorld(worldName);
    this.worldList.put(worldName, thisWorld);
    return thisWorld;
  }

  protected FBWorld removeWorld(String worldName) {
    return (FBWorld)this.worldList.remove(worldName);
  }

  public boolean hasWorld(String worldName) {
    return this.worldList.containsKey(worldName);
  }

  public boolean hasBukkitWorld(String worldName) {
    return getWorld(worldName).hasBukkitWorld();
  }

  public World getBukkitWorld(String worldName) {
    return getWorld(worldName).getBukkitWorld();
  }

  public FBWorld getWorld(String worldName) {
    if (hasWorld(worldName)) {
      return (FBWorld)this.worldList.get(worldName);
    }
    return addWorld(worldName);
  }

  public ArrayList<FBWorld> getAllWorlds()
  {
    ArrayList list = new ArrayList();
    list.addAll(this.worldList.values());
    return list;
  }
}