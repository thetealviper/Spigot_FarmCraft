package me.TheTealViper.farmcraft;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class Harvest {
	public String headTexture = "";
	List<ItemStack> drops = new ArrayList<ItemStack>();
	
	public Harvest(String headTexture, List<ItemStack> drops){
		this.headTexture = headTexture;
		this.drops = drops;
	}
}
