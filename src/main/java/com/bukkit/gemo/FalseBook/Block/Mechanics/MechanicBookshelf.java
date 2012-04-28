package com.bukkit.gemo.FalseBook.Block.Mechanics;

import com.bukkit.gemo.FalseBook.Block.Config.ConfigHandler;
import com.bukkit.gemo.FalseBook.Block.FalseBookBlockCore;
import com.bukkit.gemo.FalseBook.Mechanics.MechanicListener;
import com.bukkit.gemo.utils.UtilPermissions;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class MechanicBookshelf extends MechanicListener
{
  private ArrayList<String> books;

  public MechanicBookshelf(FalseBookBlockCore plugin)
  {
    plugin.getMechanicHandler().registerEvent(PlayerInteractEvent.class, this);
  }

  public void onLoad()
  {
    loadBooksFromFile();
  }

  public void reloadMechanic()
  {
    loadBooksFromFile();
  }

  private void loadBooksFromFile() {
    if (this.books != null)
      this.books.clear();
    else {
      this.books = new ArrayList();
    }
    File f = new File("plugins" + System.getProperty("file.separator") + "FalseBook" + System.getProperty("file.separator") + "books.txt");
    if (!f.exists()) {
      FalseBookBlockCore.printInConsole("No Books loaded!");
      return;
    }
    try
    {
      BufferedReader in = new BufferedReader(new FileReader("plugins" + System.getProperty("file.separator") + "FalseBook" + System.getProperty("file.separator") + "books.txt"));
      String zeile = null;
      while ((zeile = in.readLine()) != null) {
        this.books.add(zeile);
      }

      FalseBookBlockCore.printInConsole(this.books.size() + " Books loaded!");
    } catch (Exception e) {
      FalseBookBlockCore.printInConsole("Error while reading 'plugins/FalseBook/books.txt'");
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

    if (!ConfigHandler.isReadBooks(event.getClickedBlock().getWorld().getName())) {
      return;
    }

    if (event.getClickedBlock().getTypeId() != Material.BOOKSHELF.getId()) {
      return;
    }

    Player player = event.getPlayer();
    if (player.getItemInHand().getTypeId() != 0) {
      return;
    }

    if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.readbooks")) {
      player.sendMessage(ChatColor.RED + "You are not allowed to read bookshelfs.");
      return;
    }
    if (this.books.size() < 1) {
      return;
    }

    Random random = new Random();
    player.sendMessage(ChatColor.GRAY + "You pick up a book...");
    player.sendMessage(ChatColor.DARK_GREEN + (String)this.books.get(random.nextInt(this.books.size())));
  }
}