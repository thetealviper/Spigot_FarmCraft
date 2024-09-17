package me.TheTealViper.farmcraft;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Crop {
	public double growTime;
	public int requiredLight;
	public int requiredWaterRadius;
	public int requiredXP;
	public ItemStack seed;
	public Map<Integer, List<PotentialHarvest>> harvestData;
	public Material leafBlockMaterial;
	
	public Crop(double growTime, int requiredLight, int requiredWaterRadius, int requiredXP, ItemStack seed, Map<Integer, List<PotentialHarvest>> harvestData, Material leafBlockType) {
		this.growTime = 0.0D;
		this.requiredLight = -1; this.requiredWaterRadius = -1;
		this.seed = null;
		this.harvestData = new HashMap<>();
		this.leafBlockMaterial = Material.OAK_LEAVES;
		    
		this.growTime = growTime;
		this.requiredLight = requiredLight;
		this.requiredWaterRadius = requiredWaterRadius;
		this.requiredXP = requiredXP;
		this.seed = seed;
		this.harvestData = harvestData;
		this.leafBlockMaterial = leafBlockType;
	}
}
