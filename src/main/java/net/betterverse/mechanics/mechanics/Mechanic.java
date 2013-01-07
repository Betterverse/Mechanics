package net.betterverse.mechanics.mechanics;

import net.betterverse.mechanics.Mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

public abstract class Mechanic {
    protected static final BlockFace[] pos = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };

    protected final Mechanics plugin;
    protected final String text;

    //Gets the plugin, and name of the mechanic
    public Mechanic(String text) {
        plugin = (Mechanics) Bukkit.getServer().getPluginManager().getPlugin("Mechanics");
        this.text = text;
    }

    //Returns whether or not a player can create a made mechanic
    public abstract boolean canCreate(Player player);

    //Returns whether or not a player can use a made mechanic
    public abstract boolean canUse(Player player);

    //What gets called when the player right clicks on the block
    public abstract void activate(Player player, Block block);
    
    //What gets called when the redstone triggers the event
    public abstract void activateRedstone(Block block);
    
    //Grabs info from the config.yml file for the mechanic
    public abstract void loadConfig();

    //Gets the name of the mechanic
    public abstract String getName();
    
    
    //Some handler methods used in multiple mechanics
    public Chest findChest(Block block, Block check, int howfar) {
        if( block.getType() == Material.CHEST){
    		Chest test = (Chest) block.getState();
    		if(check != null){
    			if(test.getBlockInventory().contains(check.getTypeId())) {
    				return test;
    			}
    		}
    		else{
    			return test;
    		}
    	}
    	if(howfar < 0){
    		return null;
    	}
    	Chest chest;
    	//Check down
    	chest = findChest( block.getRelative(BlockFace.DOWN), check, howfar-1 );
    	if(chest != null){
    		return chest;
    	}
    	//Check up
    	chest = findChest( block.getRelative(BlockFace.UP), check, howfar-1 );
    	if(chest != null){
    		return chest;
    	}
    	//Check East
    	chest = findChest( block.getRelative(BlockFace.EAST), check, howfar-1 );
    	if(chest != null){
    		return chest;
    	}
    	//Check West
    	chest = findChest( block.getRelative(BlockFace.WEST), check, howfar-1 );
    	if(chest != null){
    		return chest;
    	}
    	//Check North
    	chest = findChest( block.getRelative(BlockFace.NORTH), check, howfar-1 );
    	if(chest != null){
    		return chest;
    	}
    	//Check South
    	chest = findChest( block.getRelative(BlockFace.SOUTH), check, howfar-1 );
    	if(chest != null){
    		return chest;
    	}
    	return null;
    }
    
    public int getWidth(Block start, BlockFace dir, int maxWidth) {
    	int width = 0;
    	Block original = start;
    	for( int i=0; i<maxWidth; i++ ) {
    		if(start.getRelative(dir).getType() != original.getType() ) {
    			break;
    		}
    		start = start.getRelative(dir);
    		width++;
    	}
    	return width;
    }
    
    //These deal with sign direction
    public static BlockFace getLeft(Block sign) {

        if (sign.getType() == Material.SIGN_POST) {
            switch (sign.getData()) {
                case 0x0:
                    return BlockFace.WEST;
                case 0x1:
                case 0x2:
                case 0x3:
                case 0x4:
                    return BlockFace.NORTH;
                case 0x5:
                case 0x6:
                case 0x7:
                case 0x8:
                    return BlockFace.EAST;
                case 0x9:
                case 0xA:
                case 0xB:
                case 0xC:
                    return BlockFace.SOUTH;
                case 0xD:
                case 0xE:
                case 0xF:
                default:
                    return BlockFace.SELF;
            }
        } else {
            switch (sign.getData()) {
                case 0x3:
                    return BlockFace.WEST;
                case 0x2:
                    return BlockFace.EAST;
                case 0x4:
                    return BlockFace.NORTH;
                case 0x5:
                    return BlockFace.SOUTH;
                default:
                    return BlockFace.SELF;
            }
        }
    }
    
    public static BlockFace getRight(Block sign) {

        if (sign.getType() == Material.SIGN_POST) {
            switch (sign.getData()) {
                case 0x0:
                    return BlockFace.EAST;
                case 0x1:
                case 0x2:
                case 0x3:
                case 0x4:
                    return BlockFace.SOUTH;
                case 0x5:
                case 0x6:
                case 0x7:
                case 0x8:
                    return BlockFace.WEST;
                case 0x9:
                case 0xA:
                case 0xB:
                case 0xC:
                    return BlockFace.NORTH;
                case 0xD:
                case 0xE:
                case 0xF:
                default:
                    return BlockFace.SELF;
            }
        } else {
            switch (sign.getData()) {
                case 0x3:
                    return BlockFace.EAST;
                case 0x2:
                    return BlockFace.WEST;
                case 0x4:
                    return BlockFace.SOUTH;
                case 0x5:
                    return BlockFace.NORTH;
                default:
                    return BlockFace.SELF;
            }
        }
    }
    
    public static BlockFace getBack(Block sign) {
		
    	if (sign.getType() == Material.SIGN_POST) {
            switch (sign.getData()) {
                case 0x0:
                    return BlockFace.NORTH;
                case 0x1:
                case 0x2:
                case 0x3:
                case 0x4:
                    return BlockFace.EAST;
                case 0x5:
                case 0x6:
                case 0x7:
                case 0x8:
                    return BlockFace.SOUTH;
                case 0x9:
                case 0xA:
                case 0xB:
                case 0xC:
                    return BlockFace.WEST;
                case 0xD:
                case 0xE:
                case 0xF:
                default:
                    return BlockFace.SELF;
            }
        } else {
            switch (sign.getData()) {
                case 0x3:
                    return BlockFace.NORTH;
                case 0x2:
                    return BlockFace.SOUTH;
                case 0x4:
                    return BlockFace.EAST;
                case 0x5:
                    return BlockFace.WEST;
                default:
                    return BlockFace.SELF;
            }
        }
	}
}
