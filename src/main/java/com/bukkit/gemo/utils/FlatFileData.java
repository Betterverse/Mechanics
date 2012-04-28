package com.bukkit.gemo.utils;

public class FlatFileData
{
  private String name;
  private String value;

  public FlatFileData(String name, Object value)
  {
    this.name = name;
    this.value = value.toString();
  }

  public String getName()
  {
    return this.name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getValue()
  {
    return this.value;
  }

  public void setValue(Object value)
  {
    this.value = value.toString();
  }
}