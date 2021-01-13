package me.TheTealViper.farmcraft;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.TheTealViper.farmcraft.Utils.PluginFile;
import me.TheTealViper.farmcraft.Utils.ReflectionUtils;
import me.TheTealViper.farmcraft.Utils.UtilityEquippedJavaPlugin;
 
public class FarmCraft extends UtilityEquippedJavaPlugin implements Listener{
	public Map<String, Crop> cropMap = new HashMap<String, Crop>();
	public PluginFile growingSeeds = new PluginFile(this, "growing.data");
	public PluginFile grownSeeds = new PluginFile(this, "grown.data");
	public PluginFile seedInfo = new PluginFile(this, "info.data");
	public Map<Player, Long> getInfoMap = new HashMap<Player, Long>();
	public static boolean debug = false;
	public Map<Player, Long> purgeInfo = new HashMap<Player, Long>();
 
    public void onEnable(){
    	StartupPlugin(this, "50031");
    	
    	loadShit();
    }
   
    public void onDisable(){
        //getLogger().info("FarmCraft from TheTealViper shutting down. Bshzzzzzz");
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender instanceof Player){
            Player p = (Player) sender;
            boolean explain = false;
            if(args.length == 0) {
            	explain = true;
            }
            else if(args.length == 1){
            	if(args[0].equalsIgnoreCase("list") && p.hasPermission("farmcraft.admin")){
            		for(String s : cropMap.keySet())
            			p.sendMessage("- " + s);
            	}else if(args[0].equalsIgnoreCase("seedfix")){
            		for(Crop c : cropMap.values()){
            			ItemStack seed = c.seed.clone();
            			int amount = 0;
            			for(int i = 0;i < 36;i++){
            				if(p.getInventory().getItem(i) != null && p.getInventory().getItem(i).isSimilar(seed)){
            					amount += p.getInventory().getItem(i).getAmount();
            					p.getInventory().getItem(i).setAmount(0);
            				}
            			}
            			seed.setAmount(amount);
            			p.getInventory().setItem(p.getInventory().firstEmpty(), seed);
            		}
            	}else if(args[0].equalsIgnoreCase("reload") && p.hasPermission("farmcraft.admin")){
            		p.sendMessage("Reloading...");
            		reloadShit();
            	}else
            		explain = true;
            }else if(args.length == 2){
            	if(args[0].equalsIgnoreCase("give") && p.hasPermission("farmcraft.admin")){
            		if(cropMap.containsKey(args[1])){
            			p.getInventory().addItem(cropMap.get(args[1]).seed.clone());
            		}else
            			p.sendMessage("That crop doesn't exist.");
            	}else if(args[0].equalsIgnoreCase("purge") && p.hasPermission("farmcraft.admin")){
            		if(cropMap.containsKey(args[1])){
            			if(!purgeInfo.containsKey(p))
            				purgeInfo.put(p, 0L);
            			if(System.currentTimeMillis() - purgeInfo.get(p) > 5000){
            				p.sendMessage("For safety reasons, please type the command again within 5 seconds.");
            				purgeInfo.put(p, System.currentTimeMillis());
            			}else{
            				p.sendMessage("Starting purge. This cannot be undone. This may cause lag.");
            				int purgedAmount = 0;
            				ConfigurationSection sec = growingSeeds.getConfigurationSection(args[1]);
            				for(String s : sec.getKeys(false)){
            					Location loc = getStringUtils().fromLocString(s, false);
            					loc.getBlock().setType(Material.AIR);
            					loc.add(0, 1, 0).getBlock().setType(Material.AIR);
            					purgedAmount++;
            					growingSeeds.set(args[1] + "." + s, null);
            					growingSeeds.save();
            				}
            				for(String s : grownSeeds.getKeys(false)){
            					if(grownSeeds.getString(s).equals(args[1])){
            						Location loc = getStringUtils().fromLocString(s, false);
                					loc.getBlock().setType(Material.AIR);
                					loc.add(0, 1, 0).getBlock().setType(Material.AIR);
                					purgedAmount++;
                					grownSeeds.set(s, null);
                					grownSeeds.save();
            					}
            				}
            				p.sendMessage("Purge over. " + purgedAmount + " crops purged.");
            			}
            		}else{
            			p.sendMessage("That crop doesn't exist.");
            		}
            	}else
            		explain = true;
            }else if(args.length == 3){
            	if(args[0].equalsIgnoreCase("give") && p.hasPermission("farmcraft.admin")){
            		if(cropMap.containsKey(args[1])){
            			if(Bukkit.getOfflinePlayer(args[2]).isOnline())
            				Bukkit.getPlayer(args[2]).getInventory().addItem(cropMap.get(args[1]).seed.clone());
            			else
            				p.sendMessage("That player isn't online.");
            		}else
            			p.sendMessage("That crop doesn't exist.");
            	}else
            		explain = true;
            }else if(args.length == 4){
            	if(args[0].equalsIgnoreCase("give") && p.hasPermission("farmcraft.admin")){
            		if(cropMap.containsKey(args[1])){
            			if(Bukkit.getOfflinePlayer(args[2]).isOnline()){
            				try{
	            				ItemStack seed = cropMap.get(args[1]).seed.clone();
	            				seed.setAmount(Integer.valueOf(args[3]));
	            				Bukkit.getPlayer(args[2]).getInventory().addItem(seed);
            				}catch(Exception e){
            					p.sendMessage("That's not a number.");
            				}
            			}
            			else
            				p.sendMessage("That player isn't online.");
            		}else
            			p.sendMessage("That crop doesn't exist.");
            	}else
            		explain = true;
            }
            if(explain){
            	p.sendMessage("FarmCraft Commands\n"
            			+ "/farmcraft seedfix" + ChatColor.GRAY + " - Combines seeds into a single stack.");
            	if(p.hasPermission("farmcraft.admin")){
            		p.sendMessage("FarmCraft Admin Commands");
            		p.sendMessage("/farmcraft list" + ChatColor.GRAY + " - Lists all available crops.");
            		p.sendMessage("/farmcraft reload" + ChatColor.GRAY + " - I refuse to explain this.");
            		p.sendMessage("/farmcraft give <crop> (player) (amount)" + ChatColor.GRAY + " - Gives crop seeds.");
            		p.sendMessage("/farmcraft purge <crop>" + ChatColor.GRAY + " - Removes all instances of a crop.");
            	}
            }
        }else{
        	//Not a player
        	boolean explain = false;
        	if(args.length == 0)
            	explain = true;
            else if(args.length == 1){
            	if(args[0].equalsIgnoreCase("list")){
            		for(String s : cropMap.keySet())
            			sender.sendMessage("- " + s);
            	}else
            		explain = true;
            }else if(args.length == 2){
            	if(args[0].equalsIgnoreCase("give")){
            		if(cropMap.containsKey(args[1])){
            			sender.sendMessage("Please provide a player.");
            		}else
            			sender.sendMessage("That crop doesn't exist.");
            	}else
            		explain = true;
            }else if(args.length == 3){
            	if(args[0].equalsIgnoreCase("give")){
            		if(cropMap.containsKey(args[1])){
            			if(Bukkit.getOfflinePlayer(args[2]).isOnline())
            				Bukkit.getPlayer(args[2]).getInventory().addItem(cropMap.get(args[1]).seed.clone());
            			else
            				sender.sendMessage("That player isn't online.");
            		}else
            			sender.sendMessage("That crop doesn't exist.");
            	}else
            		explain = true;
            }else if(args.length == 4){
            	if(args[0].equalsIgnoreCase("give")){
            		if(cropMap.containsKey(args[1])){
            			if(Bukkit.getOfflinePlayer(args[2]).isOnline()){
            				try{
	            				ItemStack seed = cropMap.get(args[1]).seed.clone();
	            				seed.setAmount(Integer.valueOf(args[3]));
	            				Bukkit.getPlayer(args[2]).getInventory().addItem(seed);
            				}catch(Exception e){
            					sender.sendMessage("That's not a number.");
            				}
            			}
            			else
            				sender.sendMessage("That player isn't online.");
            		}else
            			sender.sendMessage("That crop doesn't exist.");
            	}else
            		explain = true;
            }
            if(explain){
            	sender.sendMessage("FarmCraft Commands\n"
            			+ "/farmcraft seedfix" + ChatColor.GRAY + " - Combines seeds into a single stack.");
            	sender.sendMessage("FarmCraft Admin Commands");
            	sender.sendMessage("/farmcraft list" + ChatColor.GRAY + " - Lists all available crops.");
            	sender.sendMessage("/farmcraft reload" + ChatColor.GRAY + " - I refuse to explain this.");
            	sender.sendMessage("/farmcraft give <crop> (player) (amount)" + ChatColor.GRAY + " - Gives crop seeds.");
            }
        }
        return false;
    }
    
    @EventHandler
    public void onPlant(PlayerInteractEvent e){
    	if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().equals(Material.FARMLAND)){
    		for(String cropName : cropMap.keySet()){
    			Crop c = cropMap.get(cropName);
    			if(c.seed.isSimilar(e.getItem())){
    				//They right clicked with seed
    				if(debug)
    					Bukkit.broadcastMessage("Attempted to plant " + cropName);
    				e.setCancelled(true);
    				Player p = e.getPlayer();
    				if(c.requiredLight == -1 || e.getClickedBlock().getLightLevel() >= c.requiredLight){
    					//Light check passed
    					boolean waterCheck = false;
    					if(c.requiredWaterRadius == -1)
    						waterCheck = true;
    					else{
    						for(int dX = 0;dX < c.requiredWaterRadius;dX++){
    							for(int dZ = 0;dZ < c.requiredWaterRadius;dZ++){
    								if(waterCheck)
    									continue;
    								if(p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(dX, 0, dZ)).getType().equals(Material.LEGACY_STATIONARY_WATER)
    										|| p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(-dX, 0, dZ)).getType().equals(Material.LEGACY_STATIONARY_WATER)
    										|| p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(dX, 0, -dZ)).getType().equals(Material.LEGACY_STATIONARY_WATER)
    										|| p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(-dX, 0, -dZ)).getType().equals(Material.LEGACY_STATIONARY_WATER)){
    									waterCheck = true;
    								}
    							}
    						}
    					}
    					if(waterCheck){
    						//Water check passed
    						p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
    						String locString = getStringUtils().toLocString(e.getClickedBlock().getLocation().add(0, 1, 0), false, false, null);
    						growingSeeds.set(cropName + "." + locString, System.currentTimeMillis());
    						growingSeeds.set("Percentages." + locString, 0);
    						growingSeeds.save();
    						seedInfo.set(locString + ".crop", cropName);
    						seedInfo.set(locString + ".player", p.getName());
    						seedInfo.set(locString + ".planted", System.currentTimeMillis());
    						seedInfo.save();
    						e.getClickedBlock().setType(Material.DIRT);
    						e.getClickedBlock().getLocation().add(0, 1, 0).getBlock().setType(Material.OAK_LEAVES);
    						Block b = e.getClickedBlock().getLocation().add(0, 2, 0).getBlock();
    						b.setType(Material.PLAYER_HEAD);
//    						try {
//    						  Block b = e.getClickedBlock().getLocation().add(0.0D, 2.0D, 0.0D).getBlock();
//    			              b.setType(Material.SKULL);
//    			              MaterialData data = b.getState().getData();
//    			              data.setData((byte)1);
//    			              b.getState().setData(data);
//    			              b.getState().update(true);
//    						}
    						setSkullUrl(c.harvestData.get(0).get(0).harvest.headTexture, b);
    					}
    				}
    				break;
    			}
    		}
    	}
    }
    
    @EventHandler
    public void onHarvest(BlockBreakEvent e){
    	if(e.getBlock().getType().equals(Material.PLAYER_HEAD)){
    		Location dummy = e.getBlock().getLocation();
    		dummy = new Location(dummy.getWorld(), dummy.getX(), dummy.getY() - 1, dummy.getZ());
    		String locString = getStringUtils().toLocString(dummy, false, false, null);
    		for(String cropName : cropMap.keySet()){
    			if(growingSeeds.contains(cropName + "." + locString) || (grownSeeds.contains(locString) && grownSeeds.getString(locString).equals(cropName))){
    				if(debug)
    					Bukkit.broadcastMessage("Attempting to harvest " + cropName);
    				e.setCancelled(true);
    				e.getBlock().setType(Material.AIR);
    				e.getBlock().getLocation().add(0, -1, 0).getBlock().setType(Material.AIR);
    				Crop c = cropMap.get(cropName);
					getStringUtils().fromLocString(locString, false);
					long alive = System.currentTimeMillis() - growingSeeds.getLong(cropName + "." + locString);
					int percent = (int) ((alive / 1000D) / (double) c.growTime * 100D);
					int closestHarvestPercent = 0;
					for(int p : c.harvestData.keySet()){
						if(p > closestHarvestPercent && p <= percent)
							closestHarvestPercent = p;
					}
					Harvest h = getHarvest(c, closestHarvestPercent);
					for(ItemStack i : h.drops){
						e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), i);
					}
					if(growingSeeds.contains(cropName + "." + locString)){
						growingSeeds.set(cropName + "." + locString, null);
						growingSeeds.set("Percentages." + locString, null);
						growingSeeds.save();
					}else{
						grownSeeds.set(locString, null);
						grownSeeds.save();
					}
					seedInfo.set(locString, null);
					seedInfo.save();
					return;
    			}
    		}
    	}
    	if(e.getBlock().getType().equals(Material.OAK_LEAVES)){
    		String locString = getStringUtils().toLocString(e.getBlock().getLocation(), false, false, null);
    		for(String cropName : cropMap.keySet()){
    			if(growingSeeds.contains(cropName + "." + locString) || (grownSeeds.contains(locString) && grownSeeds.getString(locString).equals(cropName))){
    				if(debug)
    					Bukkit.broadcastMessage("Attempting to harvest " + cropName);
    				e.setCancelled(true);
    				e.getBlock().setType(Material.AIR);
    				e.getBlock().getLocation().add(0, 1, 0).getBlock().setType(Material.AIR);
    				Crop c = cropMap.get(cropName);
					getStringUtils().fromLocString(locString, false);
					long alive = System.currentTimeMillis() - growingSeeds.getLong(cropName + "." + locString);
					int percent = (int) ((alive / 1000D) / (double) c.growTime * 100D);
					int closestHarvestPercent = 0;
					for(int p : c.harvestData.keySet()){
						if(p > closestHarvestPercent && p <= percent)
							closestHarvestPercent = p;
					}
					for(ItemStack i : getHarvest(c, closestHarvestPercent).drops){
						e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), i);
					}
					if(growingSeeds.contains(cropName + "." + locString)){
						growingSeeds.set(cropName + "." + locString, null);
						growingSeeds.set("Percentages." + locString, null);
						growingSeeds.save();
					}else{
						grownSeeds.set(locString, null);
						grownSeeds.save();
					}
					seedInfo.set(locString, null);
					seedInfo.save();
					return;
    			}
    		}
    	}
    }
    
    public Harvest getHarvest(Crop c, int percent){
    	List<PotentialHarvest> potentialHarvests = c.harvestData.get(percent);
    	List<PotentialHarvest> fixedPotentialHarvests = new ArrayList<PotentialHarvest>();
    	int total = 0;
    	for(PotentialHarvest dummy : potentialHarvests){
    		PotentialHarvest ph = new PotentialHarvest(dummy.chance, dummy.harvest);
    		int lChance = total;
    		total+=ph.chance;
    		int hChance = total - 1;
    		ph.chance = lChance;
    		ph.mChance = hChance;
    		fixedPotentialHarvests.add(ph);
    	}
    	int random = (int) (Math.random() * total);
    	for(PotentialHarvest ph : fixedPotentialHarvests){
    		if(random >= ph.chance && random <= ph.mChance){
    			return ph.harvest;
    		}
    	}
    	return null;
    }
    
    @EventHandler
    public void onGetInfo(PlayerInteractEvent e){
    	if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().equals(Material.PLAYER_HEAD)){
    		if(!getInfoMap.containsKey(e.getPlayer()))
    			getInfoMap.put(e.getPlayer(), 0L);
    		long delta = System.currentTimeMillis() - getInfoMap.get(e.getPlayer());
    		if(delta < 75)
    			return;
    		getInfoMap.put(e.getPlayer(), System.currentTimeMillis());
    		String locString = getStringUtils().toLocString(e.getClickedBlock().getLocation().add(0, -1, 0), false, false, null);
    		if(seedInfo.contains(locString)){
    			//They clicked a seed
    			e.setCancelled(true);
    			if(!getConfig().getBoolean("Crop_Info_Use_Hologram")){
	    			Player p = e.getPlayer();
	    			for(String s : getConfig().getStringList("Crop_Info_Chat")){
	    				p.sendMessage(formatString(s, locString));
	    			}
    			}else{
    				hologramShit.handle(this, e, locString);
    			}
    		}
    	}
    }
    
    @EventHandler
    public void onLeafDecay(LeavesDecayEvent e){
    	String locString = getStringUtils().toLocString(e.getBlock().getLocation(), false, false, null);
    	if(seedInfo.contains(locString))
    		e.setCancelled(true);
    }
    
    @EventHandler
    public void onGrassBreak(BlockBreakEvent e){
    	if((e.getBlock().getType().equals(Material.TALL_GRASS) || e.getBlock().getType().equals(Material.GRASS)) && getConfig().getBoolean("Enable_Seed_Drop")){
    		ConfigurationSection conSec = getConfig().getConfigurationSection("Seed_Drop_Chance");
    		for(String cropName : conSec.getKeys(false)){
    			double number = Math.random() * 100;
    			if(number <= conSec.getDouble(cropName)){
    				Bukkit.broadcastMessage("3");
    				e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), cropMap.get(cropName).seed);
    				return;
    			}
    		}
    	}
    }
    
    public static void setSkullUrl(String skinUrl, Block block) {
        block.setType(Material.PLAYER_HEAD);
        Skull skullData = (Skull)block.getState();
        try{
        	
	        Object reflectWorld = ReflectionUtils.invokeMethod(block, "getWorld");
	        Object reflectHandle = ReflectionUtils.invokeMethod(reflectWorld, "getHandle");
	        Object reflectBlockPosition = ReflectionUtils.instantiateObject("BlockPosition", ReflectionUtils.PackageType.MINECRAFT_SERVER, block.getX(), block.getY(), block.getZ());
	        Object reflectTileEntity = ReflectionUtils.invokeMethod(reflectHandle, "getTileEntity", reflectBlockPosition);
	        ReflectionUtils.invokeMethod(reflectTileEntity, "setGameProfile", getNonPlayerProfile(skinUrl));
        }catch(Exception e){
        	
        }
//        TileEntitySkull skullTile = (TileEntitySkull)((CraftWorld) block.getWorld()).getHandle().getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
//        skullTile.setGameProfile(getNonPlayerProfile(skinUrl));
        block.getState().setRawData((byte) 1);
        block.getState().update(true);
    }
    public static GameProfile getNonPlayerProfile(String skinURL) {
        GameProfile newSkinProfile = new GameProfile(UUID.randomUUID(), null);
        newSkinProfile.getProperties().put("textures", new Property("textures", Base64Coder.encodeString("{textures:{SKIN:{url:\"http://textures.minecraft.net/texture/" + skinURL + "\"}}}")));
        return newSkinProfile;
    }
    
    private void loadShit(){
    	File folder = new File("plugins/FarmCraft/crops");
    	if(!folder.exists()){
    		try {
				YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("corn.yml"))).save("plugins/FarmCraft/crops/corn.yml");
			} catch (IOException e) {
				e.printStackTrace();
			}
			folder.mkdirs();
    	}
    	for(File cropFile : folder.listFiles()){
    		YamlConfiguration crop = YamlConfiguration.loadConfiguration(cropFile);
    		int requiredLight = -1;
    		int requiredWaterRadius = -1;
    		int growTime = crop.getInt("Grow_Time");
//    		ItemStack seed = ItemCreator.createItemFromConfiguration(crop.getConfigurationSection("Seed"));
    		ItemStack seed = getLoadItemstackFromConfig().getItem(crop.getConfigurationSection("Seed"));
    		Map<Integer, List<PotentialHarvest>> harvestData = new HashMap<Integer, List<PotentialHarvest>>();
    		for(String harvestID : crop.getConfigurationSection("Harvests").getKeys(false)){
    			ConfigurationSection harvest = crop.getConfigurationSection("Harvests." + harvestID);
    			int percent = harvest.getInt("percent");
    			String headTexture = harvest.getString("headtexture");
    			List<ItemStack> drops = new ArrayList<ItemStack>();
    			for(String itemID : harvest.getKeys(false)){
    				if(!itemID.equals("percent") && !itemID.equals("chance") && !itemID.equalsIgnoreCase("headtexture")){
//    					drops.add(ItemCreator.createItemFromConfiguration(harvest.getConfigurationSection(itemID)));
    					drops.add(getLoadItemstackFromConfig().getItem((harvest.getConfigurationSection(itemID))));
    				}
    			}
    			int chance = harvest.getInt("chance");
    			Harvest h = new Harvest(headTexture, drops);
    			List<PotentialHarvest> potentialHarvests = harvestData.containsKey(percent) ? harvestData.get(percent) : new ArrayList<PotentialHarvest>();
    			potentialHarvests.add(new PotentialHarvest(chance, h));
    			harvestData.put(percent, potentialHarvests);
    		}
    		Crop c = new Crop(growTime, requiredLight, requiredWaterRadius, seed, harvestData);
    		cropMap.put(cropFile.getName().replace(".yml", ""), c);
    		System.out.println(cropFile.getName().replace(".yml", "") + " loaded as crop successfully.");
    	}
    	//Now we schedule the check
    	Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {public void run() {
			for(String cropName : growingSeeds.getKeys(false)){
				if(cropName.equals("Percentages"))
					continue;
				Crop c = cropMap.get(cropName);
				if(!growingSeeds.contains(cropName))
					continue;
				for(String locString : growingSeeds.getConfigurationSection(cropName).getKeys(false)){
					Location loc = getStringUtils().fromLocString(locString, false);
//					loc.getBlock().setType(Material.LEAVES);
					long alive = System.currentTimeMillis() - growingSeeds.getLong(cropName + "." + locString);
					int percent = (int) ((alive / 1000D) / (double) c.growTime * 100D);
					int closestHarvestPercent = 0;
					for(int p : c.harvestData.keySet()){
						if(p > closestHarvestPercent && p <= percent)
							closestHarvestPercent = p;
					}
					if(growingSeeds.getInt("Percentages." + locString) != closestHarvestPercent){
						growingSeeds.set("Percentages." + locString, closestHarvestPercent);
						growingSeeds.save();
						//Get harvest TODO
						Harvest h = c.harvestData.get(closestHarvestPercent).get(0).harvest;
						//Got harvest ^
						setSkullUrl(h.headTexture, loc.add(0, 1, 0).getBlock());
						if(closestHarvestPercent == 100){
							//Fully grown
							growingSeeds.set(cropName + "." + locString, null);
							growingSeeds.set("Percentages." + locString, null);
							growingSeeds.save();
							grownSeeds.set(locString, cropName);
							grownSeeds.save();
						}
					}
				}
			}
		}}, 0, 100);
    }

    String formatString(String s, String locString){
    	String crop = seedInfo.getString(locString + ".crop");
    	while(s.contains("%farmcraft_crop%"))
    		s = s.replace("%farmcraft_crop%", crop);
    	while(s.contains("%farmcraft_planter%"))
    		s = s.replace("%farmcraft_planter%", seedInfo.getString(locString + ".player"));
    	long delta = System.currentTimeMillis() - seedInfo.getLong(locString + ".planted");
    	double grownDecimal = (delta / 1000) / cropMap.get(crop).growTime;
    	int grown = (int) (grownDecimal * 100);
    	grown = grown > 100 ? 100 : grown;
    	while(s.contains("%farmcraft_grown%"))
    		s = s.replace("%farmcraft_grown%", grown + "");
    	return s;
    }
    
    private void reloadShit(){
    	cropMap = new HashMap<String, Crop>();
    	reloadConfig();
    	growingSeeds.reload();
    	grownSeeds.reload();
    	seedInfo.reload();
    	loadShit();
    }
    
    public static String makeColors(String s){
        String replaced = s
                .replaceAll("&0", "" + ChatColor.BLACK)
                .replaceAll("&1", "" + ChatColor.DARK_BLUE)
                .replaceAll("&2", "" + ChatColor.DARK_GREEN)
                .replaceAll("&3", "" + ChatColor.DARK_AQUA)
                .replaceAll("&4", "" + ChatColor.DARK_RED)
                .replaceAll("&5", "" + ChatColor.DARK_PURPLE)
                .replaceAll("&6", "" + ChatColor.GOLD)
                .replaceAll("&7", "" + ChatColor.GRAY)
                .replaceAll("&8", "" + ChatColor.DARK_GRAY)
                .replaceAll("&9", "" + ChatColor.BLUE)
                .replaceAll("&a", "" + ChatColor.GREEN)
                .replaceAll("&b", "" + ChatColor.AQUA)
                .replaceAll("&c", "" + ChatColor.RED)
                .replaceAll("&d", "" + ChatColor.LIGHT_PURPLE)
                .replaceAll("&e", "" + ChatColor.YELLOW)
                .replaceAll("&f", "" + ChatColor.WHITE)
                .replaceAll("&r", "" + ChatColor.RESET)
                .replaceAll("&l", "" + ChatColor.BOLD)
                .replaceAll("&o", "" + ChatColor.ITALIC)
                .replaceAll("&k", "" + ChatColor.MAGIC)
                .replaceAll("&m", "" + ChatColor.STRIKETHROUGH)
                .replaceAll("&n", "" + ChatColor.UNDERLINE)
                .replaceAll("\\\\", " ");
        return replaced;
    }
}