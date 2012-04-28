package com.bukkit.gemo.FalseBook.Block.World;

public enum EnumSettings
{
  BRIDGE_ENABLED("BridgeEnabled"), 

  BRIDGE_MAX_SIDEWIDTH("MaxBridgeSideWidth"), 

  BRIDGE_MAX_LENGTH("MaxBridgeLength"), 

  BRIDGE_ALLOW_REDSTONE("allowRedstoneForBridges"), 

  BRIDGE_ALLOWED_BLOCKS("allowedBridgeBlocks"), 

  DOOR_ENABLED("DoorEnabled"), 

  DOOR_MAX_SIDEWIDTH("MaxDoorSideWidth"), 

  DOOR_MAX_HEIGHT("MaxDoorHeight"), 

  DOOR_ALLOW_REDSTONE("allowRedstoneForDoors"), 

  DOOR_ALLOWED_BLOCKS("allowedDoorBlocks"), 

  GATE_ENABLED("GateEnabled"), 

  GATE_MAX_WIDTH("maxGateWidth"), 

  GATE_MAX_HEIGHT("maxGateHeight"), 

  GATE_ALLOW_REDSTONE("allowRedstoneForGates"), 

  GATE_ALLOWED_BLOCKS("allowedGateBlocks"), 

  AREA_TOOL("AreaSelectionTool"), 

  AREA_ALLOW_REDSTONE("allowRedstoneForAreas"), 

  CAULDRON_NATIVE("AllowMinecraftCauldron"), 

  CAULDRON_COOLDOWN("CauldronCoolDownInSeconds"), 

  LIGHTSWITCH_ENABLED("LightswitchEnabled"), 

  LIGHTSWITCH_MAX_TOGGLE("MaxLightswitchToggle"), 

  LWC_RESPECT("respectLWCProtections"), 

  BOOKSHELFS_ENABLED("ReadableBookshelfs"), 

  APPLE_DROP_CHANCE("AppleDropChance");

  private final String thisName;

  private EnumSettings(String name) { this.thisName = name; }

  public String getName()
  {
    return this.thisName;
  }
}