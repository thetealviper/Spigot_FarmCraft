package me.TheTealViper.farmcraft;

public class PotentialHarvest {
	public int chance = 0, mChance = 0;
	public Harvest harvest = null;
	
	public PotentialHarvest(int chance, Harvest harvest){
		this.chance = chance;
		this.harvest = harvest;
	}
}
