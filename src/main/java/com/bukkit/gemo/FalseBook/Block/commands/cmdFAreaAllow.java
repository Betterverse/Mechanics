package com.bukkit.gemo.FalseBook.Block.commands;

import com.bukkit.gemo.FalseBook.Block.Areas.Area;
import com.bukkit.gemo.FalseBook.Block.Areas.AreaBlockType;
import com.bukkit.gemo.FalseBook.Block.FalseBookBlockCore;
import com.bukkit.gemo.FalseBook.Block.Mechanics.BlockMechanicHandler;
import com.bukkit.gemo.FalseBook.Block.Mechanics.MechanicArea;
import com.bukkit.gemo.commands.ExtendedCommand;
import com.bukkit.gemo.utils.ChatUtils;
import com.bukkit.gemo.utils.UtilPermissions;
import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class cmdFAreaAllow extends ExtendedCommand
{
  public cmdFAreaAllow(String pluginName, String syntax, String arguments, String node)
  {
    super(pluginName, syntax, arguments, node);
    this.description = "Allow specific blocktypes in areas";
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

    if (args.length > 0) {
      String areaName = "";
      for (int i = 0; i < args.length - 1; i++) {
        if (i > 0)
          areaName = areaName + " ";
        areaName = areaName + args[i];
      }
      String[] split = args[(args.length - 1)].split(":");
      int typeID = 0;
      byte typeData = 0;

      if (split.length == 1)
        try {
          typeID = Integer.valueOf(split[0]).intValue();
        } catch (Exception e) {
          ChatUtils.printError(sender, this.pluginName, "Wrong syntax! Use '/fareaallow <areaname> <ID>[:<SUB>]'");
          return;
        }
      else {
        try {
          typeID = Integer.valueOf(split[0]).intValue();
          typeData = Byte.valueOf(split[1]).byteValue();
        } catch (Exception e) {
          ChatUtils.printError(sender, this.pluginName, "Wrong syntax! Use '/fareaallow <areaname> <ID>[:<SUB>]'");
          return;
        }
      }

      AreaBlockType thisBlock = new AreaBlockType(typeID, typeData);
      boolean f = false;
      for (int i = mechanic.getAreas().size() - 1; i >= 0; i--) {
        if (((Area)mechanic.getAreas().get(i)).getAreaName().equalsIgnoreCase(areaName)) {
          if (!((Area)mechanic.getAreas().get(i)).isInAllowed(thisBlock))
          {
            ((Area)mechanic.getAreas().get(i)).getAllowedBlocks().add(thisBlock);
            ChatUtils.printSuccess(sender, this.pluginName, "Added " + typeID + ":" + typeData + " to toggleable Blocks!");
          }
          else {
            ((Area)mechanic.getAreas().get(i)).getAllowedBlocks().remove(((Area)mechanic.getAreas().get(i)).getInAllowed(thisBlock.getTypeID(), thisBlock.getData()));
            ChatUtils.printSuccess(sender, this.pluginName, "Removed " + typeID + ":" + typeData + " from toggleable Blocks!");
          }
          mechanic.saveAreas(((Area)mechanic.getAreas().get(i)).getAreaName(), true);
          f = true;
        }
      }
      if (!f)
        ChatUtils.printError(sender, this.pluginName, "Area '" + areaName + "' not found!");
    }
  }
}