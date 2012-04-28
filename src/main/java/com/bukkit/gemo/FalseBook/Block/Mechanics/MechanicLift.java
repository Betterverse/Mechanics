package com.bukkit.gemo.FalseBook.Block.Mechanics;

import com.bukkit.gemo.FalseBook.Block.Config.ConfigHandler;
import com.bukkit.gemo.FalseBook.Block.FalseBookBlockCore;
import com.bukkit.gemo.FalseBook.Mechanics.MechanicListener;
import com.bukkit.gemo.utils.BlockUtils;
import com.bukkit.gemo.utils.LWCProtection;
import com.bukkit.gemo.utils.SignUtils;
import com.bukkit.gemo.utils.UtilPermissions;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class MechanicLift extends MechanicListener
{
  public MechanicLift(FalseBookBlockCore plugin)
  {
    plugin.getMechanicHandler().registerEvent(BlockBreakEvent.class, this);
    plugin.getMechanicHandler().registerEvent(BlockPistonExtendEvent.class, this);
    plugin.getMechanicHandler().registerEvent(BlockPistonRetractEvent.class, this);
    plugin.getMechanicHandler().registerEvent(SignChangeEvent.class, this);
    plugin.getMechanicHandler().registerEvent(EntityChangeBlockEvent.class, this);
    plugin.getMechanicHandler().registerEvent(EntityExplodeEvent.class, this);
    plugin.getMechanicHandler().registerEvent(PlayerInteractEvent.class, this);
  }

  private boolean isBlockBreakable(List<Block> blockList)
  {
    ArrayList<Sign> signList = new ArrayList();
    for (int j = 0; j < blockList.size(); j++) {
      SignUtils.addAdjacentWallSigns(signList, (Block)blockList.get(j));
    }

    if (signList.size() > 0)
    {
      boolean liftFound = false;
      for (Sign sign : signList) {
        if ((sign.getLine(1).equalsIgnoreCase("[Lift Up]")) || (sign.getLine(1).equalsIgnoreCase("[Lift Down]")) || (sign.getLine(1).equalsIgnoreCase("[Lift]"))) {
          liftFound = true;
          break;
        }
      }
      signList.clear();

      if (liftFound)
        return false;
    }
    return true;
  }

  private boolean isBlockBreakable(Block block)
  {
    ArrayList<Sign> signList = new ArrayList();
    SignUtils.addAdjacentWallSigns(signList, block);

    if (signList.size() > 0)
    {
      boolean liftFound = false;
      for (Sign sign : signList) {
        if ((sign.getLine(1).equalsIgnoreCase("[Lift Up]")) || (sign.getLine(1).equalsIgnoreCase("[Lift Down]")) || (sign.getLine(1).equalsIgnoreCase("[Lift]"))) {
          liftFound = true;
          break;
        }
      }
      signList.clear();

      if (liftFound)
        return false;
    }
    return true;
  }

  public void onEntityExplode(EntityExplodeEvent event)
  {
    if (event.isCancelled()) {
      return;
    }
    if (!isBlockBreakable(event.blockList())) {
      event.setYield(0.0F);
      event.setCancelled(true);
    }
  }

  public void onEntityChangeBlock(EntityChangeBlockEvent event)
  {
    if (event.isCancelled()) {
      return;
    }
    List blockList = new ArrayList();
    blockList.add(event.getBlock());
    if (!isBlockBreakable(blockList)) {
      event.setCancelled(true);
    }
    blockList.clear();
    blockList = null;
  }

  public void onBlockBreak(BlockBreakEvent event)
  {
    if (event.isCancelled()) {
      return;
    }

    Block block = event.getBlock();
    if ((block.getType().equals(Material.SIGN_POST)) || (block.getType().equals(Material.WALL_SIGN))) {
      Sign sign = (Sign)block.getState();
      if ((!sign.getLine(1).equalsIgnoreCase("[Lift Up]")) && (!sign.getLine(1).equalsIgnoreCase("[Lift Down]")) && (!sign.getLine(1).equalsIgnoreCase("[Lift]"))) {
        return;
      }

      Player player = event.getPlayer();
      if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.lift")) {
        player.sendMessage(ChatColor.RED + "You are not allowed to destroy lifts.");
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
      boolean liftFound = false;
      for (Sign sign : signList) {
        if ((sign.getLine(1).equalsIgnoreCase("[Lift Up]")) || (sign.getLine(1).equalsIgnoreCase("[Lift Down]")) || (sign.getLine(1).equalsIgnoreCase("[Lift]"))) {
          liftFound = true;
          break;
        }
      }
      signList.clear();

      if (!liftFound) {
        return;
      }

      Player player = event.getPlayer();
      if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.lift")) {
        player.sendMessage(ChatColor.RED + "You are not allowed to destroy lifts.");
        event.setCancelled(true);
        return;
      }
    }
  }

  public void onBlockPistonExtend(BlockPistonExtendEvent event)
  {
    if (event.isCancelled()) {
      return;
    }
    if (!isBlockBreakable(event.getBlocks()))
      event.setCancelled(true);
  }

  public void onBlockPistonRetract(BlockPistonRetractEvent event)
  {
    if (event.isCancelled()) {
      return;
    }
    if (!event.isSticky()) {
      return;
    }
    if (!isBlockBreakable(event.getRetractLocation().getBlock()))
      event.setCancelled(true);
  }

  public void onSignChange(SignChangeEvent event)
  {
    if (event.isCancelled()) {
      return;
    }

    Player player = event.getPlayer();
    if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.lift")) {
      SignUtils.cancelSignCreation(event, "You are not allowed to build lifts.");
      return;
    }

    if (event.getLine(1).equalsIgnoreCase("[Lift Up]")) {
      event.setLine(1, "[Lift Up]");
      if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.lift")) {
        SignUtils.cancelSignCreation(event, "You are not allowed to build Elevators.");
        return;
      }
      if (searchLiftDown(event.getBlock()))
        player.sendMessage(ChatColor.GREEN + "Elevator sign created and linked.");
      else
        player.sendMessage(ChatColor.GRAY + "Elevator sign created but not linked yet.");
    }
    else if (event.getLine(1).equalsIgnoreCase("[Lift Down]")) {
      event.setLine(1, "[Lift Down]");
      if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.lift")) {
        SignUtils.cancelSignCreation(event, "You are not allowed to build Elevators.");
        return;
      }
      if (searchLiftUp(event.getBlock()))
        player.sendMessage(ChatColor.GREEN + "Elevator sign created and linked.");
      else
        player.sendMessage(ChatColor.GRAY + "Elevator sign created but not linked yet.");
    }
    else if (event.getLine(1).equalsIgnoreCase("[Lift]")) {
      event.setLine(1, "[Lift]");
      if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.lift")) {
        SignUtils.cancelSignCreation(event, "You are not allowed to build Elevators.");
        return;
      }
      if ((searchLiftUp(event.getBlock())) || (searchLiftDown(event.getBlock())))
        player.sendMessage(ChatColor.GREEN + "Elevator sign created and linked.");
      else
        player.sendMessage(ChatColor.GRAY + "Elevator sign created but not linked yet.");
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

    Block block = event.getClickedBlock();
    if ((!isWallSign) && (!isSignPost)) {
      return;
    }

    Sign sign = (Sign)block.getState();
    if ((!sign.getLine(1).equalsIgnoreCase("[Lift Up]")) && (!sign.getLine(1).equalsIgnoreCase("[Lift Down]")) && (!sign.getLine(1).equalsIgnoreCase("[Lift]"))) {
      return;
    }

    event.setUseInteractedBlock(Event.Result.DENY);
    event.setUseItemInHand(Event.Result.DENY);
    event.setCancelled(true);

    Player player = event.getPlayer();
    if ((!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.lift")) && (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.lift.use"))) {
      player.sendMessage(ChatColor.RED + "You are not allowed to use lifts.");
      return;
    }

    if ((ConfigHandler.isRespectLWCProtections(player.getWorld().getName())) && 
      (!LWCProtection.canAccessWithCModify(player, block)) && (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.ignoreLWCProtections"))) {
      player.sendMessage(ChatColor.RED + "This lift is protected!");
      return;
    }

    check(sign, player, block);
  }

  private boolean searchLiftUp(Block block)
  {
    Block newBlock = block;
    World current = block.getWorld();
    for (int i = block.getLocation().getBlockY() - 1; i > 0; i--) {
      newBlock = current.getBlockAt(block.getX(), i, block.getZ());
      if ((newBlock.getType().equals(Material.WALL_SIGN)) || (newBlock.getType().equals(Material.SIGN_POST))) {
        Sign sign2 = (Sign)newBlock.getState();
        if ((sign2.getLine(1).equalsIgnoreCase("[Lift Up]")) || (sign2.getLine(1).equalsIgnoreCase("[Lift Down]")) || (sign2.getLine(1).equalsIgnoreCase("[Lift]"))) {
          return true;
        }
      }
    }
    return false;
  }

  private Block getLiftUpBlock(Block block)
  {
    Block newBlock = block;
    World current = block.getWorld();
    for (int i = block.getLocation().getBlockY() - 1; i > 0; i--) {
      newBlock = current.getBlockAt(block.getX(), i, block.getZ());
      if ((newBlock.getType().equals(Material.WALL_SIGN)) || (newBlock.getType().equals(Material.SIGN_POST))) {
        Sign sign2 = (Sign)newBlock.getState();
        if ((sign2.getLine(1).equalsIgnoreCase("[Lift Down]")) || (sign2.getLine(1).equalsIgnoreCase("[Lift Up]")) || (sign2.getLine(1).equalsIgnoreCase("[Lift]"))) {
          return newBlock;
        }
      }
    }
    return null;
  }

  private boolean searchLiftDown(Block block)
  {
    Block newBlock = block;
    World current = block.getWorld();
    for (int i = block.getLocation().getBlockY() + 1; i < block.getWorld().getMaxHeight() - 1; i++) {
      newBlock = current.getBlockAt(block.getX(), i, block.getZ());
      if ((newBlock.getType().equals(Material.WALL_SIGN)) || (newBlock.getType().equals(Material.SIGN_POST))) {
        Sign sign2 = (Sign)newBlock.getState();
        if ((sign2.getLine(1).equalsIgnoreCase("[Lift Up]")) || (sign2.getLine(1).equalsIgnoreCase("[Lift Down]")) || (sign2.getLine(1).equalsIgnoreCase("[Lift]"))) {
          return true;
        }
      }
    }
    return false;
  }

  private Block getLiftDownBlock(Block block)
  {
    Block newBlock = block;
    World current = block.getWorld();
    for (int i = block.getLocation().getBlockY() + 1; i < block.getWorld().getMaxHeight() - 1; i++) {
      newBlock = current.getBlockAt(block.getX(), i, block.getZ());
      if ((newBlock.getType().equals(Material.WALL_SIGN)) || (newBlock.getType().equals(Material.SIGN_POST))) {
        Sign sign2 = (Sign)newBlock.getState();
        if ((sign2.getLine(1).equalsIgnoreCase("[Lift Up]")) || (sign2.getLine(1).equalsIgnoreCase("[Lift Down]")) || (sign2.getLine(1).equalsIgnoreCase("[Lift]"))) {
          return newBlock;
        }
      }
    }
    return null;
  }

  private void check(Sign sign, Player player, Block block)
  {
    Location playerPos = player.getLocation().clone();

    if (sign.getLine(1).equalsIgnoreCase("[Lift Up]")) {
      if (searchLiftDown(block))
      {
        Block newBlock = getLiftDownBlock(block);
        boolean isFree = false;
        boolean isFreeAbove = false;

        for (int i = newBlock.getY() + 1; i > newBlock.getY() - 3; i--) {
          if (BlockUtils.canPassThrough(block.getWorld().getBlockAt(playerPos.getBlockX(), i, playerPos.getBlockZ()).getTypeId())) {
            if (isFreeAbove) {
              isFree = true;
              playerPos.setY(i);
            } else {
              isFreeAbove = true;
            }
          } else isFreeAbove = false;
        }

        if (isFree) {
          if (!BlockUtils.canPassThrough(block.getWorld().getBlockAt(playerPos.getBlockX(), playerPos.getBlockY() - 1, playerPos.getBlockZ()).getTypeId())) {
            player.teleport(playerPos);
            player.sendMessage(ChatColor.GOLD + "Lift Up");
            if (((Sign)newBlock.getState()).getLine(0).length() > 0)
              player.sendMessage(ChatColor.GRAY + "Floor: " + ((Sign)newBlock.getState()).getLine(0));
          } else {
            player.sendMessage(ChatColor.RED + "You would have nothing to stand on.");
          }
        }
        else player.sendMessage(ChatColor.RED + "You would be obstructed.");
      }
      else
      {
        player.sendMessage(ChatColor.RED + "No lift found.");
      }

    }
    else if (sign.getLine(1).equalsIgnoreCase("[Lift Down]"))
      if (searchLiftUp(block))
      {
        Block newBlock = getLiftUpBlock(block);
        boolean isFree = false;
        boolean isFreeAbove = false;

        for (int i = newBlock.getY() + 1; i > newBlock.getY() - 3; i--) {
          if (BlockUtils.canPassThrough(block.getWorld().getBlockAt(playerPos.getBlockX(), i, playerPos.getBlockZ()).getTypeId())) {
            if (isFreeAbove) {
              isFree = true;
              playerPos.setY(i);
            } else {
              isFreeAbove = true;
            }
          } else isFreeAbove = false;
        }

        if (isFree) {
          if (!BlockUtils.canPassThrough(block.getWorld().getBlockAt(playerPos.getBlockX(), playerPos.getBlockY() - 1, playerPos.getBlockZ()).getTypeId())) {
            player.teleport(playerPos);
            player.sendMessage(ChatColor.GOLD + "Lift Down");
            if (((Sign)newBlock.getState()).getLine(0).length() > 0)
              player.sendMessage(ChatColor.GRAY + "Floor: " + ((Sign)newBlock.getState()).getLine(0));
          } else {
            player.sendMessage(ChatColor.RED + "You would have nothing to stand on.");
          }
        }
        else player.sendMessage(ChatColor.RED + "You would be obstructed.");
      }
      else
      {
        player.sendMessage(ChatColor.RED + "No lift found.");
      }
  }
}