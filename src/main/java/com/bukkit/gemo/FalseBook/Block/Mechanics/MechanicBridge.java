package com.bukkit.gemo.FalseBook.Block.Mechanics;

import com.bukkit.gemo.FalseBook.Block.Config.ConfigHandler;
import com.bukkit.gemo.FalseBook.Block.FalseBookBlockCore;
import com.bukkit.gemo.FalseBook.Mechanics.MechanicListener;
import com.bukkit.gemo.FalseBook.Values.ValueIntegerList;
import com.bukkit.gemo.utils.BlockUtils;
import com.bukkit.gemo.utils.ChatUtils;
import com.bukkit.gemo.utils.LWCProtection;
import com.bukkit.gemo.utils.Parser;
import com.bukkit.gemo.utils.SignUtils;
import com.bukkit.gemo.utils.UtilPermissions;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
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
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class MechanicBridge extends MechanicListener
{
  private ArrayList<BridgeArea> BridgeAreas = new ArrayList();

  public MechanicBridge(FalseBookBlockCore plugin) {
    plugin.getMechanicHandler().registerEvent(BlockBreakEvent.class, this);
    plugin.getMechanicHandler().registerEvent(BlockPistonExtendEvent.class, this);
    plugin.getMechanicHandler().registerEvent(BlockPistonRetractEvent.class, this);
    plugin.getMechanicHandler().registerEvent(SignChangeEvent.class, this);
    plugin.getMechanicHandler().registerEvent(EntityChangeBlockEvent.class, this);
    plugin.getMechanicHandler().registerEvent(EntityExplodeEvent.class, this);
    plugin.getMechanicHandler().registerEvent(PlayerInteractEvent.class, this);
  }

  public boolean isActivatedByRedstone(Block block, BlockRedstoneEvent event)
  {
    return (ConfigHandler.isRedstoneAllowedForBridges(block.getWorld().getName())) && (ConfigHandler.isBridgeEnabled(block.getWorld().getName()));
  }

  public void onLoad()
  {
    loadBridges();
  }

  public void reloadMechanic()
  {
    saveBridges();
    this.BridgeAreas = new ArrayList();
    loadBridges();
  }

  public void onUnload()
  {
    saveBridges();
  }

  public void onEntityExplode(EntityExplodeEvent event)
  {
    if (event.isCancelled()) {
      return;
    }
    if (isBlockProtected(event.blockList())) {
      event.setYield(0.0F);
      event.setCancelled(true);
    }
  }

  public void onEntityChangeBlock(EntityChangeBlockEvent event)
  {
    if (event.isCancelled()) {
      return;
    }
    if (isBlockProtected(event.getBlock()))
      event.setCancelled(true);
  }

  public void onSignChange(SignChangeEvent event)
  {
    if (event.isCancelled()) {
      return;
    }

    if (!ConfigHandler.isBridgeEnabled(event.getBlock().getWorld().getName())) {
      return;
    }

    Player player = event.getPlayer();
    if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.bridge")) {
      SignUtils.cancelSignCreation(event, "You are not allowed to build bridges.");
      return;
    }

    String worldName = event.getBlock().getWorld().getName();

    if (SignUtils.getDirection((Sign)event.getBlock().getState()) == -1) {
      SignUtils.cancelSignCreation(event, "Bridgesigns may only be created at specific angles (90 degrees).");
      return;
    }

    if (event.getBlock().getTypeId() != Material.SIGN_POST.getId()) {
      SignUtils.cancelSignCreation(event, "Bridgesigns must be signposts.");
      return;
    }

    if (event.getLine(1).equalsIgnoreCase("[Bridge]"))
      event.setLine(1, "[Bridge]");
    if (event.getLine(1).equalsIgnoreCase("[Bridge End]")) {
      event.setLine(1, "[Bridge End]");
    }
    if (Parser.isIntegerOrEmpty(event.getLine(2))) {
      int width = Parser.getInteger(event.getLine(2), 1);
      if (width < 0) {
        SignUtils.cancelSignCreation(event, "Line 3 must be >= 0.");
        return;
      }
      if (width > ConfigHandler.getMaxBridgeSideWidth(worldName)) {
        SignUtils.cancelSignCreation(event, "Line 3 must be <= " + ConfigHandler.getMaxBridgeSideWidth(worldName));
        return;
      }
      if (width == 1)
        event.setLine(2, "");
    }
    else {
      SignUtils.cancelSignCreation(event, "Line 3 must be a number >= 0, or leave it empty.");
      return;
    }
    if (Parser.isIntegerOrEmpty(event.getLine(3))) {
      int width = Parser.getInteger(event.getLine(3), 1);
      if (width < 0) {
        SignUtils.cancelSignCreation(event, "Line 4 must be >= 0.");
        return;
      }
      if (width > ConfigHandler.getMaxBridgeSideWidth(worldName)) {
        SignUtils.cancelSignCreation(event, "Line 4 must be <= " + ConfigHandler.getMaxBridgeSideWidth(worldName));
        return;
      }
      if (width == 1)
        event.setLine(3, "");
    }
    else {
      SignUtils.cancelSignCreation(event, "Line 4 must be a number >= 0, or leave it empty.");
      return;
    }
    ChatUtils.printSuccess(player, "[FB-Block]", "Bridgesign created.");
  }

  public void onBlockBreak(BlockBreakEvent event)
  {
    if (event.isCancelled()) {
      return;
    }

    if (!ConfigHandler.isBridgeEnabled(event.getBlock().getWorld().getName())) {
      return;
    }

    Block block = event.getBlock();
    if ((block.getType().equals(Material.SIGN_POST)) || (block.getType().equals(Material.WALL_SIGN))) {
      Sign sign = (Sign)block.getState();
      if ((!sign.getLine(1).equalsIgnoreCase("[Bridge]")) && (!sign.getLine(1).equalsIgnoreCase("[Bridge End]"))) {
        return;
      }

      Player player = event.getPlayer();
      if ((isBlockProtected(block)) && 
        (!UtilPermissions.playerCanUseCommand(player, "falsebook.destroy.blocks"))) {
        player.sendMessage(ChatColor.RED + "You are not allowed to destroy bridgesigns.");
        event.setCancelled(true);
        return;
      }
    }
    else {
      boolean isOp = UtilPermissions.playerCanUseCommand(event.getPlayer(), "falsebook.destroy.blocks");
      if ((isBlockProtected(block)) && (!isOp)) {
        event.getPlayer().sendMessage(ChatColor.AQUA + "[ FalseBook ] " + ChatColor.RED + "You are not allowed to destroy bridgeblocks.!");
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

    if (!ConfigHandler.isBridgeEnabled(event.getBlock().getWorld().getName())) {
      return;
    }
    if (isBlockProtected(event.getBlocks()))
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

    if (!ConfigHandler.isBridgeEnabled(event.getBlock().getWorld().getName())) {
      return;
    }
    if (isBlockProtected(event.getRetractLocation().getBlock()))
      event.setCancelled(true);
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
    if ((!sign.getLine(1).equalsIgnoreCase("[Bridge]")) && (!sign.getLine(1).equalsIgnoreCase("[Bridge End]"))) {
      return;
    }

    if (!ConfigHandler.isBridgeEnabled(block.getWorld().getName())) {
      return;
    }

    event.setUseInteractedBlock(Event.Result.DENY);
    event.setUseItemInHand(Event.Result.DENY);
    event.setCancelled(true);

    if ((ConfigHandler.isRespectLWCProtections(event.getPlayer().getWorld().getName())) && 
      (!LWCProtection.canAccessWithCModify(event.getPlayer(), block)) && (!UtilPermissions.playerCanUseCommand(event.getPlayer(), "falsebook.blocks.ignoreLWCProtections"))) {
      event.getPlayer().sendMessage(ChatColor.RED + "This bridge is protected!");
      return;
    }

    handleReturnResult(event.getPlayer(), toggle(sign));
  }

  private void handleReturnResult(Player player, int result) {
    switch (result)
    {
    case -11:
      player.sendMessage(ChatColor.RED + "Bridgesigns must be signposts.");
      break;
    case -10:
      player.sendMessage(ChatColor.RED + "Internal error while toggling bridge.");
      break;
    case -9:
      player.sendMessage(ChatColor.RED + "Bridgesigns must be created at an angle divisionable by 90 degrees.");
      break;
    case -8:
      player.sendMessage(ChatColor.RED + "No bridge found.");
      break;
    case -7:
      player.sendMessage(ChatColor.RED + "The bridgewidth of both signs is different.");
      break;
    case 0:
      player.sendMessage(ChatColor.RED + "This blocktype is not allowed for building bridges.");
      break;
    case 1:
      player.sendMessage(ChatColor.RED + "Bridges must be made out of one material.");
      break;
    case 2:
      player.sendMessage(ChatColor.RED + "Bridgesigns must be more than 1 block away from eachother.");
      break;
    case 8:
      player.sendMessage(ChatColor.GOLD + "Bridge toggled.");
    case -6:
    case -5:
    case -4:
    case -3:
    case -2:
    case -1:
    case 3:
    case 4:
    case 5:
    case 6:
    case 7: }  } 
  private boolean isBlockProtected(List<Block> blockList) { for (int j = 0; j < blockList.size(); j++) {
      try
      {
        for (int i = 0; i < this.BridgeAreas.size(); i++)
          if (((BridgeArea)this.BridgeAreas.get(i)).isBlockInArea((Block)blockList.get(j)))
            return true;
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    return false;
  }

  private boolean isBlockProtected(Block block)
  {
    try
    {
      for (int i = 0; i < this.BridgeAreas.size(); i++)
        if (((BridgeArea)this.BridgeAreas.get(i)).isBlockInArea(block))
          return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }

  private void saveBridges()
  {
    File folder = new File("plugins" + System.getProperty("file.separator") + "FalseBook");
    folder.mkdirs();
    try {
      Writer output = new BufferedWriter(new FileWriter("plugins" + System.getProperty("file.separator") + "FalseBook" + System.getProperty("file.separator") + "Bridges.db"));
      output.write("BridgesCount=" + this.BridgeAreas.size() + "\r\n");
      for (int i = 0; i < this.BridgeAreas.size(); i++) {
        String str = "";
        str = ((BridgeArea)this.BridgeAreas.get(i)).getSign1().getBlock().getX() + "," + ((BridgeArea)this.BridgeAreas.get(i)).getSign1().getBlock().getY() + "," + ((BridgeArea)this.BridgeAreas.get(i)).getSign1().getBlock().getZ() + ";";
        str = str + ((BridgeArea)this.BridgeAreas.get(i)).getSign2().getBlock().getX() + "," + ((BridgeArea)this.BridgeAreas.get(i)).getSign2().getBlock().getY() + "," + ((BridgeArea)this.BridgeAreas.get(i)).getSign2().getBlock().getZ() + ";";
        str = str + ((BridgeArea)this.BridgeAreas.get(i)).getUp() + ";";
        str = str + ((BridgeArea)this.BridgeAreas.get(i)).getSign1().getBlock().getWorld().getName();
        output.write(str + "\r\n");
      }
      output.close();
    } catch (IOException e) {
      FalseBookBlockCore.printInConsole("Error while saving file: plugins/FalseBook/Bridges.db");
      e.printStackTrace();
    }
  }

  private boolean loadBridges()
  {
    File f = new File("plugins" + System.getProperty("file.separator") + "FalseBook" + System.getProperty("file.separator") + "Bridges.db");
    if (f.exists()) {
      try {
        FileInputStream fstream = new FileInputStream("plugins" + System.getProperty("file.separator") + "FalseBook" + System.getProperty("file.separator") + "Bridges.db");
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String strLine = br.readLine();
        if (strLine == null) {
          FalseBookBlockCore.printInConsole("No Bridges loaded");
          return false;
        }
        String[] split = strLine.split("=");
        int count = Integer.valueOf(split[1]).intValue();
        for (int i = 0; i < count; i++) {
          strLine = br.readLine();
          if (strLine == null) {
            continue;
          }
          String[] splitData = strLine.split(";");
          if (splitData.length == 4) {
            String[] sign1 = splitData[0].split(",");
            String[] sign2 = splitData[1].split(",");
            Boolean isUp = Boolean.valueOf(splitData[2]);
            String worldName = splitData[3];
            World thisWorld = Bukkit.getServer().getWorld(worldName);
            if (thisWorld == null) {
              continue;
            }
            Block thisBlock = thisWorld.getBlockAt(Integer.valueOf(sign1[0]).intValue(), Integer.valueOf(sign1[1]).intValue(), Integer.valueOf(sign1[2]).intValue());
            Block thisBlock2 = thisWorld.getBlockAt(Integer.valueOf(sign2[0]).intValue(), Integer.valueOf(sign2[1]).intValue(), Integer.valueOf(sign2[2]).intValue());

            if ((thisBlock == null) || (thisBlock2 == null)) {
              continue;
            }
            if ((thisBlock.getType().equals(Material.SIGN_POST)) && (thisBlock2.getType().equals(Material.SIGN_POST))) {
              Sign signBlock1 = (Sign)thisBlock.getState();
              Sign signBlock2 = (Sign)thisBlock2.getState();
              boolean canInit = true;

              Point corner1 = new Point(signBlock1.getX(), signBlock1.getZ());
              Point corner2 = new Point(signBlock2.getX(), signBlock2.getZ());
              int signDir = SignUtils.getDirection(signBlock1);
              int bridgeLeft = Parser.getInteger(signBlock1.getLine(2), 1);
              int bridgeRight = Parser.getInteger(signBlock1.getLine(3), 1);
              switch (signDir)
              {
              case 1:
                corner1.x -= bridgeLeft;
                corner2.x += bridgeRight;
                break;
              case 2:
                corner1.y += bridgeLeft;
                corner2.y -= bridgeRight;
                break;
              case 3:
                corner1.x += bridgeLeft;
                corner2.x -= bridgeRight;
                break;
              case 4:
                corner1.y -= bridgeLeft;
                corner2.y += bridgeRight;
                break;
              default:
                canInit = false;
              }

              if (!canInit) {
                continue;
              }
              BridgeArea now = new BridgeArea(signBlock1, signBlock2, corner1, corner2);
              now.setUp(isUp);
              this.BridgeAreas.add(now);
            }
          }
        }
        FalseBookBlockCore.printInConsole(this.BridgeAreas.size() + " Bridges successfully loaded.");
        return true;
      } catch (Exception e) {
        e.printStackTrace();
        FalseBookBlockCore.printInConsole("Error while reading file: plugins/FalseBook/Bridges.db");
        return false;
      }
    }
    return false;
  }

  private boolean isBlockTypeAllowed(int ID, String worldName)
  {
    return ConfigHandler.getAllowedBridgeBlocks(worldName).hasValue(ID);
  }

  public int toggle(Sign signBlock)
  {
    if (signBlock.getTypeId() != Material.SIGN_POST.getId()) {
      return -11;
    }

    int signDir = SignUtils.getDirection(signBlock);
    if (signDir == -1) {
      return -9;
    }
    int left = Parser.getInteger(signBlock.getLine(2), 1);
    int right = Parser.getInteger(signBlock.getLine(3), 1);

    Sign opSign = getOppositeSign(signBlock, signDir);
    if (opSign == null) {
      return -8;
    }
    int left2nd = Parser.getInteger(opSign.getLine(2), 1);
    int right2nd = Parser.getInteger(opSign.getLine(3), 1);

    if ((left != right2nd) || (right != left2nd))
    {
      return -7;
    }

    int result = -10;

    result = toggle(signBlock, opSign, signBlock.getBlock().getRelative(0, 1, 0), signDir, left, right, 1);

    if (result != 8) {
      result = toggle(signBlock, opSign, signBlock.getBlock().getRelative(0, -1, 0), signDir, left, right, -1);
    }
    return result;
  }

  private int toggle(Sign signBlock, Sign opSign, Block originBlock, int signDir, int bridgeLeft, int bridgeRight, int yOffset)
  {
    if (!isBlockTypeAllowed(originBlock.getTypeId(), originBlock.getWorld().getName())) {
      originBlock = null;
      return 0;
    }

    Point corner1 = new Point(signBlock.getX(), signBlock.getZ());
    Point corner2 = new Point(opSign.getX(), opSign.getZ());
    boolean keepX = false;
    switch (signDir)
    {
    case 1:
      corner1.x -= bridgeLeft;
      corner2.x += bridgeRight;
      keepX = false;
      break;
    case 2:
      corner1.y += bridgeLeft;
      corner2.y -= bridgeRight;
      keepX = true;
      break;
    case 3:
      corner1.x += bridgeLeft;
      corner2.x -= bridgeRight;
      keepX = false;
      break;
    case 4:
      corner1.y -= bridgeLeft;
      corner2.y += bridgeRight;
      keepX = true;
      break;
    default:
      return -9;
    }

    ArrayList newArea = getArea(signBlock, opSign, signDir, bridgeLeft, bridgeRight, yOffset);
    if (newArea.size() == 0) {
      return 2;
    }

    boolean toggleOn = ((Block)newArea.get(0)).getTypeId() != originBlock.getTypeId();

    if (!canToggle(originBlock, opSign, corner1, corner2, toggleOn, keepX)) {
      return 1;
    }

    World world = originBlock.getWorld();
    for (int x = Math.min(corner1.x, corner2.x); x <= Math.max(corner1.x, corner2.x); x++) {
      for (int z = Math.min(corner1.y, corner2.y); z <= Math.max(corner1.y, corner2.y); z++) {
        if (toggleOn) {
          if (keepX) {
            if ((x != originBlock.getX()) && (x != opSign.getX()))
              world.getBlockAt(x, originBlock.getY(), z).setTypeIdAndData(originBlock.getTypeId(), originBlock.getData(), true);
          }
          else if ((z != originBlock.getZ()) && (z != opSign.getZ())) {
            world.getBlockAt(x, originBlock.getY(), z).setTypeIdAndData(originBlock.getTypeId(), originBlock.getData(), true);
          }
        }
        else if (keepX) {
          if ((x != originBlock.getX()) && (x != opSign.getX()))
            world.getBlockAt(x, originBlock.getY(), z).setType(Material.AIR);
        }
        else if ((z != originBlock.getZ()) && (z != opSign.getZ())) {
          world.getBlockAt(x, originBlock.getY(), z).setType(Material.AIR);
        }

      }

    }

    BridgeArea now = new BridgeArea(signBlock, opSign, corner1, corner2);

    if (originBlock.getY() > signBlock.getY())
      now.setUp(Boolean.valueOf(true));
    else {
      now.setUp(Boolean.valueOf(false));
    }
    if (!toggleOn)
    {
      int indexInList = isInBridgeList(now);
      if (indexInList != -1) {
        this.BridgeAreas.remove(indexInList);
      }

    }
    else if (isInBridgeList(now) == -1) {
      this.BridgeAreas.add(now);
    }

    originBlock = null;
    newArea.clear();
    newArea = null;
    return 8;
  }

  public boolean canToggle(Block originBlock, Sign opSign, Point corner1, Point corner2, boolean toggleOn, boolean keepX) {
    World world = originBlock.getWorld();
    int ID = 0;
    for (int x = Math.min(corner1.x, corner2.x); x <= Math.max(corner1.x, corner2.x); x++) {
      for (int z = Math.min(corner1.y, corner2.y); z <= Math.max(corner1.y, corner2.y); z++) {
        ID = world.getBlockAt(x, originBlock.getY(), z).getTypeId();
        if (toggleOn)
        {
          if (keepX) {
            if ((x == originBlock.getX()) || (x == opSign.getX()))
            {
              if ((originBlock.getTypeId() != ID) || (originBlock.getData() != world.getBlockAt(x, originBlock.getY(), z).getData())) {
                return false;
              }

            }
            else if ((ID != Material.AIR.getId()) && (ID != Material.WATER.getId()) && (ID != Material.STATIONARY_WATER.getId()) && (ID != Material.LAVA.getId()) && (ID != Material.STATIONARY_LAVA.getId())) {
              return false;
            }

          }
          else if ((z == originBlock.getZ()) || (z == opSign.getZ()))
          {
            if ((originBlock.getTypeId() != ID) || (originBlock.getData() != world.getBlockAt(x, originBlock.getY(), z).getData())) {
              return false;
            }

          }
          else if ((ID != Material.AIR.getId()) && (ID != Material.WATER.getId()) && (ID != Material.STATIONARY_WATER.getId()) && (ID != Material.LAVA.getId()) && (ID != Material.STATIONARY_LAVA.getId())) {
            return false;
          }

        }
        else if ((originBlock.getTypeId() != ID) || (originBlock.getData() != world.getBlockAt(x, originBlock.getY(), z).getData())) {
          return false;
        }
      }
    }
    return true;
  }

  private ArrayList<Block> getArea(Sign firstSign, Sign secondSign, int signDir, int widthLeft, int widthRight, int yDir)
  {
    ArrayList newArea = new ArrayList();
    Block originBlock = firstSign.getBlock();
    int flag = 1;

    if ((signDir == 1) || (signDir == 3))
    {
      if (signDir == 1)
        flag = -1;
      int bridgeLength = Math.abs(firstSign.getBlock().getZ() - secondSign.getBlock().getZ()) - 1;
      for (int i = 1; i <= bridgeLength; i++) {
        newArea.add(originBlock.getRelative(0, yDir, i * flag));
        for (int left = 1; left <= widthLeft; left++) {
          newArea.add(originBlock.getRelative(left * flag, yDir, i * flag));
        }
        for (int right = 1; right <= widthRight; right++)
          newArea.add(originBlock.getRelative(right * flag * -1, yDir, i * flag));
      }
    }
    else
    {
      if (signDir == 2)
        flag = -1;
      int bridgeLength = Math.abs(firstSign.getBlock().getX() - secondSign.getBlock().getX()) - 1;
      for (int i = 1; i <= bridgeLength; i++) {
        newArea.add(originBlock.getRelative(i * flag, yDir, 0));
        for (int left = 1; left <= widthLeft; left++) {
          newArea.add(originBlock.getRelative(i * flag, yDir, left * flag));
        }
        for (int right = 1; right <= widthRight; right++) {
          newArea.add(originBlock.getRelative(i * flag, yDir, right * flag * -1));
        }
      }
    }
    return newArea;
  }

  private Sign getOppositeSign(Sign signBlock, int signDir)
  {
    Block block = signBlock.getBlock();
    int thisID = -1;
    int flag = 1;

    if ((signDir == 1) || (signDir == 3))
    {
      if (signDir == 1) {
        flag = -1;
      }
      for (int i = 2; i <= ConfigHandler.getMaxBridgeLength(signBlock.getWorld().getName()) + 1; i++) {
        thisID = block.getRelative(0, 0, i * flag).getTypeId();
        if ((thisID != Material.WALL_SIGN.getId()) && (thisID != Material.SIGN_POST.getId())) {
          continue;
        }
        Sign opSign = (Sign)block.getRelative(0, 0, i * flag).getState();
        if (((signDir == 1) && (SignUtils.getDirection(opSign) == 3)) || ((signDir == 3) && (SignUtils.getDirection(opSign) == 1) && (
          (opSign.getLine(1).equalsIgnoreCase("[Bridge]")) || (opSign.getLine(1).equalsIgnoreCase("[Bridge End]"))))) {
          return opSign;
        }
      }
    }
    else
    {
      if (signDir == 2) {
        flag = -1;
      }
      for (int i = 2; i <= ConfigHandler.getMaxBridgeLength(signBlock.getWorld().getName()); i++) {
        thisID = block.getRelative(i * flag, 0, 0).getTypeId();
        if ((thisID != Material.WALL_SIGN.getId()) && (thisID != Material.SIGN_POST.getId())) {
          continue;
        }
        Sign opSign = (Sign)block.getRelative(i * flag, 0, 0).getState();
        if (((signDir == 2) && (SignUtils.getDirection(opSign) == 4)) || ((signDir == 4) && (SignUtils.getDirection(opSign) == 2) && (
          (opSign.getLine(1).equalsIgnoreCase("[Bridge]")) || (opSign.getLine(1).equalsIgnoreCase("[Bridge End]"))))) {
          return opSign;
        }
      }
    }

    return null;
  }

  private int isInBridgeList(BridgeArea a)
  {
    for (int i = 0; i < this.BridgeAreas.size(); i++) {
      BridgeArea a2 = (BridgeArea)this.BridgeAreas.get(i);
      if (((BlockUtils.LocationEquals(a.getSign1().getBlock().getLocation(), a2.getSign1().getBlock().getLocation())) && (BlockUtils.LocationEquals(a.getSign2().getBlock().getLocation(), a2.getSign2().getBlock().getLocation()))) || ((BlockUtils.LocationEquals(a.getSign1().getBlock().getLocation(), a2.getSign2().getBlock().getLocation())) && (BlockUtils.LocationEquals(a.getSign2().getBlock().getLocation(), a2.getSign1().getBlock().getLocation())) && 
        (a.getUp() == a2.getUp()))) {
        return i;
      }
    }
    return -1;
  }
}