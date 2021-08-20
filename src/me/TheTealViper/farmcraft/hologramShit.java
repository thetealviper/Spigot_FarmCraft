package me.TheTealViper.farmcraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public class hologramShit {
	
	public static Map<UUID, Hologram> existingHolograms = new HashMap<UUID, Hologram>();
	
	public static void handle(FarmCraft plugin, PlayerInteractEvent e, String locString){
		if(existingHolograms.containsKey(e.getPlayer().getUniqueId()))
			existingHolograms.get(e.getPlayer().getUniqueId()).delete();
		Hologram h = HologramsAPI.createHologram(plugin, e.getClickedBlock().getLocation().add(.5, 1, .5));
		existingHolograms.put(e.getPlayer().getUniqueId(), h);
		for(String s : plugin.getConfig().getStringList("Crop_Info_Hologram")){
			s = FarmCraft.makeColors(s);
			if(!s.contains("%farmcraft_drops%")){
				h.appendTextLine(plugin.formatString(s, locString));
				h.teleport(h.getLocation().add(0, .25, 0));
			}else{
				String crop = plugin.seedInfo.getString(locString + ".crop");
				Crop c = plugin.cropMap.get(crop);
				long alive = System.currentTimeMillis() - plugin.seedInfo.getLong(locString + ".planted");
				int percent = (int) ((alive / 1000D) / (double) c.growTime * 100D);
				int closestHarvestPercent = 0;
				for(int p : c.harvestData.keySet()){
					if(p > closestHarvestPercent && p <= percent)
						closestHarvestPercent = p;
				}
				for(PotentialHarvest ph : c.harvestData.get(closestHarvestPercent)) {
					for(ItemStack i : ph.harvest.drops) {
						h.appendItemLine(i);
						h.teleport(h.getLocation().add(0, .5, 0));
					}
				}
//				for(ItemStack i : plugin.getHarvest(c, closestHarvestPercent).drops){ // OLD VERSION
//					h.appendItemLine(i);
//					h.teleport(h.getLocation().add(0, .5, 0));
//				}
			}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {public void run() {
			h.delete();
		}}, plugin.getConfig().getInt("Hologram_Duration") * 20);
	}
}
