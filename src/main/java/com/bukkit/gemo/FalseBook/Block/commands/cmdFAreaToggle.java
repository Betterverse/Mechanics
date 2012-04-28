package com.bukkit.gemo.FalseBook.Block.commands;

import com.bukkit.gemo.FalseBook.Block.Areas.Area;
import com.bukkit.gemo.FalseBook.Block.FalseBookBlockCore;
import com.bukkit.gemo.FalseBook.Block.Mechanics.BlockMechanicHandler;
import com.bukkit.gemo.FalseBook.Block.Mechanics.MechanicArea;
import com.bukkit.gemo.commands.Command;
import com.bukkit.gemo.utils.ChatUtils;
import com.bukkit.gemo.utils.UtilPermissions;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class cmdFAreaToggle extends Command
{
  public cmdFAreaToggle(String pluginName, String syntax, String arguments, String node)
  {
    super(pluginName, syntax, arguments, node);
    this.description = "Toggle options";
  }

  public void execute(String[] args, CommandSender sender)
  {
    if ((sender instanceof Player))
    {
      Player player = (Player)sender;

      if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.area")) {
        ChatUtils.printError(player, this.pluginName, "You are not allowed to use this command.");
        return;
      }

    }

    MechanicArea mechanic = (MechanicArea)FalseBookBlockCore.getInstance().getMechanicHandler().getMechanic("AREA");

    String areaName = args[1];

    if (args[0].equalsIgnoreCase("AutoSave")) {
      boolean f = false;
      for (int i = mechanic.getAreas().size() - 1; i >= 0; i--) {
        if (((Area)mechanic.getAreas().get(i)).getAreaName().equalsIgnoreCase(areaName)) {
          ((Area)mechanic.getAreas().get(i)).setAutoSave(!((Area)mechanic.getAreas().get(i)).isAutoSave());
          mechanic.saveAreas(areaName, true);
          ChatUtils.printSuccess(sender, this.pluginName, "Changed Autosave from Area '" + areaName + "' to: " + ((Area)mechanic.getAreas().get(i)).isAutoSave());
          f = true;
        }
      }
      if (!f)
        ChatUtils.printError(sender, this.pluginName, "Area '" + areaName + "' not found!");
    } else if (args[0].equalsIgnoreCase("protect")) {
      boolean f = false;
      for (int i = mechanic.getAreas().size() - 1; i >= 0; i--) {
        if (((Area)mechanic.getAreas().get(i)).getAreaName().equalsIgnoreCase(areaName)) {
          ((Area)mechanic.getAreas().get(i)).setProtect(!((Area)mechanic.getAreas().get(i)).isProtect());
          mechanic.saveAreas(areaName, true);
          ChatUtils.printSuccess(sender, this.pluginName, "Changed Protection from Area '" + areaName + "' to: " + ((Area)mechanic.getAreas().get(i)).isProtect());
          f = true;
        }
      }
      if (!f)
        ChatUtils.printError(sender, this.pluginName, "Area '" + areaName + "' not found!");
    } else if (args[0].equalsIgnoreCase("interact")) {
      boolean f = false;
      for (int i = mechanic.getAreas().size() - 1; i >= 0; i--) {
        if (((Area)mechanic.getAreas().get(i)).getAreaName().equalsIgnoreCase(areaName)) {
          ((Area)mechanic.getAreas().get(i)).setInteractBlocked(!((Area)mechanic.getAreas().get(i)).isInteractBlocked());
          mechanic.saveAreas(areaName, true);
          ChatUtils.printSuccess(sender, this.pluginName, "Changed Interact-Protection from Area '" + areaName + "' to: " + ((Area)mechanic.getAreas().get(i)).isInteractBlocked());
          f = true;
        }
      }
      if (!f)
        ChatUtils.printError(sender, this.pluginName, "Area '" + areaName + "' not found!");
    } else {
      ChatUtils.printError(sender, this.pluginName, "Wrong syntax!");
      ChatUtils.printInfo(sender, this.pluginName, ChatColor.GRAY, getHelpMessage());
    }
  }
}