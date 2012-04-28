package com.bukkit.gemo.FalseBook.Block.Areas;

import java.io.Serializable;

public class AreaSign extends AreaComplexBlock
  implements Serializable
{
  private static final long serialVersionUID = -2940465813606518671L;
  private String[] Lines;

  public AreaSign(String[] Lines)
  {
    setLines(Lines);
  }

  public String[] getLines() {
    return this.Lines;
  }
  public void setLines(String[] lines) {
    this.Lines = lines;
  }
}