package com.bukkit.gemo.FalseBook.Block.Areas;

import java.io.Serializable;
import java.util.ArrayList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AreaChest extends AreaComplexBlock
  implements Serializable
{
  private static final long serialVersionUID = -2940465813606518671L;
  private ArrayList<AreaChestItem> ItemList;

  public AreaChest(Inventory inv)
  {
    this.ItemList = new ArrayList();
    for (int i = 0; i < inv.getSize(); i++)
      this.ItemList.add(new AreaChestItem(inv.getItem(i).getTypeId(), inv.getItem(i).getDurability(), inv.getItem(i).getAmount()));
  }

  public ArrayList<AreaChestItem> getItemList()
  {
    return this.ItemList;
  }

  public void setItemList(ArrayList<AreaChestItem> itemList) {
    this.ItemList = itemList;
  }
}