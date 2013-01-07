package net.betterverse.mechanics.mechanics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;

public class Bridge extends Mechanic {

    //The config variables (I(Zippo007) might change these to not being globals.)
	int maxDist = 20;
	int maxWidth = 5;
	List<Integer> allowedMaterials = new ArrayList<Integer>();
	int maxChestDist = 2;
	boolean needMat = true;
	
	//Creates the bridge, and its text
    public Bridge(String text) {
        super(text);
    }

    //Returns whether or not a player can create a made mechanic based on their permission
    @Override
    public boolean canCreate(Player player) {
        return player.hasPermission("mechanics.bridge");
    }
    
    //Returns whether or not a player can use a made mechanic based on their permission
    @Override
    public boolean canUse(Player player) {
        return true;
    }

    //Get the name of this mechanic Bridge
    @Override
    public String getName() {
        return "Bridge";
    }
    
	//This is where we check things:
	//	If you can use the bridge you clicked: No on [Bridge End] yes on [Bridge].
	//	If the block you want to use is the correct type.
	//  Gets where to place the blocks width, and height.
	//  Looks for a chest of correct materials.
	//	If we run out of materials we return early, after making part of the bridge.
	//	Otherwise makes the bridge fully.
    @Override
    public void activate(Player player, Block block) {
    	
    	if(text.equals("[Bridge End]")){
    		player.sendMessage(ChatColor.RED + "You can't directly use this type of bridge!");
    		return;
    	}
    	loadConfig();
        BlockFace toCheck = ((Sign) block.getState().getData()).getAttachedFace();
        BlockFace dir = getBack(block);
        Block bridgeBlock = block.getRelative(toCheck);
        Block tmpBlock = bridgeBlock;
        Chest chest;
        if(bridgeBlock.getRelative(dir).getType() == Material.AIR) {
        	chest = findChest( block, bridgeBlock, maxChestDist);
        }
        else{
        	chest = findChest( block, null, maxChestDist);
        }
        boolean checkMat = false;
        int widthLeft = getWidth(bridgeBlock, getLeft(block), maxWidth);
        maxWidth = maxWidth - widthLeft;
        int widthRight = getWidth(bridgeBlock, getRight(block), maxWidth);
        int dist = getDistToEnd(block, getBack(block));
        int error = 0;
        
        checkMat = allowedMaterials.contains(bridgeBlock.getTypeId());

        if(!checkMat){
        	player.sendMessage(ChatColor.RED + "You don't have the right material for this, You tried  " + bridgeBlock.getType().getId());
        	return;
        }
        if(dist == -1) {
        	player.sendMessage(ChatColor.RED + "No matching sign in the distance searched! Max Distance: " + maxDist);
        	return;
        }
        if(chest == null) {
        	player.sendMessage(ChatColor.RED + "No chest with correct materials");
        	return;
        }      
        for( int j=1; j<=widthLeft; j++) {
        	tmpBlock = tmpBlock.getRelative(getLeft(block));
        	error = makeBridge(tmpBlock, dir, dist, chest);
        }
        if(error != -1) {
        	error = makeBridge(bridgeBlock, dir, dist, chest);
	        if(error != -1) {
		        tmpBlock = bridgeBlock;
		        for( int j=1; j<=widthRight; j++) {
		        	tmpBlock = tmpBlock.getRelative(getRight(block));
		        	error = makeBridge(tmpBlock, dir, dist, chest );
		        }
	        }
        }
        if(error == 0) {
        	player.sendMessage(ChatColor.GREEN + "Switched bridge!");
        }
        else if(error == -1) {
        	player.sendMessage(ChatColor.YELLOW + "Not enough blocks in chest to finish!");
        }
    }
    
    
    //This method make the bridge, based off of how far it needs to go/ unless it hits a different material then it was passed as the type to use
    private int makeBridge(Block bridgeBlock, BlockFace dir, int dist, Chest chest) {
    	boolean create = bridgeBlock.getRelative(dir).getType() == Material.AIR;
    	Block chestblock = bridgeBlock;
    	Material toSet = bridgeBlock.getType();
        bridgeBlock = bridgeBlock.getRelative(dir);
    	for( int r=1; r<dist; r++ ) {
    		boolean didremove = false;
    		if(create) {
    			ItemStack[] toRemoveFrom = chest.getBlockInventory().getContents();
    			for(ItemStack i : toRemoveFrom) {
         			if(i != null) {
         				if(i.getAmount()-1 >= 0 && i.getTypeId() == chestblock.getTypeId()){
         					i.setAmount((i.getAmount()-1));
         					didremove = true;
         					break;
         				}
         			}
         		}
    			if(didremove) {
    				bridgeBlock.setType(toSet);
    				chest.getBlockInventory().setContents(toRemoveFrom); 
    				bridgeBlock = bridgeBlock.getRelative(dir);
    			}
    			else {
    				return -1;
    			}
         	}
         	else {
         		if(bridgeBlock.getType() != Material.AIR) {
	         		ItemStack[] toAddto = chest.getBlockInventory().getContents();
	         		boolean check = true;
	         		for(ItemStack i : toAddto) {
	         			if(i != null) {
	         				if(i.getAmount() < 64 && i.getTypeId() == chestblock.getTypeId()){
	         					i.setAmount((i.getAmount()+1));
	         					check = false;
	         					break;
	         				}
	         			}
	         		}
	         		if(check) {
	         			int f=0;
	         			for(ItemStack i : toAddto) {
	             			if(i == null) {
	             				i = new ItemStack(chestblock.getTypeId(),1);
	             				toAddto[f] = i;
	             				break;
	             			}
	             			f++;
	             		}
	         		}
	         		chest.getBlockInventory().setContents(toAddto); 
         		}
         		bridgeBlock.setType(Material.AIR);
         		bridgeBlock = bridgeBlock.getRelative(dir);
         	}
        }
    	return 0;
    }

    //This grabs the distance between the signs, and converts it into the necessary blocks for the making of the actual bridge, doesn't require for the space inbetween to be clear
    private int getDistToEnd( Block start, BlockFace dir ) {
    	boolean check = false;
    	int dist = 0;
    	
    	for( int i=0; i <= maxDist; i++) {
    		if(start.getRelative(dir).getType() == Material.WALL_SIGN){
    			check = true;
    			dist--;
    			break;
    		}
    		else if(start.getRelative(dir).getType() == Material.SIGN || start.getRelative(dir).getType() == Material.SIGN_POST) {
    			if(text.equals("[Bridge]") || text.equals("[Bridge End]")){
    				dist++;
    				check = true;
    				break;
    			}
    		}
    		start = start.getRelative(dir);
    		dist++;
    	}
    	if(check) {
    		return dist;
    	}
    	else {
    		return -1;
    	}
    }
    
  //Loads all of our configs (Will be changed to do it all its self, when I(Zippo007) get to it....soonish)
    @Override
    public void loadConfig(){
    	FileConfiguration config;
    	File configFile;
    	allowedMaterials.add(1);
    	allowedMaterials.add(5);
    	allowedMaterials.add(7);
    	allowedMaterials.add(17);
    	allowedMaterials.add(20);
    	try{
    		configFile = new File( plugin.getDataFolder(), "config.yml");
	    	config = plugin.getConfig();
	    	
	    	if(!config.contains("mechanic.bridge.maxDist")) {
	    		config.set("mechanic.bridge.maxDist", 20);
	    		maxDist = 20;
	    	}
	    	else{
	    		maxDist = config.getInt("mechanic.bridge.maxDist");
	    	}
	    	if(!config.contains("mechanic.bridge.maxWidth")) {
	    		config.set("mechanic.bridge.maxWidth", 5);
	    		maxWidth = 5;
	    	}
	    	else{
	    		maxWidth = config.getInt("mechanic.bridge.maxWidth");
	    	}
	    	if(!config.contains("mechanic.bridge.maxChestDist")) {
	    		config.set("mechanic.bridge.maxChestDist", 2);
	    		maxChestDist = 2;
	    	}
	    	else{
	    		maxChestDist = config.getInt("mechanic.bridge.maxChestDist");
	    	}
	    	if(!config.contains("mechanic.bridge.needMat")) {
    			config.set("mechanic.bridge.needMat", true);
    			needMat = true;
    		}
    		else{
    			needMat = config.getBoolean("mechanic.bridge.needMat");
    		}
	    	if(!config.contains("mechanic.bridge.allowedMaterials")) {
    			config.set("mechanic.bridge.allowedMaterials", allowedMaterials);
    		}
    		else{
    			allowedMaterials = (List<Integer>) config.getList("mechanic.bridge.allowedMaterials");
    		}
	    		plugin.saveConfig();
    	}catch(Exception e1){
    		e1.printStackTrace();
    	}
    }

	@Override
	public void activateRedstone(Block block) {
		if(text.equals("[Bridge End]")){
    		return;
    	}
    	loadConfig();
        BlockFace toCheck = ((Sign) block.getState().getData()).getAttachedFace();
        BlockFace dir = getBack(block);
        Block bridgeBlock = block.getRelative(toCheck);
        Block tmpBlock = bridgeBlock;
        Chest chest;
        if(bridgeBlock.getRelative(dir).getType() == Material.AIR) {
        	chest = findChest( block, bridgeBlock, maxChestDist);
        }
        else{
        	chest = findChest( block, null, maxChestDist);
        }
        boolean checkMat = false;
        int widthLeft = getWidth(bridgeBlock, getLeft(block), maxWidth);
        maxWidth = maxWidth - widthLeft;
        int widthRight = getWidth(bridgeBlock, getRight(block), maxWidth);
        int dist = getDistToEnd(block, getBack(block));
        int error = 0;
        
        checkMat = allowedMaterials.contains(bridgeBlock.getTypeId());

        if(!checkMat){
        	return;
        }
        if(dist == -1) {
        	return;
        }
        if(chest == null) {
        	return;
        }      
        for( int j=1; j<=widthLeft; j++) {
        	tmpBlock = tmpBlock.getRelative(getLeft(block));
        	error = makeBridge(tmpBlock, dir, dist, chest);
        }
        if(error != -1) {
        	error = makeBridge(bridgeBlock, dir, dist, chest);
	        if(error != -1) {
		        tmpBlock = bridgeBlock;
		        for( int j=1; j<=widthRight; j++) {
		        	tmpBlock = tmpBlock.getRelative(getRight(block));
		        	error = makeBridge(tmpBlock, dir, dist, chest );
		        }
	        }
        }
		
	}
}
