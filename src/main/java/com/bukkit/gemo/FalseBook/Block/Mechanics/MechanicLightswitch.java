package com.bukkit.gemo.FalseBook.Block.Mechanics;

import com.bukkit.gemo.FalseBook.Block.Config.ConfigHandler;
import com.bukkit.gemo.FalseBook.Block.FalseBookBlockCore;
import com.bukkit.gemo.FalseBook.Mechanics.MechanicListener;
import com.bukkit.gemo.utils.SignUtils;
import com.bukkit.gemo.utils.UtilPermissions;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class MechanicLightswitch extends MechanicListener
{
  public MechanicLightswitch(FalseBookBlockCore plugin)
  {
    plugin.getMechanicHandler().registerEvent(BlockBreakEvent.class, this);
    plugin.getMechanicHandler().registerEvent(SignChangeEvent.class, this);
    plugin.getMechanicHandler().registerEvent(PlayerInteractEvent.class, this);
  }

  public void onBlockBreak(BlockBreakEvent event)
  {
    if (event.isCancelled()) {
      return;
    }

    Block block = event.getBlock();
    if (block.getType().equals(Material.WALL_SIGN)) {
      Sign sign = (Sign)block.getState();
      if ((!sign.getLine(1).equalsIgnoreCase("[i]")) && (!sign.getLine(1).equalsIgnoreCase("[|]"))) {
        return;
      }

      Player player = event.getPlayer();
      if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.lightswitch.create")) {
        player.sendMessage(ChatColor.RED + "You are not allowed to destroy lightswitches.");
        event.setCancelled(true);
        return;
      }

    }

    if (!SignUtils.isSignAnchor(block)) {
      return;
    }
    ArrayList<Sign> signList = SignUtils.getAdjacentWallSigns(block);
    if (signList.size() > 0)
    {
      boolean switchFound = false;
      for (Sign sign : signList) {
        if ((sign.getLine(1).equalsIgnoreCase("[i]")) || (sign.getLine(1).equalsIgnoreCase("[|]"))) {
          switchFound = true;
          break;
        }
      }
      signList.clear();

      if (!switchFound) {
        return;
      }

      Player player = event.getPlayer();
      if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.lightswitch.create")) {
        player.sendMessage(ChatColor.RED + "You are not allowed to destroy lightswitches.");
        event.setCancelled(true);
        return;
      }
    }
  }

  public void onPlayerInteract(PlayerInteractEvent event, boolean isWallSign, boolean isSignPost)
  {
    if (event.isCancelled()) {
      return;
    }

    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    if ((!isWallSign) && (!isSignPost)) {
      return;
    }

    Player player = event.getPlayer();
    Block block = event.getClickedBlock();
    handleRightclick(player, block);
  }

  public void onSignChange(SignChangeEvent event)
  {
    if (event.isCancelled()) {
      return;
    }
    Player player = event.getPlayer();
    String worldName = player.getWorld().getName();

    if (!ConfigHandler.isLightswitchEnabled(worldName)) {
      return;
    }

    if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.lightswitch.create")) {
      SignUtils.cancelSignCreation(event, "You are not allowed to build a lightswitch.");
      return;
    }

    if (event.getBlock().getTypeId() != Material.WALL_SIGN.getId()) {
      SignUtils.cancelSignCreation(event, "A lightswitches must be wallsign.");
      return;
    }

    event.setLine(1, event.getLine(1).toUpperCase());
  }
  private void handleRightclick(Player player, Block signBlock) {
    if (!signBlock.getType().equals(Material.WALL_SIGN)) {
      return;
    }
    if (!ConfigHandler.isLightswitchEnabled(signBlock.getWorld().getName())) {
      return;
    }
    Block over = signBlock.getRelative(0, 1, 0);
    if ((!over.getType().equals(Material.TORCH)) && (!over.getType().equals(Material.REDSTONE_TORCH_ON)) && (!over.getType().equals(Material.REDSTONE_TORCH_OFF))) {
      return;
    }
    if ((!((Sign)signBlock.getState()).getLine(1).equalsIgnoreCase("[i]")) && (!((Sign)signBlock.getState()).getLine(1).equalsIgnoreCase("[|]"))) {
      return;
    }

    if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.lightswitch.use")) {
      player.sendMessage(ChatColor.RED + "You are not allowed to use a lightswitch.");
      return;
    }

    boolean on = !over.getType().equals(Material.TORCH);
    if (on)
      over.setTypeIdAndData(Material.TORCH.getId(), over.getData(), false);
    else {
      over.setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(), over.getData(), false);
    }

    int toggled = -1;
    for (int x = -10; x <= 10; x++) {
      for (int z = -10; z <= 10; z++) {
        for (int y = -5; y <= 5; y++) {
          if ((signBlock.getRelative(x, y, z).getTypeId() == Material.TORCH.getId()) || (signBlock.getRelative(x, y, z).getTypeId() == Material.REDSTONE_TORCH_OFF.getId()) || (signBlock.getRelative(x, y, z).getTypeId() == Material.REDSTONE_TORCH_ON.getId())) {
            byte data = signBlock.getRelative(x, y, z).getData();
            if (on)
              signBlock.getRelative(x, y, z).setTypeIdAndData(Material.TORCH.getId(), data, true);
            else {
              signBlock.getRelative(x, y, z).setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(), data, true);
            }
            toggled++;
            if (toggled >= ConfigHandler.getLightswitchMaxToggle(signBlock.getWorld().getName())) {
              player.sendMessage(ChatColor.DARK_GREEN + "Toggled " + toggled + " lights.");
              return;
            }
          }
        }
      }
    }
    player.sendMessage(ChatColor.DARK_GREEN + "Toggled " + toggled + " lights.");
  }
}