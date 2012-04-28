package com.bukkit.gemo.utils;

import com.bukkit.gemo.FalseBook.Core.FalseBookCore;
import com.bukkit.gemo.utils.Permissions.IPermissions;
import com.bukkit.gemo.utils.Permissions.System.FBPermissionHandler;
import com.bukkit.gemo.utils.Permissions.VaultPermissions;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class UtilPermissions
{
  private static IPermissions permissions;
  private static Server bukkitServer;

  public Server getServer()
  {
    return bukkitServer;
  }

  public void setServer(Server bukkitServer)
  {
    bukkitServer = bukkitServer;
  }

  public UtilPermissions(Server bukkitServer)
  {
    setServer(bukkitServer);
    getSecurityPlugin();
  }

  private static Plugin getPlugin(String name, boolean enable)
  {
    Plugin temp = FalseBookCore.getMCServer().getPluginManager().getPlugin(name);
    if (temp == null) {
      return null;
    }

    if ((enable) && (!temp.isEnabled())) {
      bukkitServer.getPluginManager().enablePlugin(temp);
    }
    return temp;
  }

  private static boolean getSecurityPlugin()
  {
    if (getPlugin("Vault", false) != null) {
      permissions = new VaultPermissions();
      FalseBookCore.printInConsole("using Vault for Permissions!");
      return true;
    }
    return false;
  }

  public static boolean playerCanUseCommand(Player player, String command)
  {
    return FBPermissionHandler.hasPermission(player, command, false);
  }

  public static boolean criticalPlayerCanUseCommand(Player player, String permissionNode)
  {
    return FBPermissionHandler.hasPermission(player, permissionNode, true);
  }

  public static boolean forcePermissionUpdate(Player player, String permissionNode)
  {
    return FBPermissionHandler.hasPermission(player, permissionNode, true);
  }

  public static boolean getPermissionFromPermissionPlugin(Player player, String permissionNode)
  {
    if (player == null) {
      return false;
    }
    if (permissions == null) {
      getSecurityPlugin();
    }
    if (permissions != null) {
      return (permissions.permission(player, permissionNode)) || (player.isOp());
    }
    return player.isOp();
  }

  public static boolean isPlayerInGroup(Player player, String groupName, String worldName)
  {
    if (permissions == null) {
      getSecurityPlugin();
    }
    if (permissions != null) {
      List<String> groups = permissions.getGroups(FalseBookCore.getPlayer(player.getName()));
      for (String grp : groups)
        if (grp.equalsIgnoreCase(groupName))
          return true;
    }
    return false;
  }

  public static boolean isPlayerInGroupWithoutInhetirance(Player player, String groupName, String worldName)
  {
    if (permissions == null) {
      getSecurityPlugin();
    }
    if (permissions != null) {
      List groups = permissions.getGroups(FalseBookCore.getPlayer(player.getName()));
      if (groups.size() > 0)
        return ((String)groups.get(0)).equalsIgnoreCase(groupName);
    }
    return false;
  }

  public static String getGroupName(Player player)
  {
    return getGroupName(player.getName(), ((World)bukkitServer.getWorlds().get(0)).getName());
  }

  public static String getGroupName(String playername, String worldName)
  {
    if (permissions == null) {
      getSecurityPlugin();
    }
    if (permissions != null) {
      List groups = permissions.getGroups(playername, worldName);
      if (groups.size() > 0) {
        return (String)groups.get(0);
      }
      return "";
    }
    return "";
  }
}