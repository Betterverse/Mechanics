package net.betterverse.mechanics.mechanics;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Lift extends Mechanic {

    //The config variables (I(Zippo007) might change these to not being globals.)
	int maxDist = 255;
	
	//Creates the list, and its text
    public Lift(String text) {
        super(text);
    }

    //Returns whether or not a player can create a made mechanic based on their permission
    @Override
    public boolean canCreate(Player player) {
        return player.hasPermission("mechanics.lift");
    }

    //Returns whether or not a player can use a made mechanic based on their permission
    @Override
    public boolean canUse(Player player) {
        return true;
    }
    
    //Get the name of this mechanic (Lift)
    @Override
    public String getName() {
        return "Lift";
    }
    
    //This is where we check things:
  	//	If you can use the Lift you clicked: No on [Lift] yes on [Lift Up], or [Lift Down].
  	//	Checks is a sign exists that we can go to.
  	//	If that succeeds teleports the player up to that block.
    @Override
    public void activate(Player player, Block block) {
    	loadConfig();
        BlockFace direction = null;
        if (text.equals("[Lift]")) {
            player.sendMessage(ChatColor.RED + "You can't directly use this type of lift!");
            return;
        } else if (text.equals("[Lift Up]")) {
            direction = BlockFace.UP;
        } else if (text.equals("[Lift Down]")) {
            direction = BlockFace.DOWN;
        }
        int travel = getDistToEnd(player, block, direction);
        if(travel == -1){
        	player.sendMessage("No matching lift");
        	return;
        }
        Location playerLoc = player.getLocation();
        
        if(direction == BlockFace.UP){
        	playerLoc.setX(playerLoc.getX());
        	playerLoc.setZ(playerLoc.getZ());
        	playerLoc.setY(playerLoc.getY()+travel);
        	
        	Location locToCheck = player.getLocation();
            int Y1 = (int) (locToCheck.getY()-1+travel); 
            int X1 = (int) locToCheck.getX();
            int Z1 = (int) locToCheck.getZ();
 
            
            locToCheck.setY(Y1);
            locToCheck.setX(X1);
            locToCheck.setZ(Z1);
            
            //player.sendMessage("Loc: " + locToCheck)
            if( block.getWorld().getBlockAt(locToCheck).getType() == Material.AIR ){
            	player.sendMessage(ChatColor.RED + "Nothing to stand on!");
            	return;
            }
            
            
            locToCheck.setY((Y1+1));
            
            if( block.getWorld().getBlockAt(locToCheck).getType() != Material.AIR ){
            	player.sendMessage(ChatColor.RED + "You would be obstructed");
            	return;
            }
            
            locToCheck.setY((Y1+1));
            
            if( block.getWorld().getBlockAt(locToCheck).getType() != Material.AIR ){
            	player.sendMessage(ChatColor.RED + "You would be obstructed");
            	return;
            }
            
        	player.teleport(playerLoc);
        	player.sendMessage(ChatColor.GOLD + "Lift Up!");
        }
        else{
        	playerLoc.setX(playerLoc.getX());
        	playerLoc.setZ(playerLoc.getZ());
        	playerLoc.setY(playerLoc.getY()-travel);
        	player.teleport(playerLoc);
        	player.sendMessage(ChatColor.GOLD + "Lift Down!");
        }
    }
    
    //This grabs the distance between the signs, and converts it into the necessary number for the teleportation, doesn't require for the space inbetween to be clear
    private int getDistToEnd(Player player,  Block start, BlockFace dir ) {
    	boolean check = false;
    	int dist = 0;
    	
    	for( int i=0; i < maxDist; i++) {
    		
    		if(start.getRelative(dir).getType() == Material.WALL_SIGN){
    			if(text.equals("[Lift Up]") || text.equals("[Lift Down]") ||text.equals("[Lift]")){
    				dist++;
    				check = true;
    				break;
    			}
    		}
    		else if(start.getRelative(dir).getType() == Material.SIGN || start.getRelative(dir).getType() == Material.SIGN_POST) {
    			if(text.equals("[Lift Up]") || text.equals("[Lift Down]") ||text.equals("[Lift]")){
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
    	try{
    		config = plugin.getConfig();
    		configFile = new File( plugin.getDataFolder(), "config.yml");
	    	if(!config.contains("mechanic.lift.maxDist")) {
	    		config.set("mechanic.lift.maxDist", maxDist);
	    		maxDist = 255;
	    	}
	    	else{
	    		maxDist = config.getInt("mechanic.lift.maxDist");
	    	}
	    	plugin.saveConfig();
    	}catch(Exception e1){
    		e1.printStackTrace();
    	}
	}

	@Override
	public void activateRedstone(Block block) {
		return;
	}
}
