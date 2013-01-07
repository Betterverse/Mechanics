package net.betterverse.mechanics.mechanics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.material.Sign;

public class Gate extends Mechanic implements Listener {

    //The config variables (I(Zippo007) might change these to not being globals.)
    int maxWidth = 50;
    int maxHeight = 35;
    int signDist = 2;
    List<Integer> allowedMaterials = new ArrayList<Integer>();
    
    //This one is for checking if blocks in gate zone on breakable when on.
    HashSet<Location> blocks = new HashSet<Location>();
    
    //Creates the Gate, and its text
    public Gate() {
        super("[Gate]");
    }

    //Returns whether or not a player can create a made mechanic based on their permission
    @Override
    public boolean canCreate(Player player) {
        return player.hasPermission("mechanics.gate");
    }

    //Returns whether or not a player can use a made mechanic based on their permission
    @Override
    public boolean canUse(Player player) {
        return true;
    }
    
    @Override
    //Get the name of this mechanic (Gate)    
    public String getName() {
        return "Gate";
    }
    

    //This is where we check things:
  	//	If you can use the Gate you clicked: yes on [Gate].
  	//	If the block you want to use is the correct type.
  	//  Gets where to place the blocks width, and height.
  	//	Makes the gate fully.
    @Override
    public void activate(Player player, Block block) {
    	loadConfig();
    	BlockFace toCheck = ((Sign) block.getState().getData()).getAttachedFace();
    	BlockFace dir = BlockFace.DOWN;
    	Block gateBlock = findGate(block, signDist);
    	if(gateBlock == null){
    		player.sendMessage(ChatColor.RED + "No Gate found!");
    		return;
    	}
    	Block tmpBlock = gateBlock;
    	
    	int widthLeft = getWidth(gateBlock, getLeft(block), maxWidth);
        maxWidth = maxWidth - widthLeft;
        int widthRight = getWidth(gateBlock, getRight(block), maxWidth);
        for(int i=1; i<=widthLeft; i++){
        	tmpBlock = tmpBlock.getRelative(getLeft(block));
        	makeGate(tmpBlock, dir);
        }
        makeGate(gateBlock, dir);
        tmpBlock = gateBlock;
        for(int i=1; i<=widthRight; i++){
        	tmpBlock = tmpBlock.getRelative(getRight(block));
        	makeGate(tmpBlock, dir);
        }
        
        player.sendMessage(ChatColor.GREEN + "Gate toggled!");
    }

    
    //This makes the gate until it hits something other than air/the material used in it if closing, or until it hits max height.
    //Sets the block to uniteractable/interactable based on toggle on/off.
    public void makeGate(Block gateBlock, BlockFace dir){
    	blocks.add(gateBlock.getLocation());
    	Material toSet = gateBlock.getType();
    	gateBlock = gateBlock.getRelative(dir);
    	blocks.add(gateBlock.getLocation());
    	boolean create = gateBlock.getRelative(dir).getType() == Material.AIR;
    	for( int r=1; r<maxHeight; r++ ) {
    		if(create) {
    			if(gateBlock.getType() == Material.AIR){
    				gateBlock.setType(toSet);
    				gateBlock = gateBlock.getRelative(dir);
    				blocks.add(gateBlock.getLocation());
    			}
    		}
         	else {
         		if(gateBlock.getType() == toSet ){
         			gateBlock.setType(Material.AIR);
         			gateBlock = gateBlock.getRelative(dir);
         			blocks.remove(gateBlock.getLocation());
         		}
         	}
        }
    }
    
    //Goes and finds the closet gate allowed material and sends it back to be made into a bridge
    public Block findGate(Block block, int howfar) {
		if(block == null){
			return null;
		}
		if(howfar < 0){
    		return null;
    	}
    	if( allowedMaterials.contains(block.getTypeId()) ) {
    		return block;
    	}
    	Block gateBlock;
    	//Check down
    	gateBlock = findGate( block.getRelative(BlockFace.DOWN), howfar-1 );
    	if(gateBlock != null){
    		return gateBlock;
    	}
    	//Check up
    	gateBlock = findGate( block.getRelative(BlockFace.UP), howfar-1 );
    	if(gateBlock != null){
    		return gateBlock;
    	}
    	//Check East
    	gateBlock = findGate( block.getRelative(BlockFace.EAST), howfar-1 );
    	if(gateBlock != null){
    		return gateBlock;
    	}
    	//Check West
    	gateBlock = findGate( block.getRelative(BlockFace.WEST), howfar-1 );
    	if(gateBlock != null){
    		return gateBlock;
    	}
    	//Check North
    	gateBlock = findGate( block.getRelative(BlockFace.NORTH), howfar-1 );
    	if(gateBlock != null){
    		return gateBlock;
    	}
    	//Check South
    	gateBlock = findGate( block.getRelative(BlockFace.SOUTH), howfar-1 );
    	if(gateBlock != null){
    		return gateBlock;
    	}
    	return null;
    }

    //Checks do make sure the blocks in it cannot be messed with.
    public boolean canBreak(Block blockToCheck){
    	if(blocks.contains(blockToCheck.getLocation())){
    		return false;
    	}
    	else{
    		return true;
    	}
    }
    public boolean canPistonRetract(Location locToCheck){
    	if(blocks.contains(locToCheck)){
    		return false;
    	}
    	else{
    		return true;
    	}
    }

    //Loads all of our configs (Will be changed to do it all its self, when I(Zippo007) get to it....soonish)
    @Override
    public void loadConfig(){
    	allowedMaterials.add(101);
    	allowedMaterials.add(102);
    	allowedMaterials.add(85);
    	FileConfiguration config;
    	File configFile;
    	try{
    		config = plugin.getConfig();
    		configFile = new File( plugin.getDataFolder(), "config.yml");
	    	if(!config.contains("mechanic.gate.maxWidth")) {
	    		config.set("mechanic.gate.maxWidth", maxWidth);
	    		maxWidth = 50;
	    	}
	    	else{
	    		maxWidth = config.getInt("mechanic.gate.maxWidth");
	    	}
	    	if(!config.contains("mechanic.gate.maxHeight")) {
	    		config.set("mechanic.gate.maxHeight", maxHeight);
	    		maxHeight = 35;
	    	}
	    	else{
	    		maxHeight = config.getInt("mechanic.gate.maxHeight");
	    	}
	    	if(!config.contains("mechanic.gate.allowedMaterials")) {
    			config.set("mechanic.gate.allowedMaterials", allowedMaterials);
    		}
    		else{
    			allowedMaterials = (List<Integer>) config.getList("mechanic.gate.allowedMaterials");
    		}
	    	if(!config.contains("mechanic.gate.signDist")) {
	    		config.set("mechanic.gate.signDist", 2);
	    	}
	    	else{
	    		signDist = config.getInt("mechanic.gate.signDist");
	    	}
	    	plugin.saveConfig();
    	}catch(Exception e1){
    		e1.printStackTrace();
    	}
    }

	@Override
	public void activateRedstone(Block block) {
		loadConfig();
    	BlockFace toCheck = ((Sign) block.getState().getData()).getAttachedFace();
    	BlockFace dir = BlockFace.DOWN;
    	Block gateBlock = block.getRelative(toCheck);
    	Block tmpBlock = gateBlock;
    	
    	int widthLeft = getWidth(gateBlock, getLeft(block), maxWidth);
        maxWidth = maxWidth - widthLeft;
        int widthRight = getWidth(gateBlock, getRight(block), maxWidth);
        for(int i=1; i<=widthLeft; i++){
        	tmpBlock = tmpBlock.getRelative(getLeft(block));
        	makeGate(tmpBlock, dir);
        }
        makeGate(gateBlock, dir);
        tmpBlock = gateBlock;
        for(int i=1; i<=widthRight; i++){
        	tmpBlock = tmpBlock.getRelative(getRight(block));
        	makeGate(tmpBlock, dir);
        }
	}
}
