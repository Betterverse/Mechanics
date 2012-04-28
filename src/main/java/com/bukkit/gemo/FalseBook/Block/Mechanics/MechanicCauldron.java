package com.bukkit.gemo.FalseBook.Block.Mechanics;

import com.bukkit.gemo.FalseBook.Block.Cauldrons.CauldronRecipe;
import com.bukkit.gemo.FalseBook.Block.Config.ConfigHandler;
import com.bukkit.gemo.FalseBook.Block.FalseBookBlockCore;
import com.bukkit.gemo.FalseBook.Mechanics.MechanicListener;
import com.bukkit.gemo.utils.BlockUtils;
import com.bukkit.gemo.utils.SignUtils;
import com.bukkit.gemo.utils.UtilPermissions;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class MechanicCauldron extends MechanicListener
{
  private ArrayList<CauldronRecipe> Recipes = new ArrayList();
  private HashMap<String, Long> cauldronsCooldown = new HashMap();

  public MechanicCauldron(FalseBookBlockCore plugin) {
    plugin.getMechanicHandler().registerEvent(PlayerInteractEvent.class, this);
  }

  public void onLoad()
  {
    loadCauldrons("FalseBook" + System.getProperty("file.separator") + "Cauldrons.txt");
  }

  public void reloadMechanic()
  {
    this.Recipes = new ArrayList();
    loadCauldrons("FalseBook" + System.getProperty("file.separator") + "Cauldrons.txt");
  }

  public void onPlayerInteract(PlayerInteractEvent event, boolean isWallSign, boolean isSignPost)
  {
    if (event.isCancelled()) {
      return;
    }

    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }
    Player player = event.getPlayer();
    Block block = event.getClickedBlock();

    if (isCauldronSpace(event.getClickedBlock()))
    {
      event.setUseInteractedBlock(Event.Result.DENY);
      event.setUseItemInHand(Event.Result.DENY);
      event.setCancelled(true);
      handleCauldron(event.getClickedBlock(), player);
    } else if ((ConfigHandler.isAllowMinecraftCauldron(block.getWorld().getName())) && (event.getClickedBlock().getTypeId() == Material.CAULDRON.getId()))
    {
      handleCauldron(event.getClickedBlock().getRelative(0, -1, 0), player);
    }
  }

  private boolean loadCauldrons(String FileName)
  {
    File file = new File("plugins" + System.getProperty("file.separator") + FileName);
    if (file.exists()) {
      try {
        FileInputStream fstream = new FileInputStream(file);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine = "";

        while ((strLine = br.readLine()) != null) {
          strLine = strLine.trim();
          String[] split = strLine.split(";");
          if (split.length == 3) {
            ArrayList iList = SignUtils.parseLineToItemList(split[1], ",", false);
            ArrayList rList = SignUtils.parseLineToItemList(split[2], ",", false);
            if ((((iList != null ? 1 : 0) & (rList != null ? 1 : 0)) != 0) && (iList.size() > 0) && (rList.size() > 0)) {
              this.Recipes.add(new CauldronRecipe(split[0], iList, rList));
            }
          }

        }

        FalseBookBlockCore.printInConsole(this.Recipes.size() + " Cauldrons successfully loaded.");
        return true;
      }
      catch (Exception e) {
        e.printStackTrace();
        FalseBookBlockCore.printInConsole("Error while reading file: plugins/" + FileName);
        return false;
      }
    }
    FalseBookBlockCore.printInConsole("No Cauldrons loaded!");
    return false;
  }

  private boolean isCauldronSpace(Block block)
  {
    return (block.getType().equals(Material.GLASS)) && ((block.getRelative(0, -1, 0).getType().equals(Material.LAVA)) || (block.getRelative(0, -1, 0).getType().equals(Material.STATIONARY_LAVA))) && (block.getRelative(1, 0, 0).getType().equals(Material.STONE)) && (block.getRelative(-1, 0, 0).getType().equals(Material.STONE)) && (block.getRelative(0, 0, 1).getType().equals(Material.STONE)) && (block.getRelative(0, 0, -1).getType().equals(Material.STONE)) && (block.getRelative(1, 1, 0).getType().equals(Material.STONE)) && (block.getRelative(-1, 1, 0).getType().equals(Material.STONE)) && (block.getRelative(0, 1, 1).getType().equals(Material.STONE)) && (block.getRelative(0, 1, -1).getType().equals(Material.STONE)) && (block.getRelative(1, -1, 0).getType().equals(Material.STONE)) && (block.getRelative(-1, -1, 0).getType().equals(Material.STONE)) && (block.getRelative(0, -1, 1).getType().equals(Material.STONE)) && (block.getRelative(0, -1, -1).getType().equals(Material.STONE));
  }

  private boolean handleCauldron(Block block, Player player)
  {
    long thisTime = System.currentTimeMillis();
    if (!this.cauldronsCooldown.containsKey(block.getLocation().toString())) {
      this.cauldronsCooldown.put(block.getLocation().toString(), Long.valueOf(thisTime - (ConfigHandler.getCauldronCoolDownTime(block.getWorld().getName()) + 1) * 1000));
    }
    if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.cauldron.use")) {
      player.sendMessage(ChatColor.RED + "You are not allowed to use Cauldrons!");
      return false;
    }

    if (thisTime < ((Long)this.cauldronsCooldown.get(block.getLocation().toString())).longValue() + ConfigHandler.getCauldronCoolDownTime(block.getWorld().getName()) * 1000) {
      player.sendMessage(ChatColor.RED + "This cauldron is cooling down.");
      return false;
    }

    this.cauldronsCooldown.put(block.getLocation().toString(), Long.valueOf(thisTime));

    ArrayList itemList = (ArrayList)block.getWorld().getEntitiesByClass(Item.class);

    for (int i = itemList.size() - 1; i >= 0; i--) {
      Location loc = ((Item)itemList.get(i)).getLocation();
      if (!BlockUtils.LocationEquals(loc, block.getRelative(0, 1, 0).getLocation())) {
        itemList.remove(i);
      }
    }

    if (itemList.size() < 1) {
      return false;
    }

    ArrayList itemStackList = new ArrayList();
    for (int i = 0; i < itemList.size(); i++) {
      ItemStack newStack = ((Item)itemList.get(i)).getItemStack().clone();
      boolean f = false;
      for (int j = 0; j < itemStackList.size(); j++) {
        if ((newStack.getTypeId() == ((ItemStack)itemStackList.get(j)).getTypeId()) && (newStack.getDurability() == ((ItemStack)itemStackList.get(j)).getDurability())) {
          ((ItemStack)itemStackList.get(j)).setAmount(((ItemStack)itemStackList.get(j)).getAmount() + newStack.getAmount());
          f = true;
        }
      }
      if (!f) {
        itemStackList.add(newStack);
      }

    }

    HashMap<Integer, CauldronRecipe> possibleRecipes = new HashMap();
    boolean done = false;
    ArrayList<ItemStack> result = null;
    int VALUE = -1;
    for (int i = 0; i < this.Recipes.size(); i++) {
      if (((CauldronRecipe)this.Recipes.get(i)).verifyCauldron(itemStackList)) {
        possibleRecipes.put(Integer.valueOf(i), (CauldronRecipe)this.Recipes.get(i));
      }
    }

    int maximumIndex = -1;
    int maxIngAmount = -1;
    for (Map.Entry recipe : possibleRecipes.entrySet()) {
      if (possibleRecipes.size() == 1) {
        done = true;
        VALUE = ((Integer)recipe.getKey()).intValue();
        break;
      }
      if (maxIngAmount < ((CauldronRecipe)recipe.getValue()).getIngredientsSize()) {
        maxIngAmount = ((CauldronRecipe)recipe.getValue()).getIngredientsSize();
        maximumIndex = ((Integer)recipe.getKey()).intValue();
      }

    }

    if (possibleRecipes.size() > 1) {
      done = true;
      VALUE = maximumIndex;
    }

    if (done)
    {
      int multiplier = ((CauldronRecipe)this.Recipes.get(VALUE)).getMultiplier(itemStackList);

      result = ((CauldronRecipe)this.Recipes.get(VALUE)).getResultItems(itemStackList);
      player.sendMessage(ChatColor.GOLD + "*POOOOOOF* You have made '" + ((CauldronRecipe)this.Recipes.get(VALUE)).getName() + "'.");
      for (ItemStack res : result) {
        player.getWorld().dropItem(((Item)itemList.get(0)).getLocation(), res.clone());
      }

      HashMap<String, ItemStack> returnItems = new HashMap();
      ItemStack item;
      for (int i = itemList.size() - 1; i >= 0; i--) {
        item = ((Item)itemList.get(i)).getItemStack().clone();
        if (returnItems.containsKey(item.getTypeId() + ":" + item.getDurability())) {
          ItemStack thisItem = (ItemStack)returnItems.get(item.getTypeId() + ":" + item.getDurability());
          thisItem.setAmount(thisItem.getAmount() + item.getAmount());
        } else {
          returnItems.put(item.getTypeId() + ":" + item.getDurability(), item);
        }

      }

      for (ItemStack thisItem : returnItems.values()) {
        ItemStack thisI = ((CauldronRecipe)this.Recipes.get(VALUE)).getIngredient(thisItem);
        if (thisI != null) {
          thisItem.setAmount(thisItem.getAmount() - multiplier * thisI.getAmount());
        }

      }

      for (int i = itemList.size() - 1; i >= 0; i--) {
        ((Item)itemList.get(i)).remove();
      }

      for (ItemStack ittem : returnItems.values()) {
        if (ittem.getAmount() > 0) {
          player.getWorld().dropItem(((Item)itemList.get(0)).getLocation(), ittem);
        }
      }
      result.clear();
    }
    else {
      player.sendMessage(ChatColor.RED + "Recipe not found.");
    }

    itemStackList.clear();
    return done;
  }
}