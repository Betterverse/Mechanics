package com.bukkit.gemo.utils.Permissions.System;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class FBPermissionHandler
{
  public static HashMap<String, WorldPermissions> worldList = new HashMap();

  private static void addWorld(String worldName, WorldPermissions permissions)
  {
    worldList.put(worldName, permissions);
  }

  private static WorldPermissions getWorldForPlayer(Player player)
  {
    WorldPermissions wPermissions = (WorldPermissions)worldList.get(player.getWorld().getName());
    if (wPermissions == null) {
      wPermissions = new WorldPermissions();
      addWorld(player.getWorld().getName(), wPermissions);
    }
    return wPermissions;
  }

  public static boolean hasPermission(Player player, String permissionNode, boolean forceUpdate)
  {
    return getWorldForPlayer(player).hasPermission(player, permissionNode, forceUpdate);
  }

  public static void removePlayer(Player player)
  {
    for (Map.Entry entry : worldList.entrySet())
      ((WorldPermissions)entry.getValue()).removePlayer(player.getName());
  }
}