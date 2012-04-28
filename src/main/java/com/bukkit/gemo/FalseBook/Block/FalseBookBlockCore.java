package com.bukkit.gemo.FalseBook.Block;

import com.bukkit.gemo.FalseBook.Block.Config.ConfigHandler;
import com.bukkit.gemo.FalseBook.Block.Handler.WorldHandlerBlock;
import com.bukkit.gemo.FalseBook.Block.Listeners.FalseBookBlockListener;
import com.bukkit.gemo.FalseBook.Block.Mechanics.BlockMechanicHandler;
import com.bukkit.gemo.FalseBook.Block.commands.cmdDelFArea;
import com.bukkit.gemo.FalseBook.Block.commands.cmdFArea;
import com.bukkit.gemo.FalseBook.Block.commands.cmdFAreaAllow;
import com.bukkit.gemo.FalseBook.Block.commands.cmdFAreaToggle;
import com.bukkit.gemo.FalseBook.Block.commands.cmdFBBlock;
import com.bukkit.gemo.FalseBook.Block.commands.cmdListFArea;
import com.bukkit.gemo.FalseBook.Block.commands.cmdReload;
import com.bukkit.gemo.FalseBook.Block.commands.cmdReloadSettings;
import com.bukkit.gemo.FalseBook.Core.FalseBookCore;
import com.bukkit.gemo.commands.CommandList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

public class FalseBookBlockCore
{
  public FalseBookCore plugin;
	public FalseBookBlockCore(FalseBookCore plugin) {
		this.plugin = plugin;
	}
	
	private FalseBookCore core = null;
  private String pluginName;
  private FalseBookBlockListener listener;
  private CommandList commandList;
  private WorldHandlerBlock worldHandler = null;
  private BlockMechanicHandler mechHandler = null;

  private static FalseBookBlockCore instance = null;

  public static void printInConsole(String str)
  {
    System.out.println("[ FalseBook Block ] " + str);
  }

  public void onDisable()
  {
    if (this.core != null) {
      this.mechHandler.onUnload();
      printInConsole(this.pluginName + " disabled");
    }
  }

  public void onEnable()
  {

    instance = this;
    loadVersion();

    new ConfigHandler();
    ConfigHandler.loadWorldSettings();

    initCommands();

    this.worldHandler = new WorldHandlerBlock();
    loadWorldSettings();

    this.listener = new FalseBookBlockListener(this.worldHandler);

    plugin.getServer().getPluginManager().registerEvents(this.listener, plugin);

    this.mechHandler = new BlockMechanicHandler();
    this.mechHandler.registerBlockMechanics(this);
    this.mechHandler.onLoad();

    printInConsole(this.pluginName + " enabled");
  }

  private void initCommands()
  {
    com.bukkit.gemo.commands.Command[] commands = { 
      new cmdFArea("[FB-Block]", "/farea", "<Areaname>", ""), 
      new cmdDelFArea("[FB-Block]", "/delfarea", "<Areaname>", ""), 
      new cmdListFArea("[FB-Block]", "/listfarea", "", ""), 
      new cmdFAreaAllow("[FB-Block]", "/fareaallow", "<ItemList>", ""), 
      new cmdFAreaAllow("[FB-Block]", "/farealistallow", "<Areaname>", ""), 
      new cmdFAreaToggle("[FB-Block]", "/fareatoggle", "<Autosave|Interact|Protect> <Areaname>", ""), 
      new cmdFBBlock("[FB-Block]", "/fbblock", "", "", false, new com.bukkit.gemo.commands.Command[] { 
      new cmdReload("[FB-Block]", "reload", "", ""), 
      new cmdReloadSettings("[FB-Block]", "reloadsettings", "", "") }) };

    this.commandList = new CommandList("[FB-Chat]", commands);
  }

  public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args)
  {
      this.commandList.handleCommand(sender, label, args);
    return true;
  }

  private void loadVersion()
  {
    PluginDescriptionFile pdfFile = plugin.getDescription();
    this.pluginName = (pdfFile.getName() + " v" + pdfFile.getVersion() + " by GeMo");
  }

  public BlockMechanicHandler getMechanicHandler()
  {
    return this.mechHandler;
  }

  public static FalseBookBlockCore getInstance() {
    return instance;
  }

  public void loadWorldSettings()
  {
    for (int i = 0; i < Bukkit.getWorlds().size(); i++)
      getOrCreateSettings(((World)Bukkit.getWorlds().get(i)).getName());
  }

  public void getOrCreateSettings(String worldName)
  {
    this.worldHandler.getWorld(worldName);
  }
}