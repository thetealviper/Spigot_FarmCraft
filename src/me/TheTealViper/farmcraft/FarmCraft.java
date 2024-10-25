package me.TheTealViper.farmcraft;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Levelled;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
//import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader; //Import this to break only Paper users >:)
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import me.TheTealViper.farmcraft.Utils.LoadItemstackFromConfig;
import me.TheTealViper.farmcraft.Utils.PluginFile;
import me.TheTealViper.farmcraft.Utils.StringUtils;
import me.TheTealViper.farmcraft.Utils.UtilityEquippedJavaPlugin;
 
public class FarmCraft extends UtilityEquippedJavaPlugin implements Listener{
	public Map<String, Crop> cropMap = new HashMap<String, Crop>();
	public PluginFile growingSeeds = new PluginFile(this, "growing.data");
	public PluginFile grownSeeds = new PluginFile(this, "grown.data");
	public PluginFile seedInfo = new PluginFile(this, "info.data");
	public Map<Player, Long> getInfoMap = new HashMap<Player, Long>();
	public static boolean debug = false;
	public Map<Player, Long> purgeInfo = new HashMap<Player, Long>();
	public PluginFile messagesYml;
 
    public void onEnable(){
    	StartupPlugin(this, "50031");
    	
    	loadShit();
    }
   
    public void onDisable(){
        //getLogger().info("FarmCraft from TheTealViper shutting down. Bshzzzzzz");
    }
    
    @SuppressWarnings("deprecation")
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
            				if(p.getInventory().getItem(i) != null && LoadItemstackFromConfig.isSimilar(p.getInventory().getItem(i), seed)){
            					amount += p.getInventory().getItem(i).getAmount();
            					p.getInventory().getItem(i).setAmount(0);
            				}
            			}
            			seed.setAmount(amount);
            			p.getInventory().setItem(p.getInventory().firstEmpty(), seed);
            		}
            	}else if(args[0].equalsIgnoreCase("reload") && p.hasPermission("farmcraft.admin")){
            		p.sendMessage(StringUtils.makeColors(messagesYml.getString("Reloading")));
            		reloadShit();
            	}else
            		explain = true;
            }else if(args.length == 2){
            	if(args[0].equalsIgnoreCase("give") && p.hasPermission("farmcraft.admin")){
            		if(cropMap.containsKey(args[1])){
            			p.getInventory().addItem(cropMap.get(args[1]).seed.clone());
            		}else
            			p.sendMessage(StringUtils.makeColors(messagesYml.getString("CropDoesntExist")));
            	}else if(args[0].equalsIgnoreCase("purge") && p.hasPermission("farmcraft.admin")){
            		if(cropMap.containsKey(args[1])){
            			if(!purgeInfo.containsKey(p))
            				purgeInfo.put(p, 0L);
            			if(System.currentTimeMillis() - purgeInfo.get(p) > 5000){
            				p.sendMessage(StringUtils.makeColors(messagesYml.getString("PurgeDoubleCheck")));
            				purgeInfo.put(p, System.currentTimeMillis());
            			}else{
            				p.sendMessage(StringUtils.makeColors(messagesYml.getString("PurgeStart")));
            				int purgedAmount = 0;
            				ConfigurationSection sec = growingSeeds.getConfigurationSection(args[1]);
            				for(String s : sec.getKeys(false)){
            					Location loc = StringUtils.fromLocString(s, false);
            					loc.getBlock().setType(Material.AIR);
            					loc.add(0, 1, 0).getBlock().setType(Material.AIR);
            					purgedAmount++;
            					growingSeeds.set(args[1] + "." + s, null);
            					growingSeeds.save();
            				}
            				for(String s : grownSeeds.getKeys(false)){
            					if(grownSeeds.getString(s).equals(args[1])){
            						Location loc = StringUtils.fromLocString(s, false);
                					loc.getBlock().setType(Material.AIR);
                					loc.add(0, 1, 0).getBlock().setType(Material.AIR);
                					purgedAmount++;
                					grownSeeds.set(s, null);
                					grownSeeds.save();
            					}
            				}
            				p.sendMessage(StringUtils.makeColors(messagesYml.getString("PurgeSummary")).replaceAll("%purgeamount%", "" + purgedAmount));
            			}
            		}else{
            			p.sendMessage(StringUtils.makeColors(messagesYml.getString("CropDoesntExist")));
            		}
            	}else
            		explain = true;
            }else if(args.length == 3){
            	if(args[0].equalsIgnoreCase("give") && p.hasPermission("farmcraft.admin")){
            		if(cropMap.containsKey(args[1])){
            			if(Bukkit.getOfflinePlayer(args[2]).isOnline())
            				Bukkit.getPlayer(args[2]).getInventory().addItem(cropMap.get(args[1]).seed.clone());
            			else
            				p.sendMessage(StringUtils.makeColors(messagesYml.getString("PlayerIsntOnline")));
            		}else
            			p.sendMessage(StringUtils.makeColors(messagesYml.getString("CropDoesntExist")));
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
            					p.sendMessage(StringUtils.makeColors(messagesYml.getString("NotANumber")));
            				}
            			}
            			else
            				p.sendMessage(StringUtils.makeColors(messagesYml.getString("PlayerIsntOnline")));
            		}else
            			p.sendMessage(StringUtils.makeColors(messagesYml.getString("CropDoesntExist")));
            	}else
            		explain = true;
            }
            if(explain){
            	for (String s : messagesYml.getStringList("FarmCraftStandardCommands")) {
            		p.sendMessage(StringUtils.makeColors(s));
            	}
            	if(p.hasPermission("farmcraft.admin")){
            		for (String s : messagesYml.getStringList("FarmCraftAdminCommands")) {
                		p.sendMessage(StringUtils.makeColors(s));
                	}
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
            			sender.sendMessage(StringUtils.makeColors(messagesYml.getString("PlayerNotProvided")));
            		}else
            			sender.sendMessage(StringUtils.makeColors(messagesYml.getString("CropDoesntExist")));
            	}else
            		explain = true;
            }else if(args.length == 3){
            	if(args[0].equalsIgnoreCase("give")){
            		if(cropMap.containsKey(args[1])){
            			if(Bukkit.getOfflinePlayer(args[2]).isOnline())
            				Bukkit.getPlayer(args[2]).getInventory().addItem(cropMap.get(args[1]).seed.clone());
            			else
            				sender.sendMessage(StringUtils.makeColors(messagesYml.getString("PlayerIsntOnline")));
            		}else
            			sender.sendMessage(StringUtils.makeColors(messagesYml.getString("CropDoesntExist")));
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
            					sender.sendMessage(StringUtils.makeColors(messagesYml.getString("NotANumber")));
            				}
            			}
            			else
            				sender.sendMessage(StringUtils.makeColors(messagesYml.getString("PlayerIsntOnline")));
            		}else
            			sender.sendMessage(StringUtils.makeColors(messagesYml.getString("CropDoesntExist")));
            	}else
            		explain = true;
            }
            if(explain){
            	for (String s : messagesYml.getStringList("FarmCraftStandardCommands")) {
            		sender.sendMessage(StringUtils.makeColors(s));
            	}
        		for (String s : messagesYml.getStringList("FarmCraftAdminCommands")) {
            		sender.sendMessage(StringUtils.makeColors(s));
            	}
            }
        }
        return false;
    }
    
    @EventHandler
    public void onPlant(PlayerInteractEvent e){
    	//TODO: Change these disgustingly nested if statements to guard format (if's break, not permit further nesting)
    	if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && e.getItem().getType() != null && !e.getItem().getType().equals(Material.AIR)){
    		if (debug)
    			Bukkit.broadcastMessage("Right clicked block");
    		for(String cropName : cropMap.keySet()){
    			if (debug) {
    				Bukkit.broadcastMessage("Testing against: " + cropName);
    			}
    			Crop c = cropMap.get(cropName);
				if(LoadItemstackFromConfig.isSimilar(c.seed.clone(), e.getItem())){
    				//They right clicked with seed
    				if(debug)
    					Bukkit.broadcastMessage("Attempted to plant " + cropName);
    				e.setCancelled(true);
    				if(e.getClickedBlock().getType().equals(Material.FARMLAND)) {
    					Player p = e.getPlayer();
        				if(c.requiredLight == -1 || e.getClickedBlock().getLightLevel() >= c.requiredLight){
        					//Light check passed
        					boolean waterCheck = false;
        					if(c.requiredWaterRadius == -1)
        						waterCheck = true;
        					else{
        						//TODO: Fix this water check. This is disgusting. Why did I code it this way?
        						for(int dX = 1;dX < c.requiredWaterRadius+1;dX++){
        							for(int dZ = 1;dZ < c.requiredWaterRadius+1;dZ++){
        								if(waterCheck)
        									continue;
        								if(p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(dX, 0, dZ)).getType().equals(Material.WATER)){
        									if(debug)
        										Bukkit.broadcastMessage("Found Water");
        									Levelled blockData = (Levelled) p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(dX, 0, dZ)).getBlockData();
        									if(blockData.getLevel() == 0) {
        										if(debug)
        											Bukkit.broadcastMessage("Source water");
        										waterCheck = true;
        									}
        								}
        								if(p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(-dX, 0, dZ)).getType().equals(Material.WATER)) {
        									if(debug)
        										Bukkit.broadcastMessage("Found Water");
        									Levelled blockData = (Levelled) p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(-dX, 0, dZ)).getBlockData();
        									if(blockData.getLevel() == 0) {
        										if(debug)
        											Bukkit.broadcastMessage("Source water");
        										waterCheck = true;
        									}
        								}
        								if(p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(dX, 0, -dZ)).getType().equals(Material.WATER)) {
        									if(debug)
        										Bukkit.broadcastMessage("Found Water");
        									Levelled blockData = (Levelled) p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(dX, 0, -dZ)).getBlockData();
        									if(blockData.getLevel() == 0) {
        										if(debug)
        											Bukkit.broadcastMessage("Source water");
        										waterCheck = true;
        									}
        								}
        								if(p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(-dX, 0, -dZ)).getType().equals(Material.WATER)) {
        									if(debug)
        										Bukkit.broadcastMessage("Found Water");
        									Levelled blockData = (Levelled) p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(-dX, 0, -dZ)).getBlockData();
        									if(blockData.getLevel() == 0) {
        										if(debug)
        											Bukkit.broadcastMessage("Source water");
        										waterCheck = true;
        									}
        								}
        								if(p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(0, 0, dZ)).getType().equals(Material.WATER)) {
        									if(debug)
        										Bukkit.broadcastMessage("Found Water");
        									Levelled blockData = (Levelled) p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(0, 0, dZ)).getBlockData();
        									if(blockData.getLevel() == 0) {
        										if(debug)
        											Bukkit.broadcastMessage("Source water");
        										waterCheck = true;
        									}
        								}
        								if(p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(0, 0, -dZ)).getType().equals(Material.WATER)) {
        									if(debug)
        										Bukkit.broadcastMessage("Found Water");
        									Levelled blockData = (Levelled) p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(0, 0, -dZ)).getBlockData();
        									if(blockData.getLevel() == 0) {
        										if(debug)
        											Bukkit.broadcastMessage("Source water");
        										waterCheck = true;
        									}
        								}
        								if(p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(dX, 0, 0)).getType().equals(Material.WATER)) {
        									if(debug)
        										Bukkit.broadcastMessage("Found Water");
        									Levelled blockData = (Levelled) p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(dX, 0, 0)).getBlockData();
        									if(blockData.getLevel() == 0) {
        										if(debug)
        											Bukkit.broadcastMessage("Source water");
        										waterCheck = true;
        									}
        								}
        								if(p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(-dX, 0, 0)).getType().equals(Material.WATER)) {
        									if(debug)
        										Bukkit.broadcastMessage("Found Water");
        									Levelled blockData = (Levelled) p.getWorld().getBlockAt(e.getClickedBlock().getLocation().add(-dX, 0, 0)).getBlockData();
        									if(blockData.getLevel() == 0) {
        										if(debug)
        											Bukkit.broadcastMessage("Source water");
        										waterCheck = true;
        									}
        								}
        							}
        						}
        					}
        					if(waterCheck){
        						//Water check passed
        						if(p.getWorld().getName().contains("_")) {
        							p.sendMessage(StringUtils.makeColors(messagesYml.getString("CantPlantWorldName")));
        							return;
        						}
        						ExperienceManager xpman = new ExperienceManager(p);
        						if(xpman.getCurrentExp() < c.requiredXP) {
        							p.sendMessage(StringUtils.makeColors(messagesYml.getString("RequiredXP")).replaceAll("%requiredxp%", c.requiredXP + "").replaceAll("%currentxp%", xpman.getCurrentExp() + ""));
        							return;
        						}
        						//XP check passed
        						
        						e.getItem().setAmount(e.getItem().getAmount() - 1);
        						String locString = StringUtils.toLocString(e.getClickedBlock().getLocation().add(0, 1, 0), false, false, null);
        						growingSeeds.set(cropName + "." + locString, System.currentTimeMillis());
        						growingSeeds.set("Percentages." + locString, 0);
        						growingSeeds.save();
        						seedInfo.set(locString + ".crop", cropName);
        						seedInfo.set(locString + ".player", p.getName());
        						seedInfo.set(locString + ".planted", System.currentTimeMillis());
        						seedInfo.save();
//        						e.getClickedBlock().setType(Material.DIRT);
        						final Material originalBaseBlockMat = e.getClickedBlock().getType();
        						final Block originalBlock = e.getClickedBlock();
        						getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {public void run() {
        							if (originalBlock.getType() != null && !originalBlock.getType().equals(Material.AIR)) //Prevent duping
        								originalBlock.setType(originalBaseBlockMat);
								}}, 3);
        						e.getClickedBlock().getLocation().add(0, 1, 0).getBlock().setType(c.leafBlockMaterial);
        						Block b = e.getClickedBlock().getLocation().add(0, 2, 0).getBlock();
        						b.setType(Material.PLAYER_HEAD);
//        						try {
//        						  Block b = e.getClickedBlock().getLocation().add(0.0D, 2.0D, 0.0D).getBlock();
//        			              b.setType(Material.SKULL);
//        			              MaterialData data = b.getState().getData();
//        			              data.setData((byte)1);
//        			              b.getState().setData(data);
//        			              b.getState().update(true);
//        						}
        						setSkullUrl(c.harvestData.get(0).get(0).harvest.headTexture, b);
        					} else {
        						p.sendMessage(StringUtils.makeColors(messagesYml.getString("RequiredWater")).replaceAll("%requiredwaterradius%", "" + c.requiredWaterRadius));
        					}
        				} else {
        					p.sendMessage(StringUtils.makeColors(messagesYml.getString("RequiredLight")).replaceAll("%requiredlight%", ""+c.requiredLight).replaceAll("%currentlight%", ""+e.getClickedBlock().getLightLevel()));
        				}
    				}
    				break;
    			}
    		}
    	}
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHarvest(BlockBreakEvent e){
    	if(e.isCancelled() ||
    			(!e.getBlock().getType().equals(Material.PLAYER_HEAD) && !e.getBlock().getRelative(0, 1, 0).getType().equals(Material.PLAYER_HEAD)))
    		return;
    	
    	attemptHarvest(e, e.getBlock(), e.getPlayer(), false);
    }
    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent e) {
    	Bukkit.getServer().getLogger().info("Explosion");
    	if(e.isCancelled() || !e.getEntityType().equals(EntityType.CREEPER))
    		return;
    	
    	for (Block b : e.blockList()) {
    		if (b.getType().equals(Material.AIR))
    			continue;
    		if (attemptHarvest(e, b, null, true)) {
    			break;
    		}
    	}
    }
    public boolean attemptHarvest(Cancellable e, Block b, Player player, boolean isCreeperExplosion) {
    	Location dummy = b.getLocation();
    	Material blocktype = b.getType();
    	if (blocktype.equals(Material.PLAYER_HEAD))
    		dummy = new Location(dummy.getWorld(), dummy.getX(), dummy.getY() - 1, dummy.getZ());
    	String locString = StringUtils.toLocString(dummy, false, false, null);
		for(String cropName : cropMap.keySet()){
			if(growingSeeds.contains(cropName + "." + locString) || (grownSeeds.contains(locString) && grownSeeds.getString(locString).equals(cropName))){
				if(debug)
					Bukkit.broadcastMessage("Attempting to harvest " + cropName);
				if (!isCreeperExplosion) {
					e.setCancelled(true);
				}
				b.setType(Material.AIR);
				b.getLocation().add(0, blocktype.equals(Material.PLAYER_HEAD) ? -1 : 1, 0).getBlock().setType(Material.AIR);
				Crop c = cropMap.get(cropName);
				StringUtils.fromLocString(locString, false);
				long alive = System.currentTimeMillis() - growingSeeds.getLong(cropName + "." + locString);
				int percent = (int) ((alive / 1000D) / (double) c.growTime * 100D);
				int closestHarvestPercent = 0;
				for(int p : c.harvestData.keySet()){
					if(p > closestHarvestPercent && p <= percent)
						closestHarvestPercent = p;
				}
				Harvest h = getHarvest(c, closestHarvestPercent);
				for(ItemStack i : h.drops){
					b.getWorld().dropItemNaturally(b.getLocation(), i);
				}
				if (player != null)
					XPHandler.addXP(player, h.XP);
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
				return true;
			}
		}
		return false;
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
    		String locString = StringUtils.toLocString(e.getClickedBlock().getLocation().add(0, -1, 0), false, false, null);
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
    	checkCancelLeafBlockNaturalChange(e, e.getBlock());
    }
    @EventHandler
    public void onFernGrow(BlockGrowEvent e){
    	checkCancelLeafBlockNaturalChange(e, e.getBlock());
    }
    public void checkCancelLeafBlockNaturalChange(Cancellable e, Block b) {
    	String locString = StringUtils.toLocString(b.getLocation(), false, false, null);
    	if(seedInfo.contains(locString))
    		e.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGrassBreak(BlockBreakEvent e){
    	if(e.isCancelled())
    		return;
    	
    	if((e.getBlock().getType().equals(Material.TALL_GRASS) || e.getBlock().getType().equals(Material.SHORT_GRASS)) && getConfig().getBoolean("Enable_Seed_Drop")){
    		ConfigurationSection conSec = getConfig().getConfigurationSection("Seed_Drop_Chance");
    		for(String cropName : conSec.getKeys(false)){
    			double number = Math.random() * 100;
    			if(number <= conSec.getDouble(cropName)){
    				e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), cropMap.get(cropName).seed);
    				return;
    			}
    		}
    	}
    }
    
    public static void setSkullUrl(String skinTexture, Block block) {
        block.setType(Material.PLAYER_HEAD);
        Skull skullData = (Skull) block.getState();
        try{
	        PlayerProfile pp = Bukkit.createPlayerProfile(UUID.fromString("9c1917c9-95e1-4042-8f9c-f5cc653d266b")); //Random UUID representing heads made from this plugin.
	        PlayerTextures pt = pp.getTextures();
	        try {
				pt.setSkin(new URL("http://textures.minecraft.net/texture/" + skinTexture));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
	        pp.setTextures(pt);
	        skullData.setOwnerProfile(pp);
//	        skullData.setRawData((byte) 1);
	        skullData.update(true);
        }catch(Exception e){
        	e.printStackTrace();
        }
    }
    
    private void loadShit(){
    	//Initialize Files
    	File messagesYmlFile = new File("plugins/FarmCraft/messages.yml");
    	if (!messagesYmlFile.exists()) {
    		try {
    			Files.copy(getClass().getResourceAsStream("/messages.yml"), messagesYmlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	messagesYml = new PluginFile(this, "messages.yml");
    	File folder = new File("plugins/FarmCraft/crops");
    	if(!folder.exists()){
    		try {
				YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("corn.yml"))).save("plugins/FarmCraft/crops/corn.yml");
				YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("tomato.yml"))).save("plugins/FarmCraft/crops/tomato.yml");
			} catch (IOException e) {
				e.printStackTrace();
			}
			folder.mkdirs();
    	}
    	//Initialize Crops From Files
    	for(File cropFile : folder.listFiles()){
    		YamlConfiguration crop = YamlConfiguration.loadConfiguration(cropFile);
    		int requiredLight = crop.getInt("Required_Light");
    		int requiredWaterRadius = crop.getInt("Require_Water_Radius");
    		int requiredXP = !crop.contains("Required_XP") ? -1 : crop.getInt("Required_XP");
    		int growTime = crop.getInt("Grow_Time");
//    		ItemStack seed = ItemCreator.createItemFromConfiguration(crop.getConfigurationSection("Seed"));
    		ItemStack seed = getLoadItemstackFromConfig().getItem(crop.getConfigurationSection("Seed"));
    		Map<Integer, List<PotentialHarvest>> harvestData = new HashMap<Integer, List<PotentialHarvest>>();
    		Material leafBlockMaterial = crop.contains("Leaf_Block_Material") ? Material.getMaterial(crop.getString("Leaf_Block_Material")) : Material.OAK_LEAVES;
    		for(String harvestID : crop.getConfigurationSection("Harvests").getKeys(false)){
    			ConfigurationSection harvest = crop.getConfigurationSection("Harvests." + harvestID);
    			int percent = harvest.getInt("percent");
    			String headTexture = harvest.getString("headtexture");
    			List<ItemStack> drops = new ArrayList<ItemStack>();
    			int XP = 0;
    			for(String itemID : harvest.getKeys(false)){
    				if(!itemID.equals("percent") && !itemID.equals("chance") && !itemID.equalsIgnoreCase("headtexture") && !itemID.equalsIgnoreCase("xp")){
//    					drops.add(ItemCreator.createItemFromConfiguration(harvest.getConfigurationSection(itemID)));
    					drops.add(getLoadItemstackFromConfig().getItem((harvest.getConfigurationSection(itemID))));
    				}
    			}
    			int chance = harvest.getInt("chance");
    			if (harvest.contains("xp"))
					XP = harvest.getInt("xp");
    			Harvest h = new Harvest(headTexture, drops, XP);
    			List<PotentialHarvest> potentialHarvests = harvestData.containsKey(percent) ? harvestData.get(percent) : new ArrayList<PotentialHarvest>();
    			potentialHarvests.add(new PotentialHarvest(chance, h));
    			harvestData.put(percent, potentialHarvests);
    		}
    		Crop c = new Crop(growTime, requiredLight, requiredWaterRadius, requiredXP, seed, harvestData, leafBlockMaterial);
    		cropMap.put(cropFile.getName().replace(".yml", ""), c);
    		System.out.println(cropFile.getName().replace(".yml", "") + " loaded as crop successfully.");
    	}
    	//Now we schedule the growth check
    	Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {public void run() {
			for(String cropName : growingSeeds.getKeys(false)){
				if(cropName.equals("Percentages"))
					continue;
				Crop c = cropMap.get(cropName);
				if(!growingSeeds.contains(cropName))
					continue;
				for(String locString : growingSeeds.getConfigurationSection(cropName).getKeys(false)){
					Location loc = StringUtils.fromLocString(locString, false);
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
						//Get harvest
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
    	return StringUtils.makeColors(s);
//        String replaced = s
//                .replaceAll("&0", "" + ChatColor.BLACK)
//                .replaceAll("&1", "" + ChatColor.DARK_BLUE)
//                .replaceAll("&2", "" + ChatColor.DARK_GREEN)
//                .replaceAll("&3", "" + ChatColor.DARK_AQUA)
//                .replaceAll("&4", "" + ChatColor.DARK_RED)
//                .replaceAll("&5", "" + ChatColor.DARK_PURPLE)
//                .replaceAll("&6", "" + ChatColor.GOLD)
//                .replaceAll("&7", "" + ChatColor.GRAY)
//                .replaceAll("&8", "" + ChatColor.DARK_GRAY)
//                .replaceAll("&9", "" + ChatColor.BLUE)
//                .replaceAll("&a", "" + ChatColor.GREEN)
//                .replaceAll("&b", "" + ChatColor.AQUA)
//                .replaceAll("&c", "" + ChatColor.RED)
//                .replaceAll("&d", "" + ChatColor.LIGHT_PURPLE)
//                .replaceAll("&e", "" + ChatColor.YELLOW)
//                .replaceAll("&f", "" + ChatColor.WHITE)
//                .replaceAll("&r", "" + ChatColor.RESET)
//                .replaceAll("&l", "" + ChatColor.BOLD)
//                .replaceAll("&o", "" + ChatColor.ITALIC)
//                .replaceAll("&k", "" + ChatColor.MAGIC)
//                .replaceAll("&m", "" + ChatColor.STRIKETHROUGH)
//                .replaceAll("&n", "" + ChatColor.UNDERLINE)
//                .replaceAll("\\\\", " ");
//        return replaced;
    }
}