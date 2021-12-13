package me.TheTealViper.farmcraft.Utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UtilityEquippedJavaPlugin extends JavaPlugin{
	private UtilityEquippedJavaPlugin plugin = null;
	private LoadEnhancedItemstackFromConfig _LoadEnhancedItemstackFromConfig = null;
	private LoadItemstackFromConfig _LoadItemstackFromConfig = null;
	private StringUtils _StringUtils = null;
	
	public void StartupPlugin(UtilityEquippedJavaPlugin plugin, String spigotID) {
		this.plugin = plugin;
		new StartupUpdateCheck(plugin, spigotID);
	}
	
	public LoadEnhancedItemstackFromConfig getLoadEnhancedItemstackFromConfig() {
		if(_LoadEnhancedItemstackFromConfig == null) {
			_LoadEnhancedItemstackFromConfig = new LoadEnhancedItemstackFromConfig(plugin);
			Bukkit.getPluginManager().registerEvents(_LoadEnhancedItemstackFromConfig, plugin);
		}
		return _LoadEnhancedItemstackFromConfig;
	}
	
	public LoadItemstackFromConfig getLoadItemstackFromConfig() {
		if(_LoadItemstackFromConfig == null)
			_LoadItemstackFromConfig = new LoadItemstackFromConfig(this);
		return _LoadItemstackFromConfig;
	}
	
	public void WipeItemstackFromConfigCache() {
		_LoadItemstackFromConfig = new LoadItemstackFromConfig(this);
		_LoadEnhancedItemstackFromConfig = new LoadEnhancedItemstackFromConfig(plugin);
	}
	
	public StringUtils getStringUtils() {
		if(_StringUtils == null)
			_StringUtils = new StringUtils();
		return _StringUtils;
	}
	
}