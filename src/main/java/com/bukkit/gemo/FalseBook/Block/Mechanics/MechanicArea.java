package com.bukkit.gemo.FalseBook.Block.Mechanics;

import com.bukkit.gemo.FalseBook.Block.Areas.Area;
import com.bukkit.gemo.FalseBook.Block.Areas.AreaBlockType;
import com.bukkit.gemo.FalseBook.Block.Areas.AreaSelection;
import com.bukkit.gemo.FalseBook.Block.Config.ConfigHandler;
import com.bukkit.gemo.FalseBook.Block.FalseBookBlockCore;
import com.bukkit.gemo.FalseBook.Mechanics.MechanicListener;
import com.bukkit.gemo.utils.ChatUtils;
import com.bukkit.gemo.utils.SignUtils;
import com.bukkit.gemo.utils.UtilPermissions;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class MechanicArea extends MechanicListener
{
  private ArrayList<Area> Areas = new ArrayList();
  private ArrayList<AreaSelection> Selections = new ArrayList();

  public MechanicArea(FalseBookBlockCore plugin) {
    plugin.getMechanicHandler().registerEvent(BlockBreakEvent.class, this);
    plugin.getMechanicHandler().registerEvent(BlockPlaceEvent.class, this);
    plugin.getMechanicHandler().registerEvent(BlockPistonExtendEvent.class, this);
    plugin.getMechanicHandler().registerEvent(BlockPistonRetractEvent.class, this);
    plugin.getMechanicHandler().registerEvent(SignChangeEvent.class, this);
    plugin.getMechanicHandler().registerEvent(EntityChangeBlockEvent.class, this);
    plugin.getMechanicHandler().registerEvent(EntityExplodeEvent.class, this);
    plugin.getMechanicHandler().registerEvent(PlayerInteractEvent.class, this);
  }

  public void onLoad()
  {
    loadAreas();
  }

  public void reloadMechanic()
  {
    HashSet<String> areaNames = new HashSet();
    for (Area area : this.Areas) {
      areaNames.add(area.getAreaName());
    }
    for (String areaName : areaNames) {
      saveAreas(areaName, false);
      saveAreas(areaName, true);
    }
    this.Areas.clear();
    loadAreas();
  }

  public boolean isActivatedByRedstone(Block block, BlockRedstoneEvent event)
  {
    return ConfigHandler.isRedstoneAllowedForAreas(block.getWorld().getName());
  }

  public void onCommand(CommandSender sender, Command command, String label, String[] args)
  {
    if (!(sender instanceof Player)) {
      return;
    }
    Player player = (Player)sender;

    if ((label.equalsIgnoreCase("fareaListAllow")) && (UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.area")))
      if (args.length >= 1) {
        String areaName = "";
        for (int i = 0; i < args.length - 1; i++) {
          if (i > 0)
            areaName = areaName + " ";
          areaName = areaName + args[i];
        }

        boolean f = false;
        for (int i = this.Areas.size() - 1; i >= 0; i--) {
          if (((Area)this.Areas.get(i)).getAreaName().equalsIgnoreCase(areaName)) {
            String txt = "";
            if (((Area)this.Areas.get(i)).getAllowedBlocks().size() == 0) {
              ChatUtils.printLine(player, ChatColor.GOLD, "All blocktypes are allowed in '" + areaName + "'");
            } else {
              for (int j = 0; j < ((Area)this.Areas.get(i)).getAllowedBlocks().size(); j++) {
                txt = txt + ((AreaBlockType)((Area)this.Areas.get(i)).getAllowedBlocks().get(j)).getTypeID() + ":" + ((AreaBlockType)((Area)this.Areas.get(i)).getAllowedBlocks().get(j)).getData();
                if (j < ((Area)this.Areas.get(i)).getAllowedBlocks().size() - 1) {
                  txt = txt + ", ";
                }
              }
              ChatUtils.printLine(player, ChatColor.GOLD, "Allowed blocktypes in '" + areaName + "':");
              ChatUtils.printLine(player, ChatColor.GRAY, txt);
            }
            f = true;
          }
        }
        if (!f)
          ChatUtils.printError(player, "[FB-Block]", "Area '" + areaName + "' not found!");
      } else {
        ChatUtils.printError(player, "[FB-Block]", "Wrong syntax! Use '/fareaListAllow <areaname>'");
      }
  }

  public void onPlayerInteract(PlayerInteractEvent event, boolean isWallSign, boolean isSignPost)
  {
    if (event.isCancelled()) {
      return;
    }

    if (!event.hasBlock()) {
      return;
    }

    if ((event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && (UtilPermissions.playerCanUseCommand(event.getPlayer(), "falsebook.blocks.area"))) {
      Player player = event.getPlayer();
      Block block = event.getClickedBlock();

      if ((player.getItemInHand().getTypeId() == ConfigHandler.getAreaSelectionTool(block.getWorld().getName())) && (UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.area"))) {
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);
        boolean f = false;
        for (int i = 0; i < this.Selections.size(); i++) {
          if (((AreaSelection)this.Selections.get(i)).player.getName().equalsIgnoreCase(player.getName())) {
            if (((AreaSelection)this.Selections.get(i)).selFirst) {
              ((AreaSelection)this.Selections.get(i)).selP1 = block.getLocation();
              ((AreaSelection)this.Selections.get(i)).selP2 = null;
              ((AreaSelection)this.Selections.get(i)).selFirst = false;
              ChatUtils.printLine(player, ChatColor.LIGHT_PURPLE, "[FB-Area] Position 1 selected");
            } else {
              ((AreaSelection)this.Selections.get(i)).selP2 = block.getLocation();
              ((AreaSelection)this.Selections.get(i)).selFirst = true;
              ChatUtils.printLine(player, ChatColor.LIGHT_PURPLE, "[FB-Area] Position 2 selected");
            }
            f = true;
            break;
          }
        }
        if (!f) {
          AreaSelection sel = new AreaSelection();
          sel.player = player;
          sel.selP1 = block.getLocation();
          sel.selP2 = null;
          sel.selFirst = false;
          this.Selections.add(sel);
          ChatUtils.printLine(player, ChatColor.LIGHT_PURPLE, "[FB-Area] Position 1 selected");
        }

      }
      else if ((block.getType().equals(Material.SIGN_POST)) || (block.getType().equals(Material.WALL_SIGN))) {
        Sign sign = (Sign)block.getState();
        if ((sign.getLine(1).length() > 0) && (
          (sign.getLine(1).equalsIgnoreCase("[Toggle]")) || (sign.getLine(1).equalsIgnoreCase("[Area]")))) {
          event.setUseInteractedBlock(Event.Result.DENY);
          event.setUseItemInHand(Event.Result.DENY);
          event.setCancelled(true);
          check(FalseBookBlockCore.getInstance(), sign, player, block);
        }

      }

    }

    boolean isOp = UtilPermissions.playerCanUseCommand(event.getPlayer(), "falsebook.interact.blocks");
    int blockID = event.getClickedBlock().getTypeId();
    boolean isInteractable = (blockID == Material.WOODEN_DOOR.getId()) || (blockID == Material.DEAD_BUSH.getId()) || (blockID == Material.CAKE_BLOCK.getId()) || (blockID == Material.LEVER.getId()) || (blockID == Material.STONE_BUTTON.getId()) || (blockID == Material.CHEST.getId()) || (blockID == Material.WOOD_DOOR.getId()) || (blockID == Material.IRON_DOOR.getId()) || (blockID == Material.LONG_GRASS.getId()) || (blockID == Material.DISPENSER.getId()) || (blockID == Material.FURNACE.getId()) || (blockID == Material.BURNING_FURNACE.getId()) || (blockID == Material.JUKEBOX.getId()) || (blockID == Material.NOTE_BLOCK.getId()) || (blockID == Material.SEEDS.getId()) || (blockID == Material.SUGAR_CANE_BLOCK.getId());

    if ((isInteractable) && (!isOp))
      for (int i = 0; i < this.Areas.size(); i++) {
        if ((!((Area)this.Areas.get(i)).isInteractBlocked()) || 
          (!((Area)this.Areas.get(i)).isBlockInArea(event.getClickedBlock()))) continue;
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        ChatUtils.printError(event.getPlayer(), "[FB-Block]", "This area is interact-protected!");
        return;
      }
  }

  public void onSignChange(SignChangeEvent event)
  {
    if (event.isCancelled()) {
      return;
    }

    if (event.getLine(1).equalsIgnoreCase("[Toggle]"))
      event.setLine(1, "[Toggle]");
    else {
      event.setLine(1, "[Area]");
    }

    if (!UtilPermissions.playerCanUseCommand(event.getPlayer(), "falsebook.blocks.area")) {
      SignUtils.cancelSignCreation(event, "You are not allowed to build areasigns.");
      return;
    }
    ChatUtils.printSuccess(event.getPlayer(), "[FB-Block]", "Areasign created.");
  }

  public void onBlockPlace(BlockPlaceEvent event)
  {
    if (event.isCancelled()) {
      return;
    }
    Player player = event.getPlayer();
    Block block = event.getBlockPlaced();
    if (!UtilPermissions.playerCanUseCommand(player, "falsebook.destroy.blocks"))
      for (int i = 0; i < this.Areas.size(); i++) {
        if ((!((Area)this.Areas.get(i)).isProtect()) || 
          (!((Area)this.Areas.get(i)).isBlockInArea(block))) continue;
        ChatUtils.printError(player, "[FB-Block]", "This area is protected!");
        event.setBuild(false);
        event.setCancelled(true);
        return;
      }
  }

  public void onBlockBreak(BlockBreakEvent event)
  {
    if (event.isCancelled()) {
      return;
    }

    Player player = event.getPlayer();
    Block block = event.getBlock();
    if (!UtilPermissions.playerCanUseCommand(player, "falsebook.destroy.blocks")) {
      for (int i = 0; i < this.Areas.size(); i++) {
        if ((!((Area)this.Areas.get(i)).isProtect()) || 
          (!((Area)this.Areas.get(i)).isBlockInArea(block))) continue;
        ChatUtils.printError(event.getPlayer(), "[FB-Block]", "This area is protected!");
        event.setCancelled(true);
        return;
      }

    }

    if (event.getBlock().getTypeId() == Material.WALL_SIGN.getId()) {
      Sign sign = (Sign)event.getBlock().getState();

      if (((sign.getLine(1).equalsIgnoreCase("[Area]")) || (sign.getLine(1).equalsIgnoreCase("[Toggle]"))) && 
        (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.area"))) {
        ChatUtils.printError(player, "[FB-Block]", "You are not allowed to destroy areasigns.");
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
    if (isBlockProtected(event.getRetractLocation().getBlock()))
      event.setCancelled(true);
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

  private boolean isBlockProtected(List<Block> blockList)
  {
    try
    {
      for (int i = 0; i < this.Areas.size(); i++) {
        if (((Area)this.Areas.get(i)).isProtect()) {
          for (int j = 0; j < blockList.size(); j++)
            if (((Area)this.Areas.get(i)).isBlockInArea((Block)blockList.get(j)))
              return true;
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }

  private boolean isBlockProtected(Block block)
  {
    try {
      for (int i = 0; i < this.Areas.size(); i++) {
        if ((((Area)this.Areas.get(i)).isProtect()) && 
          (((Area)this.Areas.get(i)).isBlockInArea(block)))
          return true;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }

  private void check(FalseBookBlockCore plugin, Sign sign, Player player, Block block) {
    int[] res = toggle(plugin, sign, block, true, true);
    if ((res[0] >= 0) && ((res[1] == -2) || (res[1] >= 0))) {
      ChatUtils.printLine(player, ChatColor.GOLD, "Area toggled.");
    } else {
      if (res[0] == -1) {
        ChatUtils.printError(player, "[FB-Block]", "Area '" + sign.getLine(0) + "' not found.");
      }
      if (res[1] == -1)
        ChatUtils.printError(player, "[FB-Block]", "Area '" + sign.getLine(2) + "' not found.");
    }
  }

  public int[] toggle(FalseBookBlockCore plugin, Sign sign, Block block, boolean State, boolean playerActivated)
  {
    int[] f = new int[2];
    f[0] = -1;
    f[1] = -2;
    if (sign.getLine(2).length() > 0) {
      f[1] = -1;
    }
    ArrayList one = new ArrayList(); ArrayList two = new ArrayList();
    if ((sign.getLine(1).equalsIgnoreCase("[Toggle]")) || (sign.getLine(1).equalsIgnoreCase("[Area]")))
    {
      for (int i = 0; i < this.Areas.size(); i++) {
        if (((Area)this.Areas.get(i)).getAreaName().equalsIgnoreCase(sign.getLine(0))) {
          one.add((Area)this.Areas.get(i));
          f[0] = i;
        }
        if (((Area)this.Areas.get(i)).getAreaName().equalsIgnoreCase(sign.getLine(2))) {
          two.add((Area)this.Areas.get(i));
          f[1] = i;
        }
      }
      if (playerActivated) {
        for (int i = 0; i < one.size(); i++)
        {
          if (!((Area)one.get(i)).isShow())
          {
            for (int j = 0; j < two.size(); j++) {
              ((Area)two.get(j)).toggle(false, plugin);
            }
            ((Area)one.get(i)).toggle(true, plugin);
          }
          else {
            ((Area)one.get(i)).toggle(false, plugin);

            for (int j = 0; j < two.size(); j++) {
              ((Area)two.get(j)).toggle(true, plugin);
            }
          }
        }
      }
      else if (State)
      {
        for (int j = 0; j < two.size(); j++) {
          ((Area)two.get(j)).toggle(false, plugin);
        }
        for (int j = 0; j < one.size(); j++)
          ((Area)one.get(j)).toggle(true, plugin);
      }
      else {
        for (int j = 0; j < one.size(); j++) {
          ((Area)one.get(j)).toggle(false, plugin);
        }
        for (int j = 0; j < two.size(); j++) {
          ((Area)two.get(j)).toggle(true, plugin);
        }
      }
    }
    if (one.size() > 0)
      saveAreas(((Area)one.get(0)).getAreaName(), true);
    if (two.size() > 0) {
      saveAreas(((Area)two.get(0)).getAreaName(), true);
    }
    return f;
  }

  private boolean loadArea(File FileName) {
    String areaPath = "plugins" + System.getProperty("file.separator") + "FalseBook" + System.getProperty("file.separator") + "areas" + System.getProperty("file.separator");
    try {
      ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(areaPath + FileName.getName())));

      int count = ((Integer)in.readObject()).intValue();
      int i = 0;
      while (i < count) {
        try {
          Area newArea = (Area)in.readObject();
          newArea.initArea();
          this.Areas.add(newArea);
        } catch (Exception e) {
          FalseBookBlockCore.printInConsole("An error occured while loading an Area...");
        }
        i++;
      }
      in.close();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      FalseBookBlockCore.printInConsole("Error while reading " + areaPath + FileName.getName());
    }return false;
  }

  private boolean loadAreas()
  {
    String areaPath = "plugins" + System.getProperty("file.separator") + "FalseBook" + System.getProperty("file.separator") + "areas" + System.getProperty("file.separator");

    File f = new File(areaPath);
    f.mkdirs();
    if (f.listFiles() == null) {
      return true;
    }
    for (File file : f.listFiles()) {
      if (!file.isFile())
        continue;
      loadArea(file);
    }
    FalseBookBlockCore.printInConsole(this.Areas.size() + " Areas loaded.");
    return true;
  }

  public void saveAreas(String FileName, boolean delete)
  {
    String areaPath = "plugins" + System.getProperty("file.separator") + "FalseBook" + System.getProperty("file.separator") + "areas" + System.getProperty("file.separator");

    File folder = new File(areaPath);
    folder.mkdirs();

    if (!delete) {
      File f = new File(areaPath + FileName + ".db");
      f.delete();
    } else {
      try {
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(areaPath + FileName + ".db")));

        int i = 0;
        for (Area area : this.Areas) {
          if (area.getAreaName().equals(FileName))
            i++;
        }
        out.writeObject(Integer.valueOf(i));
        for (Area area : this.Areas) {
          if (area.getAreaName().equals(FileName))
            out.writeObject(area);
        }
        out.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void deleteArea(CommandSender sender, String name) {
    boolean f = false;
    for (int i = this.Areas.size() - 1; i >= 0; i--) {
      if (((Area)this.Areas.get(i)).getAreaName().equalsIgnoreCase(name)) {
        this.Areas.remove(i);
        saveAreas(name, false);
        ChatUtils.printSuccess(sender, "[FB-Block]", "Area '" + name + "' deleted!");
        f = true;
      }
    }
    if (!f)
      ChatUtils.printError(sender, "[FB-Block]", "Area '" + name + "' not found!");
  }

  public void listAreas(CommandSender sender) {
    String str = ChatColor.GOLD + "List of Areas: " + ChatColor.WHITE;
    for (int i = 0; i < this.Areas.size() - 1; i++) {
      str = str + ((Area)this.Areas.get(i)).getAreaName() + ", ";
    }
    if (this.Areas.size() > 0) {
      str = str + ((Area)this.Areas.get(this.Areas.size() - 1)).getAreaName();
    }
    ChatUtils.printLine(sender, ChatColor.GRAY, str);
  }

  public ArrayList<Area> getAreas() {
    return this.Areas;
  }

  public ArrayList<AreaSelection> getSelections() {
    return this.Selections;
  }
}