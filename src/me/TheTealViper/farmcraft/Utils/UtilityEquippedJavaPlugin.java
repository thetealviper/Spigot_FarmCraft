package me.TheTealViper.farmcraft.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class UtilityEquippedJavaPlugin extends JavaPlugin{
	private UtilityEquippedJavaPlugin plugin = null;
	private LoadEnhancedItemstackFromConfig _LoadEnhancedItemstackFromConfig = null;
	private LoadItemstackFromConfig _LoadItemstackFromConfig = null;
//	private StringUtils _StringUtils = null;
	
	public void StartupPlugin(UtilityEquippedJavaPlugin plugin, String spigotID) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents((Listener) plugin, plugin);
		plugin.saveDefaultConfig();
		new StartupUpdateCheck(plugin, spigotID);
		ItemstackUtils.RegisterGlowEnchant(plugin);
		plugin.getLogger().info(plugin.getDescription().getName() + " from TheTealViper initializing!");
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
	
	public void RegisterCommandsFromString(List<String> commands) {
		try {
			final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
		    bukkitCommandMap.setAccessible(true);
		    CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
			List<Command> cmds = new ArrayList<>();
			for (String cmd : commands) {
				cmds.add(new Command(cmd) {public boolean execute(CommandSender arg0, String arg1, String[] arg2) {
					return plugin.onCommand(arg0, this, arg1, arg2);
				}});
			}
		    commandMap.registerAll(getName(), cmds);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void RegisterCommands(List<Command> commands) {
		try {
			final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			bukkitCommandMap.setAccessible(true);
		    CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
		    commandMap.registerAll(getName(), commands);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	public StringUtils getStringUtils() {
//		if(_StringUtils == null)
//			_StringUtils = new StringUtils();
//		return _StringUtils;
//	}
	
}