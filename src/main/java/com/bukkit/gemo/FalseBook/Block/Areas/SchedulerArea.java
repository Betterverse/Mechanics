package com.bukkit.gemo.FalseBook.Block.Areas;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class SchedulerArea
  implements Runnable
{
  private ArrayList<AreaBlock> extraInfo;
  private World w;

  public SchedulerArea(World world, ArrayList<AreaBlock> eInfo)
  {
    this.extraInfo = eInfo;
    this.w = world;
  }

  public void run() {
    for (int c = 0; c < this.extraInfo.size(); c++) {
      if (((AreaBlock)this.extraInfo.get(c)).getInheritedData() == null) {
        continue;
      }
      if (!(((AreaBlock)this.extraInfo.get(c)).getInheritedData() instanceof AreaSign))
        continue;
      AreaSign aSign = (AreaSign)((AreaBlock)this.extraInfo.get(c)).getInheritedData();
      if ((this.w.getBlockAt(((AreaBlock)this.extraInfo.get(c)).getxPos(), ((AreaBlock)this.extraInfo.get(c)).getyPos(), ((AreaBlock)this.extraInfo.get(c)).getzPos()).getType().equals(Material.WALL_SIGN)) || (this.w.getBlockAt(((AreaBlock)this.extraInfo.get(c)).getxPos(), ((AreaBlock)this.extraInfo.get(c)).getyPos(), ((AreaBlock)this.extraInfo.get(c)).getzPos()).getType().equals(Material.SIGN_POST))) {
        Sign sign = (Sign)this.w.getBlockAt(((AreaBlock)this.extraInfo.get(c)).getxPos(), ((AreaBlock)this.extraInfo.get(c)).getyPos(), ((AreaBlock)this.extraInfo.get(c)).getzPos()).getState();
        for (int line = 0; line < 4; line++) {
          sign.setLine(line, aSign.getLines()[line]);
        }
        sign.update(true);
      }
    }
  }
}