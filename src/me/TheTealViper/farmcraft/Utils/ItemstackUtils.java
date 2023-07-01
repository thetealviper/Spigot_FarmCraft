package me.TheTealViper.farmcraft.Utils;

import java.lang.reflect.Field;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemstackUtils {
	@SuppressWarnings("unused")
	private static UtilityEquippedJavaPlugin plugin;
	private static boolean isRegistered = false;
	private static Glow glow = null;
	
	public static void RegisterGlowEnchant(UtilityEquippedJavaPlugin plugin) {
		if (isRegistered)
			return;
		
		ItemstackUtils.plugin = plugin;
		try {
			Field f = Enchantment.class.getDeclaredField("acceptingNew");
			f.setAccessible(true);
			f.set(null, true);
			NamespacedKey key = new NamespacedKey(plugin, "glow");
			glow = new Glow(key);
//			Enchantment.registerEnchantment(glow);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		isRegistered = true;
	}
	
	private static class Glow extends Enchantment{
		public Glow(NamespacedKey key) {
			super(key);
		}
		@Override
		public boolean canEnchantItem(ItemStack arg0) {
			return true;
		}
		@Override
		public boolean conflictsWith(Enchantment arg0) {
			return false;
		}
		@Override
		public EnchantmentTarget getItemTarget() {
			return null;
		}
		@Override
		public int getMaxLevel() {
			return 10;
		}
		@Override
		public String getName() {
			return "glow";
		}
		@Override
		public int getStartLevel() {
			return 0;
		}
		@Override
		public boolean isCursed() {
			return false;
		}
		@Override
		public boolean isTreasure() {
			return false;
		}
		
	}
	public static void addEnchantmentGlow(ItemStack item) {
		addEnchantmentGlow(item, true);
	}
	public static void addEnchantmentGlow(ItemStack item, boolean doHideEnchantmentsFlag) {
		item.addEnchantment(glow, 1);
		if (doHideEnchantmentsFlag) {
			ItemMeta meta = item.getItemMeta();
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			item.setItemMeta(meta);
		}
	}
	public static void addEnchantmentGlow(ItemMeta meta) {
		addEnchantmentGlow(meta, true);
	}
	public static void addEnchantmentGlow(ItemMeta meta, boolean doHideEnchantmentsFlag) {
		meta.addEnchant(glow, 1, true);
		if (doHideEnchantmentsFlag) {
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
	}
	
}
