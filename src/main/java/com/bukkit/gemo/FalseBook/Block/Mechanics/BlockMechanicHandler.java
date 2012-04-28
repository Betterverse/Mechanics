package com.bukkit.gemo.FalseBook.Block.Mechanics;

import com.bukkit.gemo.FalseBook.Block.FalseBookBlockCore;
import com.bukkit.gemo.FalseBook.Mechanics.MechanicHandler;
import com.bukkit.gemo.FalseBook.Mechanics.MechanicListener;
import java.util.HashMap;

public class BlockMechanicHandler extends MechanicHandler
{
  private HashMap<String, MechanicListener> registeredLines = new HashMap();

  public void registerBlockMechanics(FalseBookBlockCore plugin)
  {
    registerMechanic("LIFT", new MechanicLift(plugin));
    registerMechanic("AREA", new MechanicArea(plugin));
    registerMechanic("BRIDGE", new MechanicBridge(plugin));
    registerMechanic("DOOR", new MechanicDoor(plugin));
    registerMechanic("GATE", new MechanicGate(plugin));
    registerMechanic("BOOKSHELF", new MechanicBookshelf(plugin));
    registerMechanic("CAULDRON", new MechanicCauldron(plugin));
    registerMechanic("LIGHTSWITCH", new MechanicLightswitch(plugin));
    registerLines();
  }

  private void registerLines()
  {
    this.registeredLines.put("[lift]", getMechanic("LIFT"));
    this.registeredLines.put("[lift up]", getMechanic("LIFT"));
    this.registeredLines.put("[lift down]", getMechanic("LIFT"));

    this.registeredLines.put("[door up]", getMechanic("DOOR"));
    this.registeredLines.put("[door down]", getMechanic("DOOR"));

    this.registeredLines.put("[bridge]", getMechanic("BRIDGE"));
    this.registeredLines.put("[bridge end]", getMechanic("BRIDGE"));

    this.registeredLines.put("[gate]", getMechanic("GATE"));
    this.registeredLines.put("[dgate]", getMechanic("GATE"));

    this.registeredLines.put("[area]", getMechanic("AREA"));
    this.registeredLines.put("[toggle]", getMechanic("AREA"));

    this.registeredLines.put("[i]", getMechanic("LIGHTSWITCH"));
    this.registeredLines.put("[|]", getMechanic("LIGHTSWITCH"));
  }

  public MechanicListener getMechanicByLine(String line) {
    line = line.toLowerCase();
    return (MechanicListener)this.registeredLines.get(line);
  }
}