package com.bukkit.gemo.FalseBook.Block.commands;

import com.bukkit.gemo.FalseBook.Block.Areas.Area;
import com.bukkit.gemo.FalseBook.Block.Areas.AreaSelection;
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

public class cmdFArea extends Command
{
  public cmdFArea(String pluginName, String syntax, String arguments, String node)
  {
    super(pluginName, syntax, arguments, node);
    this.description = "Save area";
  }

  public void execute(String[] args, CommandSender sender)
  {
    if (!(sender instanceof Player)) {
      ChatUtils.printError(sender, this.pluginName, "This is only an ingame command.");
      return;
    }

    Player player = (Player)sender;

    if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.area")) {
      ChatUtils.printError(player, this.pluginName, "You are not allowed to use this command.");
      return;
    }

    MechanicArea mechanic = (MechanicArea)FalseBookBlockCore.getInstance().getMechanicHandler().getMechanic("AREA");

    boolean f = false;
    String aName = args[0];

    for (int i = 0; i < mechanic.getAreas().size(); i++) {
      if (((Area)mechanic.getAreas().get(i)).getAreaName().equalsIgnoreCase(aName)) {
        f = true;
      }

    }

    boolean selectionFound = false;
    for (int i = 0; i < mechanic.getSelections().size(); i++) {
      if (((AreaSelection)mechanic.getSelections().get(i)).player.getName().equalsIgnoreCase(player.getName())) {
        selectionFound = true;
        if ((((AreaSelection)mechanic.getSelections().get(i)).selP1 != null) && (((AreaSelection)mechanic.getSelections().get(i)).selP2 != null)) {
          Area newArea = new Area(player.getWorld(), aName, ((AreaSelection)mechanic.getSelections().get(i)).selP1, ((AreaSelection)mechanic.getSelections().get(i)).selP2);
          newArea.initArea();
          mechanic.getAreas().add(newArea);
          mechanic.saveAreas(aName, true);

          ChatUtils.printSuccess(player, this.pluginName, "Area saved as: '" + aName + "'");
          if (f) {
            ChatUtils.printInfo(player, this.pluginName, ChatColor.GRAY, "WARNING: Area created, but there are multiple areas with that name!");
          }
          return;
        }
        ChatUtils.printError(player, this.pluginName, "Please define an area first! (Rightclick with Selectiontool.)");
        return;
      }
    }

    if (!selectionFound)
      ChatUtils.printError(player, this.pluginName, "Please define an area first! (Rightclick with Selectiontool.)");
  }
}