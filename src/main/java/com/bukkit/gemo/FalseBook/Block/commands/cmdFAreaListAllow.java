package com.bukkit.gemo.FalseBook.Block.commands;

import com.bukkit.gemo.FalseBook.Block.Areas.Area;
import com.bukkit.gemo.FalseBook.Block.Areas.AreaBlockType;
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

public class cmdFAreaListAllow extends Command
{
  public cmdFAreaListAllow(String pluginName, String syntax, String arguments, String node)
  {
    super(pluginName, syntax, arguments, node);
    this.description = "List all allowed blocktypes of an area";
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

    String areaName = args[0];

    boolean f = false;
    for (int i = mechanic.getAreas().size() - 1; i >= 0; i--) {
      if (((Area)mechanic.getAreas().get(i)).getAreaName().equalsIgnoreCase(areaName)) {
        String txt = "";
        if (((Area)mechanic.getAreas().get(i)).getAllowedBlocks().size() == 0) {
          ChatUtils.printLine(sender, ChatColor.GOLD, "All blocktypes are allowed in '" + areaName + "'");
        } else {
          for (int j = 0; j < ((Area)mechanic.getAreas().get(i)).getAllowedBlocks().size(); j++) {
            txt = txt + ((AreaBlockType)((Area)mechanic.getAreas().get(i)).getAllowedBlocks().get(j)).getTypeID() + ":" + ((AreaBlockType)((Area)mechanic.getAreas().get(i)).getAllowedBlocks().get(j)).getData();
            if (j < ((Area)mechanic.getAreas().get(i)).getAllowedBlocks().size() - 1) {
              txt = txt + ", ";
            }
          }
          ChatUtils.printLine(sender, ChatColor.GOLD, "Allowed blocktypes in '" + areaName + "':");
          ChatUtils.printLine(sender, ChatColor.GRAY, txt);
        }
        f = true;
      }
    }
    if (!f)
      ChatUtils.printError(sender, this.pluginName, "Area '" + areaName + "' not found!");
  }
}