package com.bukkit.gemo.FalseBook.Block.Areas;

import java.io.Serializable;
import org.bukkit.Location;
import org.bukkit.World;

public class AreaLocation
  implements Serializable
{
  private static final long serialVersionUID = 381647817790289522L;
  private int BlockX;
  private int BlockY;
  private int BlockZ;
  private String worldName = "";

  public AreaLocation(Location loc) {
    setBlockX(loc.getBlockX());
    setBlockY(loc.getBlockY());
    setBlockZ(loc.getBlockZ());
    setWorldName(loc.getWorld().getName());
  }

  public int getBlockX() {
    return this.BlockX;
  }
  public void setBlockX(int blockX) {
    this.BlockX = blockX;
  }
  public int getBlockY() {
    return this.BlockY;
  }
  public void setBlockY(int blockY) {
    this.BlockY = blockY;
  }
  public int getBlockZ() {
    return this.BlockZ;
  }
  public void setBlockZ(int blockZ) {
    this.BlockZ = blockZ;
  }

  public String getWorldName() {
    return this.worldName;
  }

  public void setWorldName(String worldName) {
    this.worldName = worldName;
  }
}