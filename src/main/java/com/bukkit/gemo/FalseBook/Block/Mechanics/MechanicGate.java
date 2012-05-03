package com.bukkit.gemo.FalseBook.Block.Mechanics;

import com.bukkit.gemo.FalseBook.Block.Config.ConfigHandler;
import com.bukkit.gemo.FalseBook.Block.FalseBookBlockCore;
import com.bukkit.gemo.FalseBook.Mechanics.MechanicListener;
import com.bukkit.gemo.FalseBook.Values.ValueIntegerList;
import com.bukkit.gemo.utils.ChatUtils;
import com.bukkit.gemo.utils.LWCProtection;
import com.bukkit.gemo.utils.SignUtils;
import com.bukkit.gemo.utils.UtilPermissions;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class MechanicGate extends MechanicListener {

	private HashMap<String, Block> GateAreas = new HashMap();

	public MechanicGate(FalseBookBlockCore plugin) {
		plugin.getMechanicHandler().registerEvent(BlockBreakEvent.class, this);
		plugin.getMechanicHandler().registerEvent(BlockPistonExtendEvent.class, this);
		plugin.getMechanicHandler().registerEvent(BlockPistonRetractEvent.class, this);
		plugin.getMechanicHandler().registerEvent(SignChangeEvent.class, this);
		plugin.getMechanicHandler().registerEvent(EntityChangeBlockEvent.class, this);
		plugin.getMechanicHandler().registerEvent(EntityExplodeEvent.class, this);
		plugin.getMechanicHandler().registerEvent(PlayerInteractEvent.class, this);
	}

	public boolean isActivatedByRedstone(Block block, BlockRedstoneEvent event) {
		return (ConfigHandler.isRedstoneAllowedForGates(block.getWorld().getName())) && (ConfigHandler.isGateEnabled(block.getWorld().getName()));
	}

	public void reloadMechanic() {
		saveGates();
		this.GateAreas = new HashMap();
		loadGates();
	}

	private boolean loadGates() {
		File file = new File("plugins" + System.getProperty("file.separator") + "FalseBook" + System.getProperty("file.separator") + "Gates.db");
		if (file.exists()) {
			try {
				FileInputStream fstream = new FileInputStream(file);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));

				String strLine = br.readLine();
				if (strLine == null) {
					FalseBookBlockCore.printInConsole("No Gates loaded");
					return false;
				}
				String[] split = strLine.split("=");
				int count = Integer.valueOf(split[1]).intValue();
				for (int i = 0; i < count; i++) {
					strLine = br.readLine();
					if (strLine == null) {
						continue;
					}
					String[] splitData = strLine.split(";");
					if (splitData.length == 2) {
						String[] sign1 = splitData[0].split(",");
						String worldName = splitData[1];
						Block block = null;
						try {
							block = Bukkit.getServer().getWorld(worldName).getBlockAt(Integer.valueOf(sign1[0]).intValue(), Integer.valueOf(sign1[1]).intValue(), Integer.valueOf(sign1[2]).intValue());
							if (isBlockTypeAllowed(block.getTypeId(), worldName)) {
								this.GateAreas.put(block.getLocation().toString(), block);
							}
						} catch (Exception ex) {
							System.out.println("Error loading gate at world: " + worldName+" with "+Bukkit.getWorld(worldName));
							System.out.println("Line 1: " + sign1[0]);
							System.out.println("Line 2: " + sign1[1]);
							System.out.println("Line 3: " + sign1[2]);
						}
					}
				}
				FalseBookBlockCore.printInConsole(this.GateAreas.values().size() + " protected gateblocks successfully loaded.");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				FalseBookBlockCore.printInConsole("Error while reading file: plugins/FalseBook/Gates.db");
				return false;
			}
		}
		return false;
	}

	private void saveGates() {
		File folder = new File("plugins" + System.getProperty("file.separator") + "FalseBook");
		folder.mkdirs();
		try {
			Writer output = new BufferedWriter(new FileWriter("plugins" + System.getProperty("file.separator") + "FalseBook" + System.getProperty("file.separator") + "Gates.db"));
			output.write("GateCount=" + this.GateAreas.values().size() + "\r\n");
			for (int i = 0; i < this.GateAreas.values().size(); i++) {
				String str = "";
				str = ((Block) this.GateAreas.values().toArray()[i]).getX() + "," + ((Block) this.GateAreas.values().toArray()[i]).getY() + "," + ((Block) this.GateAreas.values().toArray()[i]).getZ() + ";";
				str = str + ((Block) this.GateAreas.values().toArray()[i]).getWorld().getName() + ";";
				output.write(str + "\r\n");
			}
			output.close();
		} catch (IOException e) {
			FalseBookBlockCore.printInConsole("Error while saving file: plugins/FalseBook/Gates.db");
			e.printStackTrace();
		}
	}

	private boolean isBlockBreakable(List<Block> blockList) {
		for (int j = 0; j < blockList.size(); j++) {
			try {
				if (this.GateAreas.containsKey(((Block) blockList.get(j)).getLocation().toString())) {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return true;
	}

	private boolean isBlockProtected(Block block) {
		try {
			if (this.GateAreas.containsKey(block.getLocation().toString())) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void onLoad() {
		loadGates();
	}

	public void onUnload() {
		saveGates();
	}

	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!isBlockBreakable(event.blockList())) {
			event.setYield(0.0F);
			event.setCancelled(true);
		}
	}

	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (isBlockProtected(event.getBlock())) {
			event.setCancelled(true);
		}
	}

	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}

		if (!ConfigHandler.isGateEnabled(event.getBlock().getWorld().getName())) {
			return;
		}

		Block block = event.getBlock();
		if (isBlockProtected(event.getBlock())) {
			Player player = event.getPlayer();
			if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.destroy")) {
				player.sendMessage(ChatColor.RED + "You are not allowed to destroy gates.");
				event.setCancelled(true);
				return;
			}

		}

		if ((block.getType().equals(Material.SIGN_POST)) || (block.getType().equals(Material.WALL_SIGN))) {
			Sign sign = (Sign) block.getState();
			if ((!sign.getLine(1).equalsIgnoreCase("[Gate]")) && (!sign.getLine(1).equalsIgnoreCase("[DGate]"))) {
				return;
			}

			Player player = event.getPlayer();
			if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.gate")) {
				player.sendMessage(ChatColor.RED + "You are not allowed to destroy gates.");
				event.setCancelled(true);
				return;
			}

		}

		if (!SignUtils.isSignAnchor(block)) {
			return;
		}
		ArrayList<Sign> signList = SignUtils.getAdjacentWallSigns(block);
		if (signList.size() > 0) {
			boolean liftFound = false;
			for (Sign sign : signList) {
				if ((sign.getLine(1).equalsIgnoreCase("[Gate]")) || (sign.getLine(1).equalsIgnoreCase("[DGate]"))) {
					liftFound = true;
					break;
				}
			}
			signList.clear();

			if (!liftFound) {
				return;
			}

			Player player = event.getPlayer();
			if (!UtilPermissions.playerCanUseCommand(player, "falsebook.destroy.blocks")) {
				player.sendMessage(ChatColor.RED + "You are not allowed to destroy gates.");
				event.setCancelled(true);
				return;
			}
		}
	}

	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		if (event.isCancelled()) {
			return;
		}

		if (!ConfigHandler.isGateEnabled(event.getBlock().getWorld().getName())) {
			return;
		}
		if (!isBlockBreakable(event.getBlocks())) {
			event.setCancelled(true);
		}
	}

	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		if (event.isCancelled()) {
			return;
		}

		if (!event.isSticky()) {
			return;
		}

		if (!ConfigHandler.isGateEnabled(event.getBlock().getWorld().getName())) {
			return;
		}
		if (isBlockProtected(event.getRetractLocation().getBlock())) {
			event.setCancelled(true);
		}
	}

	public void onSignChange(SignChangeEvent event) {
		if (event.isCancelled()) {
			return;
		}

		if (!ConfigHandler.isGateEnabled(event.getBlock().getWorld().getName())) {
			return;
		}

		Player player = event.getPlayer();
		if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.gate")) {
			SignUtils.cancelSignCreation(event, "You are not allowed to build gates.");
			return;
		}

		if (event.getLine(1).equalsIgnoreCase("[Gate]")) {
			event.setLine(1, "[Gate]");
		} else {
			event.setLine(1, "[DGate]");
		}

		if (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.gate")) {
			SignUtils.cancelSignCreation(event, "You are not allowed to build gates.");
			return;
		}

		try {
			int TypeID = ConfigHandler.getAllowedGateBlocks(event.getBlock().getWorld().getName()).getValue(0);
			if (event.getLine(2).length() > 0) {
				TypeID = Integer.valueOf(event.getLine(2)).intValue();
			}
			if (!isBlockTypeAllowed(TypeID, event.getBlock().getWorld().getName())) {
				SignUtils.cancelSignCreation(event, "This blocktype is not allowed for building gates.");
				return;
			}
		} catch (Exception e) {
			SignUtils.cancelSignCreation(event, "Line 3 must be an integer or blank.");
			return;
		}
		ChatUtils.printSuccess(player, "[FB-Block]", "Gatesign created.");
	}

	public void onPlayerInteract(PlayerInteractEvent event, boolean isWallSign, boolean isSignPost) {
		if (event.isCancelled()) {
			return;
		}

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Block block = event.getClickedBlock();
		if (!ConfigHandler.isGateEnabled(block.getWorld().getName())) {
			return;
		}

		if ((!isWallSign) && (!isSignPost)) {
			return;
		}
		Sign sign = (Sign) block.getState();
		if ((!sign.getLine(1).equalsIgnoreCase("[Gate]")) && (!sign.getLine(1).equalsIgnoreCase("[DGate]"))) {
			return;
		}

		event.setUseInteractedBlock(Event.Result.DENY);
		event.setUseItemInHand(Event.Result.DENY);
		event.setCancelled(true);

		Player player = event.getPlayer();
		try {
			int TypeID = ConfigHandler.getAllowedGateBlocks(block.getWorld().getName()).getValue(0);
			if (sign.getLine(2).length() > 0) {
				TypeID = Integer.valueOf(sign.getLine(2)).intValue();
			}
			if (!isBlockTypeAllowed(TypeID, sign.getBlock().getWorld().getName())) {
				player.sendMessage(ChatColor.RED + "This blocktype is not allowed for building gates.");
				return;
			}

			if ((ConfigHandler.isRespectLWCProtections(player.getWorld().getName()))
							&& (!LWCProtection.canAccessWithCModify(player, block)) && (!UtilPermissions.playerCanUseCommand(player, "falsebook.blocks.ignoreLWCProtections"))) {
				player.sendMessage(ChatColor.RED + "This gate is protected!");
				return;
			}

			int result = toggle(SignUtils.getSignAnchor(sign).getBlock(), sign.getLine(1).equalsIgnoreCase("[DGate]"), false, false, TypeID);
			switch (result) {
				case -1:
					player.sendMessage(ChatColor.RED + "No Gate found.");
					break;
				case 0:
					player.sendMessage(ChatColor.RED + "This blocktype is not allowed for building gates.");
					break;
				case 1:
					player.sendMessage(ChatColor.GOLD + "Gate moved.");
			}
		} catch (Exception e) {
			return;
		}
	}

	private boolean isBlockTypeAllowed(int ID, String worldName) {
		return ConfigHandler.getAllowedGateBlocks(worldName).hasValue(ID);
	}

	public int toggle(Block origin, boolean DGate, boolean override, boolean newStatus, int thisID) {
		if (!isBlockTypeAllowed(thisID, origin.getWorld().getName())) {
			return 0;
		}
		HashMap visited = new HashMap();
		int ySearch = 16;
		int yMiniSearch = 5;
		int xzSearch = 3;

		if (DGate) {
			xzSearch = 1;
			ySearch = 2;
		}

		boolean gateFound = false;
		int yOrigin = origin.getY();
		for (int x = 0; x <= xzSearch; x++) {
			for (int z = 0; z <= xzSearch; z++) {
				if (visited.containsKey(x + "_" + z)) {
					continue;
				}
				if (getTopFence(origin, x, yOrigin, z, yMiniSearch, thisID) != null) {
					searchColumn(origin, x, yOrigin, z, override, newStatus, visited, null, thisID);
					gateFound = true;
					return 1;
				}

				if (visited.containsKey(-x + "_" + -z)) {
					continue;
				}
				if (getTopFence(origin, -x, yOrigin, -z, yMiniSearch, thisID) != null) {
					searchColumn(origin, -x, yOrigin, -z, override, newStatus, visited, null, thisID);
					gateFound = true;
					return 1;
				}

				if (visited.containsKey(x + "_" + -z)) {
					continue;
				}
				if (getTopFence(origin, x, yOrigin, -z, yMiniSearch, thisID) != null) {
					searchColumn(origin, x, yOrigin, -z, override, newStatus, visited, null, thisID);
					gateFound = true;
					return 1;
				}

				if (visited.containsKey(-x + "_" + z)) {
					continue;
				}
				if (getTopFence(origin, -x, yOrigin, z, yMiniSearch, thisID) != null) {
					searchColumn(origin, -x, yOrigin, z, override, newStatus, visited, null, thisID);
					gateFound = true;
					return 1;
				}
			}

		}

		gateFound = false;
		yOrigin = origin.getY();
		for (int x = 0; x <= xzSearch; x++) {
			for (int z = 0; z <= xzSearch; z++) {
				if (visited.containsKey(x + "_" + z)) {
					continue;
				}
				if (getTopFence(origin, x, yOrigin, z, ySearch, thisID) != null) {
					searchColumn(origin, x, yOrigin, z, override, newStatus, visited, null, thisID);
					gateFound = true;
					return 1;
				}

				if (visited.containsKey(-x + "_" + -z)) {
					continue;
				}
				if (getTopFence(origin, -x, yOrigin, -z, ySearch, thisID) != null) {
					searchColumn(origin, -x, yOrigin, -z, override, newStatus, visited, null, thisID);
					gateFound = true;
					return 1;
				}

				if (visited.containsKey(x + "_" + -z)) {
					continue;
				}
				if (getTopFence(origin, x, yOrigin, -z, ySearch, thisID) != null) {
					searchColumn(origin, x, yOrigin, -z, override, newStatus, visited, null, thisID);
					gateFound = true;
					return 1;
				}

				if (visited.containsKey(-x + "_" + z)) {
					continue;
				}
				if (getTopFence(origin, -x, yOrigin, z, ySearch, thisID) != null) {
					searchColumn(origin, -x, yOrigin, z, override, newStatus, visited, null, thisID);
					gateFound = true;
					return 1;
				}
			}
		}

		visited.clear();
		visited = null;

		if (gateFound) {
			return 1;
		}
		return -1;
	}

	private void searchColumn(Block origin, int xOffset, int y, int zOffset, boolean override, boolean newStatus, HashMap<String, Boolean> visited, Block lastTop, int GateTypeID) {
		Block topFence = getTopFence(origin, xOffset, y, zOffset, 16, GateTypeID);
		if (topFence == null) {
			return;
		}
		if ((lastTop != null)
						&& (Math.abs(lastTop.getY() - topFence.getY()) > 5)) {
			return;
		}

		if (visited.containsKey(topFence.getX() + "_" + topFence.getZ())) {
			return;
		}
		visited.put(topFence.getX() + "_" + topFence.getZ(), Boolean.valueOf(false));

		if ((Math.abs(xOffset) > ConfigHandler.getMaxGateWidth(origin.getWorld().getName())) || (Math.abs(zOffset) > ConfigHandler.getMaxGateWidth(origin.getWorld().getName()))) {
			return;
		}

		int TypeID = -1;
		Block block = null;
		boolean off = newStatus;
		if (!override) {
			off = topFence.getRelative(0, -1, 0).getTypeId() == GateTypeID;
		}

		for (int i = 1; i <= ConfigHandler.getMaxGateHeight(origin.getWorld().getName()); i++) {
			block = topFence.getRelative(0, -i, 0);
			TypeID = block.getTypeId();
			if ((TypeID != Material.AIR.getId()) && (TypeID != GateTypeID) && (TypeID != Material.WATER.getId()) && (TypeID != Material.STATIONARY_WATER.getId()) && (TypeID != Material.LAVA.getId()) && (TypeID != Material.STATIONARY_LAVA.getId())) {
				break;
			}
			if (off) {
				block.setTypeIdAndData(Material.AIR.getId(), (byte) 0, true);
				this.GateAreas.remove(block.getLocation().toString());
				this.GateAreas.remove(topFence.getLocation().toString());
			} else {
				block.setTypeIdAndData(GateTypeID, (byte) 0, true);
				this.GateAreas.put(block.getLocation().toString(), block);
				this.GateAreas.put(topFence.getLocation().toString(), topFence);
			}

		}

		for (int tX = -1; tX <= 1; tX++) {
			for (int tZ = -1; tZ <= 1; tZ++) {
				searchColumn(origin, xOffset + tX, y, zOffset + tZ, override, newStatus, visited, topFence, GateTypeID);
			}
		}
	}

	private Block getTopFence(Block origin, int xOffset, int y, int zOffset, int searchWidth, int TypeID) {
		for (int i = 0; i <= searchWidth; i++) {
			if (y + i > origin.getWorld().getMaxHeight() - 1) {
				continue;
			}
			if ((origin.getRelative(xOffset, i, zOffset).getTypeId() == TypeID) && (origin.getRelative(xOffset, i + 1, zOffset).getTypeId() != TypeID) && ((origin.getRelative(xOffset, i - 1, zOffset).getTypeId() == Material.AIR.getId()) || (origin.getRelative(xOffset, i - 1, zOffset).getTypeId() == TypeID) || (origin.getRelative(xOffset, i - 1, zOffset).getTypeId() == Material.WATER.getId()) || (origin.getRelative(xOffset, i - 1, zOffset).getTypeId() == Material.STATIONARY_WATER.getId()) || (origin.getRelative(xOffset, i - 1, zOffset).getTypeId() == Material.LAVA.getId()) || (origin.getRelative(xOffset, i - 1, zOffset).getTypeId() == Material.STATIONARY_LAVA.getId()))) {
				return origin.getRelative(xOffset, i, zOffset);
			}
		}
		for (int i = -1; i >= -searchWidth; i--) {
			if (y + i < 2) {
				continue;
			}
			if ((origin.getRelative(xOffset, i, zOffset).getTypeId() == TypeID) && (origin.getRelative(xOffset, i + 1, zOffset).getTypeId() != TypeID) && ((origin.getRelative(xOffset, i - 1, zOffset).getTypeId() == Material.AIR.getId()) || (origin.getRelative(xOffset, i - 1, zOffset).getTypeId() == TypeID) || (origin.getRelative(xOffset, i - 1, zOffset).getTypeId() == Material.WATER.getId()) || (origin.getRelative(xOffset, i - 1, zOffset).getTypeId() == Material.STATIONARY_WATER.getId()) || (origin.getRelative(xOffset, i - 1, zOffset).getTypeId() == Material.LAVA.getId()) || (origin.getRelative(xOffset, i - 1, zOffset).getTypeId() == Material.STATIONARY_LAVA.getId()))) {
				return origin.getRelative(xOffset, i, zOffset);
			}
		}
		return null;
	}
}