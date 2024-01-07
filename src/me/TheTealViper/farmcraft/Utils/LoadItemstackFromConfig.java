package me.TheTealViper.farmcraft.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

public class LoadItemstackFromConfig {
	/**
	 * id: DIRT
	 * amount: 1
	 * name: "Dirt"
	 * lore:
	 *  - "Lore Line 1"
	 *  - "Lore Line 2"
	 * enchantments:
	 * 	- "arrowdamage:1"
	 *  - "arrowfire:1"
	 *  - "arrowinfinite:1"
	 *  - "arrowknockback:1"
	 *  - "damage:1"
	 *  - "digspeed:1"
	 *  - "durability:1"
	 *  - "fireaspect:1"
	 *  - "knockback:1"
	 *  - "lootbonusblock:1"
	 *  - "lootbonusmob:1"
	 *  - "luck:1"
	 *  - "protectionfall:1"
	 *  - "protectionfire:1"
	 *  - "silktouch:1"
	 * tags:
	 *  - "textureskull:SKINVALUE"
	 *  - "playerskull:PLAYERNAME"
	 *  - "vanilladurability:256"
	 *  - "unbreakable:true"
	 *  - "custommodeldata:1234567"
	 * flags:
	 *  - "HIDE_ATTRIBUTES"
	 *  - "HIDE_DESTROYS"
	 *  - "HIDE_ENCHANTS"
	 *  - "HIDE_PLACED_ON"
	 *  - "HIDE_POTION_EFFECTS"
	 *  - "HIDE_UNBREAKABLE"
	 * attributes:
	 *  - "ATTRIBUTE:VALUE:OPERATION"
	 *  - "ATTRIBUTE:VALUE:OPERATION:SLOT"
	 *  - ATTRIBUTE NAMES FOUND @ https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html 
	 *  - ATTRIBUTE OPERATIONS FOUND @ https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/AttributeModifier.Operation.html
	 *  - ATTRIBUTE SLOTS FOUND @ https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/EquipmentSlot.html
	 */
	
	public LoadItemstackFromConfig(UtilityEquippedJavaPlugin plugin){
	}

	@SuppressWarnings("deprecation")
	public ItemStack getItem(ConfigurationSection sec) {
		//Null check
		if(sec == null)
			return null;
		ItemStack item = null;
		boolean modifiedMetaSoApply = false;
		
		//Handle ID
		item = (sec == null || !sec.contains("id")) ? null : new ItemStack(Material.getMaterial(sec.getString("id")));
		
		//Initiate Meta
		ItemMeta meta = item.getItemMeta();
		
		//Handle amount
		if(sec.contains("amount")) item.setAmount(sec.getInt("amount"));
		
		//Handle name
		if(sec.contains("name")) {
			meta.setDisplayName(StringUtils.makeColors(sec.getString("name")));
			modifiedMetaSoApply = true;
		}
		
		//Handle lore
		if(sec.contains("lore")) {
			List<String> dummy = sec.getStringList("lore");
			List<String> lore = new ArrayList<String>();
			for(String s : dummy) {
				lore.add(StringUtils.makeColors(s));
			}
			meta.setLore(lore);
			modifiedMetaSoApply = true;
		}
		
		//Handle enchantments
		if(sec.contains("enchantments")) {
			List<String> enchantmentStrings = sec.getStringList("enchantments");
			for(String enchantmentString : enchantmentStrings) {
				String enchantmentName = enchantmentString.split(":")[0];
				int enchantmentLevel = Integer.valueOf(enchantmentString.split(":")[1]);
				switch(enchantmentName) {
					case "arrowdamage":
						meta.addEnchant(Enchantment.ARROW_DAMAGE, enchantmentLevel, true);
						break;
					case "arrowfire":
						meta.addEnchant(Enchantment.ARROW_FIRE, enchantmentLevel, true);
						break;
					case "arrowinfinite":
						meta.addEnchant(Enchantment.ARROW_INFINITE, enchantmentLevel, true);
						break;
					case "arrowknockback":
						meta.addEnchant(Enchantment.ARROW_KNOCKBACK, enchantmentLevel, true);
						break;
					case "damage":
						meta.addEnchant(Enchantment.DAMAGE_ALL, enchantmentLevel, true);
						break;
					case "digspeed":
						meta.addEnchant(Enchantment.DIG_SPEED, enchantmentLevel, true);
						break;
					case "durability":
						meta.addEnchant(Enchantment.DURABILITY, enchantmentLevel, true);
						break;
					case "fireaspect":
						meta.addEnchant(Enchantment.FIRE_ASPECT, enchantmentLevel, true);
						break;
					case "knockback":
						meta.addEnchant(Enchantment.KNOCKBACK, enchantmentLevel, true);
						break;
					case "lootbonusblock":
						meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, enchantmentLevel, true);
						break;
					case "lootbonusmob":
						meta.addEnchant(Enchantment.LOOT_BONUS_MOBS, enchantmentLevel, true);
						break;
					case "luck":
						meta.addEnchant(Enchantment.LUCK, enchantmentLevel, true);
						break;
					case "protectionfall":
						meta.addEnchant(Enchantment.PROTECTION_FALL, enchantmentLevel, true);
						break;
					case "protectionfire":
						meta.addEnchant(Enchantment.PROTECTION_FALL, enchantmentLevel, true);
						break;
					case "silktouch":
						meta.addEnchant(Enchantment.SILK_TOUCH, enchantmentLevel, true);
						break;
				}
			}
			modifiedMetaSoApply = true;
		}
		
		//Handle vanilla tags
		if(sec.contains("tags")) {
			for(String tagString : sec.getStringList("tags")) {
				String[] tagStringProcessed = tagString.split(":");
				String tag = tagStringProcessed[0];
				String value = tagStringProcessed[1];
				switch(tag) {
					case "textureskull":
					    SkullMeta skullMeta = (SkullMeta) meta;
				        PlayerProfile pp = Bukkit.createPlayerProfile(UUID.fromString("9c1917c9-95e1-4042-8f9c-f5cc653d266b")); //Random UUID representing heads made from this plugin.
				        PlayerTextures pt = pp.getTextures();
				        try {
							pt.setSkin(new URL("http://textures.minecraft.net/texture/" + value));
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				        pp.setTextures(pt);
				        skullMeta.setOwnerProfile(pp);
					    meta = skullMeta;
						break;
					case "playerskull":
						SkullMeta skullMeta2 = (SkullMeta) meta;
				        skullMeta2.setOwningPlayer(Bukkit.getOfflinePlayer(value));
					    meta = skullMeta2;
						break;
					case "vanilladurability":
						Damageable dam = (Damageable) meta;
						dam.setDamage(Integer.valueOf(value));
						meta = (ItemMeta) dam;
						break;
					case "unbreakable":
						meta.setUnbreakable(Boolean.valueOf(value));
						break;
					case "custommodeldata":
						meta.setCustomModelData(Integer.valueOf(value));
						break;
				}
			}
			modifiedMetaSoApply = true;
		}
		
		//Handle vanilla flags
		if(sec.contains("flags")){
			for(String s : sec.getStringList("flags")){
				meta.addItemFlags(ItemFlag.valueOf(s));
			}
			modifiedMetaSoApply = true;
		}
		
		//Handle vanilla attributes
		if(sec.contains("attributes")){
			for(String s : sec.getStringList("attributes")){
				String[] args = s.split(":");
				if(args.length == 3) {
					meta.addAttributeModifier(Attribute.valueOf(args[0].toUpperCase()), new AttributeModifier("test", Double.valueOf(args[1]), Operation.valueOf(args[2].toUpperCase())));
				}else if(args.length == 4) {
					for(String slot : args[3].split(",")) {
						meta.addAttributeModifier(Attribute.valueOf(args[0].toUpperCase()), new AttributeModifier(UUID.randomUUID(), "test", Double.valueOf(args[1]), Operation.valueOf(args[2].toUpperCase()), EquipmentSlot.valueOf(slot.toUpperCase())));
					}
				}else {
					//User messed up formatting
				}
			}
			modifiedMetaSoApply = true;
		}
		
		if(modifiedMetaSoApply) item.setItemMeta(meta);
		return item;
	}
	
	public static boolean isSimilar(ItemStack item1, ItemStack item2) {
		if(item2.getType() != item1.getType())
			return false;
		if(item2.hasItemMeta() != item1.hasItemMeta())
			return false;
		if(item2.hasItemMeta()) {
			ItemMeta item1Meta = item1.getItemMeta();
			ItemMeta item2Meta = item2.getItemMeta();
			
			if (item2Meta.hasDisplayName() != item1Meta.hasDisplayName())
				return false;
			if(item2Meta.hasDisplayName()) {
				if(!item2Meta.getDisplayName().equals(item1Meta.getDisplayName()))
					return false;
			}
			if (item2Meta.hasLore() != item1Meta.hasLore())
				return false;
			if (item2Meta.hasLore()) {
				for(int i = 0;i < item2Meta.getLore().size();i++) {
					if(!item2Meta.getLore().get(i).equals(item1Meta.getLore().get(i)))
						return false;
				}
			}
			if (item2Meta.hasEnchants() != item1Meta.hasEnchants())
				return false;
			if (item2Meta.hasEnchants()) {
                if (item2Meta.getEnchants().size() != item1Meta.getEnchants().size()) {
                    return false;
                }
                for (Entry<Enchantment, Integer> enchantInfo : item1Meta.getEnchants().entrySet()) {
                    if (item2Meta.getEnchantLevel(enchantInfo.getKey()) != item1Meta.getEnchantLevel(enchantInfo.getKey())) {
                        return false;
                    }
                }
            }
			if (item2Meta.getItemFlags().size() != item1Meta.getItemFlags().size())
				return false;
			for (ItemFlag flag : item2Meta.getItemFlags()) { //We can do this because we already know the itemflag list size is the same
                if (!item1Meta.hasItemFlag(flag)) {
                    return false;
                }
            }
			if((item2Meta instanceof Damageable) != (item1Meta instanceof Damageable))
				return false;
			if(item2Meta instanceof Damageable) {
				Damageable dam1 = (Damageable) item1Meta;
				Damageable dam2 = (Damageable) item2Meta;
				if(dam1.hasDamage() != dam2.hasDamage())
					return false;
				if(dam2.hasDamage()) {
					if(dam2.getDamage() != dam1.getDamage())
						return false;
				}
			}
			if(item2Meta.hasCustomModelData() != item1Meta.hasCustomModelData())
				return false;
			if(item2Meta.hasCustomModelData()) {
				if(item2Meta.getCustomModelData() != item1Meta.getCustomModelData())
					return false;
			}
		}
		return true;
	}
	
}
