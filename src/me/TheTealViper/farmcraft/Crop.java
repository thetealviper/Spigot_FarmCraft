package me.TheTealViper.farmcraft;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

public class Crop {
  public Crop(double growTime, int requiredLight, int requiredWaterRadius, ItemStack seed, Map<Integer, List<PotentialHarvest>> harvestData) {
    this.growTime = 0.0D;
    this.requiredLight = -1; this.requiredWaterRadius = -1;
    this.seed = null;
    this.harvestData = new HashMap();

    
    this.growTime = growTime;
    this.requiredLight = requiredLight;
    this.requiredWaterRadius = requiredWaterRadius;
    this.seed = seed;
    this.harvestData = harvestData;
  }
  
  public double growTime;
  public int requiredLight;
  public int requiredWaterRadius;
  public ItemStack seed;
  public Map<Integer, List<PotentialHarvest>> harvestData;
}
