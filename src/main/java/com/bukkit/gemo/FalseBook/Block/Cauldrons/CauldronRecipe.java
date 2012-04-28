package com.bukkit.gemo.FalseBook.Block.Cauldrons;

import com.bukkit.gemo.utils.FBItemType;
import java.util.ArrayList;
import org.bukkit.inventory.ItemStack;

public class CauldronRecipe
{
  private ArrayList<FBItemType> Ingredients;
  private ArrayList<FBItemType> Results;
  private String Name = "";

  public CauldronRecipe(String Name, ArrayList<FBItemType> ingredients, ArrayList<FBItemType> results) {
    this.Name = Name;
    this.Ingredients = ingredients;
    this.Results = results;
  }

  public boolean verifyCauldron(ArrayList<ItemStack> ents) {
    if ((this.Results.size() < 1) || (this.Ingredients.size() < 1)) {
      return false;
    }
    ArrayList ready = new ArrayList();

    for (int i = 0; i < this.Ingredients.size(); i++) {
      ready.add(Boolean.valueOf(false));
    }

    for (int i = 0; i < ents.size(); i++) {
      int nowCount = ((ItemStack)ents.get(i)).getAmount();

      for (int j = 0; j < this.Ingredients.size(); j++) {
        if ((!((FBItemType)this.Ingredients.get(j)).equals((ItemStack)ents.get(i))) || 
          (nowCount < ((FBItemType)this.Ingredients.get(j)).getAmount())) continue;
        ready.set(j, Boolean.valueOf(true));
      }

    }

    for (int i = 0; i < ready.size(); i++) {
      if (!((Boolean)ready.get(i)).booleanValue())
        return false;
    }
    return true;
  }

  public int getMultiplier(ArrayList<ItemStack> ents) {
    double bestRatio = 1.7976931348623157E+308D;
    for (int i = 0; i < ents.size(); i++) {
      int nowCount = ((ItemStack)ents.get(i)).getAmount();

      for (int j = 0; j < this.Ingredients.size(); j++) {
        if (((FBItemType)this.Ingredients.get(j)).equals((ItemStack)ents.get(i))) {
          double nowRatio = nowCount / ((FBItemType)this.Ingredients.get(j)).getAmount();
          if (nowRatio < bestRatio) {
            bestRatio = nowRatio;
          }
        }
      }
    }
    return (int)bestRatio;
  }

  public ArrayList<ItemStack> getResultItems(ArrayList<ItemStack> ents) {
    ArrayList resultList = new ArrayList();
    for (FBItemType item : this.Results) {
      resultList.add(item.getItemStack());
    }

    int multiplier = getMultiplier(ents);

    for (int i = 0; i < resultList.size(); i++) {
      ((ItemStack)resultList.get(i)).setAmount(((ItemStack)resultList.get(i)).getAmount() * multiplier);
    }

    return resultList;
  }

  public String getName() {
    return this.Name;
  }

  public int getIngredientsSize() {
    return this.Ingredients.size();
  }

  public ItemStack getIngredient(ItemStack thisItem) {
    for (FBItemType itemType : this.Ingredients)
      if (itemType.equals(thisItem))
        return itemType.getItemStack();
    return null;
  }
}