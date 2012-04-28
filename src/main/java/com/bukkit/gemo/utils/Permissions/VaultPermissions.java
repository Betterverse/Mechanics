package com.bukkit.gemo.utils.Permissions;

import java.util.List;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

public class VaultPermissions extends SuperPermsPermissions
{
  private Permission perms = null;

  public VaultPermissions() {
    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Vault");

    if (plugin == null) {
      return;
    }

    RegisteredServiceProvider rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
    this.perms = ((Permission)rsp.getProvider());
  }

  public boolean isActive()
  {
    return this.perms != null;
  }

  public List<String> getGroups(Player player)
  {
    List groups = super.getGroups(player);

    if (this.perms == null) {
      return groups;
    }

    String[] playerGroups = this.perms.getPlayerGroups(player);
    if (playerGroups != null) {
      for (String group : playerGroups)
        groups.add(group);
    }
    return groups;
  }

  public List<String> getGroups(String playerName, String worldName)
  {
    List groups = super.getGroups(playerName, worldName);

    if (this.perms == null) {
      return groups;
    }

    String[] playerGroups = this.perms.getPlayerGroups(worldName, playerName);
    if (playerGroups != null) {
      for (String group : playerGroups)
        groups.add(group);
    }
    return groups;
  }

  public boolean permission(Player player, String node)
  {
    return this.perms.playerHas(player, node);
  }
}