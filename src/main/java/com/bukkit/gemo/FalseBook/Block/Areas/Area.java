package com.bukkit.gemo.FalseBook.Block.Areas;

import com.bukkit.gemo.FalseBook.Block.FalseBookBlockCore;
import com.bukkit.gemo.utils.BlockUtils;
import java.io.Serializable;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

public class Area
  implements Serializable
{
  private static final long serialVersionUID = -1879686687354057051L;
  private ArrayList<AreaBlock> bList;
  private String worldName;
  private AreaLocation pos1;
  private AreaLocation pos2;
  private boolean show = true;
  private boolean autoSave = true;
  private boolean protect = false;
  private boolean blockInteract = false;
  private String AreaName = "";
  private ArrayList<AreaBlockType> AllowedBlocks;

  public Area(World w, String Name, Location selP1, Location selP2)
  {
    this.AreaName = Name;
    this.bList = new ArrayList();

    if (selP1.getX() > selP2.getX())
    {
      double xVar = selP2.getX();
      selP2.setX(selP1.getX());
      selP1.setX(xVar);
    }
    if (selP1.getY() > selP2.getY())
    {
      double yVar = selP2.getY();
      selP2.setY(selP1.getY());
      selP1.setY(yVar);
    }
    if (selP1.getZ() > selP2.getZ())
    {
      double zVar = selP2.getZ();
      selP2.setZ(selP1.getZ());
      selP1.setZ(zVar);
    }
    this.worldName = w.getName();
    this.pos1 = new AreaLocation(selP1);
    this.pos2 = new AreaLocation(selP2);
    updateArea(w);
  }

  public void updateArea(World world)
  {
    this.bList.clear();
    for (int x = this.pos1.getBlockX(); x <= this.pos2.getBlockX(); x++)
      for (int z = this.pos1.getBlockZ(); z <= this.pos2.getBlockZ(); z++)
        for (int y = this.pos1.getBlockY(); y <= this.pos2.getBlockY(); y++) {
          AreaBlock newBlock = new AreaBlock(x, y, z, world.getBlockAt(x, y, z).getTypeId(), world.getBlockAt(x, y, z).getData());
          this.bList.add(newBlock);
          if ((BlockUtils.isComplexBlock(world.getBlockAt(x, y, z))) || (world.getBlockAt(x, y, z).getType().equals(Material.CHEST)))
            if ((world.getBlockAt(x, y, z).getType().equals(Material.WALL_SIGN)) || (world.getBlockAt(x, y, z).getType().equals(Material.SIGN_POST))) {
              Sign sign = (Sign)world.getBlockAt(x, y, z).getState();
              newBlock.setInheritedData(new AreaSign(sign.getLines()));
            } else if (world.getBlockAt(x, y, z).getType().equals(Material.CHEST)) {
              Chest chest = (Chest)world.getBlockAt(x, y, z).getState();
              newBlock.setInheritedData(new AreaChest(chest.getInventory()));
            }
        }
  }

  public boolean isBlockInArea(Block block)
  {
    return (block.getX() >= this.pos1.getBlockX()) && (block.getX() <= this.pos2.getBlockX()) && (block.getY() >= this.pos1.getBlockY()) && (block.getY() <= this.pos2.getBlockY()) && (block.getZ() >= this.pos1.getBlockZ()) && (block.getZ() <= this.pos2.getBlockZ()) && (block.getWorld().getName().equalsIgnoreCase(this.pos1.getWorldName()));
  }

  public void toggle(FalseBookBlockCore plugin)
  {
    if (this.show)
      toggle(false, plugin);
    else
      toggle(true, plugin);
  }

  public void initArea()
  {
    if (this.AllowedBlocks == null)
      this.AllowedBlocks = new ArrayList();
  }

  public boolean isInAllowed(int ID, byte Data)
  {
    for (int i = 0; i < this.AllowedBlocks.size(); i++) {
      if ((((AreaBlockType)this.AllowedBlocks.get(i)).getTypeID() == ID) && (((AreaBlockType)this.AllowedBlocks.get(i)).getData() == Data)) {
        return true;
      }
    }
    return false;
  }

  public int getInAllowed(int ID, byte Data)
  {
    for (int i = 0; i < this.AllowedBlocks.size(); i++) {
      if ((((AreaBlockType)this.AllowedBlocks.get(i)).getTypeID() == ID) && (((AreaBlockType)this.AllowedBlocks.get(i)).getData() == Data)) {
        return i;
      }
    }
    return -1;
  }

  public boolean isInAllowed(AreaBlockType thisBlock)
  {
    return isInAllowed(thisBlock.getTypeID(), thisBlock.getData());
  }

  public void toggle(boolean on, FalseBookBlockCore plugin)
  {
    if ((this.show == on) || (isProtect())) {
      return;
    }
    initArea();

    this.show = on;
    ArrayList QueuedBlocks = new ArrayList();

    World world = plugin.plugin.getServer().getWorld(this.worldName);

    if (on)
    {
      for (int i = 0; i < this.bList.size(); i++) {
        if ((((AreaBlock)this.bList.get(i)).getTypeID() != Material.CHEST.getId()) && (!BlockUtils.isComplexBlock(((AreaBlock)this.bList.get(i)).getTypeID())))
        {
          world.getBlockAt(((AreaBlock)this.bList.get(i)).getxPos(), ((AreaBlock)this.bList.get(i)).getyPos(), ((AreaBlock)this.bList.get(i)).getzPos()).setTypeIdAndData(((AreaBlock)this.bList.get(i)).getTypeID(), ((AreaBlock)this.bList.get(i)).getData(), true);
        }
        else {
          QueuedBlocks.add((AreaBlock)this.bList.get(i));
        }
      }
      if (this.autoSave)
        this.bList.clear();
    }
    else if (!on)
    {
      if (isAutoSave()) {
        updateArea(world);
      }

      for (int x = this.pos1.getBlockX(); x <= this.pos2.getBlockX(); x++) {
        for (int z = this.pos1.getBlockZ(); z <= this.pos2.getBlockZ(); z++) {
          for (int y = this.pos1.getBlockY(); y <= this.pos2.getBlockY(); y++) {
            AreaBlockType thisBlock = new AreaBlockType(world.getBlockAt(x, y, z).getTypeId(), world.getBlockAt(x, y, z).getData());
            if ((isInAllowed(thisBlock)) || (this.AllowedBlocks.size() == 0)) {
              if ((BlockUtils.isComplexBlock(world.getBlockAt(x, y, z).getTypeId())) && (world.getBlockAt(x, y, z).getTypeId() != Material.CHEST.getId()))
              {
                world.getBlockAt(x, y, z).setTypeIdAndData(Material.AIR.getId(), (byte)0, true);
              }
              else {
                QueuedBlocks.add(new AreaBlock(x, y, z, world.getBlockAt(x, y, z).getTypeId(), world.getBlockAt(x, y, z).getData()));
              }
            }
          }
        }
      }

    }

    if (QueuedBlocks.size() > 0) {
      if (on)
      {
        for (int i = 0; i < QueuedBlocks.size(); i++)
        {
          AreaBlock aBlock = (AreaBlock)QueuedBlocks.get(i);
          world.getBlockAt(aBlock.getxPos(), aBlock.getyPos(), aBlock.getzPos()).setTypeIdAndData(aBlock.getTypeID(), aBlock.getData(), true);

          if (aBlock.getTypeID() == Material.CHEST.getId()) {
            Chest chest = (Chest)world.getBlockAt(aBlock.getxPos(), aBlock.getyPos(), aBlock.getzPos()).getState();
            AreaChest aChest = (AreaChest)((AreaBlock)QueuedBlocks.get(i)).getInheritedData();
            ItemStack[] items = new ItemStack[aChest.getItemList().size()];

            chest.getInventory().clear();
            for (int j = 0; j < aChest.getItemList().size(); j++) {
              items[j] = new ItemStack(((AreaChestItem)aChest.getItemList().get(j)).getTypeID(), ((AreaChestItem)aChest.getItemList().get(j)).getCount(), ((AreaChestItem)aChest.getItemList().get(j)).getData());
              items[j].setAmount(((AreaChestItem)aChest.getItemList().get(j)).getCount());
            }
            chest.getInventory().setContents(items);
          }
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin.plugin, new SchedulerArea(world, this.bList));
      } else if (!on)
      {
        for (int i = 0; i < QueuedBlocks.size(); i++)
        {
          AreaBlock aBlock = (AreaBlock)QueuedBlocks.get(i);
          if (world.getBlockAt(aBlock.getxPos(), aBlock.getyPos(), aBlock.getzPos()).getType().equals(Material.CHEST)) {
            Chest chest = (Chest)world.getBlockAt(aBlock.getxPos(), aBlock.getyPos(), aBlock.getzPos()).getState();
            chest.getInventory().clear();
          }

          world.getBlockAt(aBlock.getxPos(), aBlock.getyPos(), aBlock.getzPos()).setTypeIdAndData(Material.AIR.getId(), (byte) 0, true);
        }
      }
    }
    this.show = on;
  }

  public String getAreaName() {
    return this.AreaName;
  }

  public boolean isShow() {
    return this.show;
  }

  public void setAutoSave(boolean autoSave) {
    this.autoSave = autoSave;
  }

  public boolean isAutoSave() {
    return this.autoSave;
  }

  public void setProtect(boolean protect) {
    this.protect = protect;
  }

  public boolean isProtect() {
    return this.protect;
  }

  public boolean isInteractBlocked() {
    return this.blockInteract;
  }

  public void setInteractBlocked(boolean blockInteract) {
    this.blockInteract = blockInteract;
  }

  public ArrayList<AreaBlock> getbList() {
    return this.bList;
  }

  public void setbList(ArrayList<AreaBlock> bList) {
    this.bList = bList;
  }

  public String getWorldName() {
    return this.worldName;
  }

  public void setAllowedBlocks(ArrayList<AreaBlockType> allowedBlocks)
  {
    this.AllowedBlocks = allowedBlocks;
  }

  public ArrayList<AreaBlockType> getAllowedBlocks()
  {
    return this.AllowedBlocks;
  }
}