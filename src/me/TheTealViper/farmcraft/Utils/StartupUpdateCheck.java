package me.TheTealViper.farmcraft.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class StartupUpdateCheck {
	

	public StartupUpdateCheck(JavaPlugin plugin, String spigotID) {
		if(!spigotID.equals("-1"))
			updatePlugin(plugin, spigotID);
		
		updateConfig(plugin);
	}
	
	private void updatePlugin(JavaPlugin plugin, String spigotID){
		String installed = plugin.getDescription().getVersion();
		String[] installed_Arr = installed.split("[.]");
		String posted = getSpigotVersion(spigotID);
		String[] posted_Arr = posted.split("[.]");
		if(posted == null || posted.equals(""))
			return;
		boolean update = false;
		//Compare each individual section of the #.#.# version as an int (numbers) if possible, and a string (characters) if not.
		// Can't treat every element as string compare, or else "20" is seen as less than "7" for example because it goes
		// element by element and 2 < 7.
		for(int i = 0;i < posted_Arr.length;i++){
		    if (installed_Arr.length <= i) {
		        update = true;
		        break;
		    }
		    try { //This treats the _.#._ as though it is a numeric string
		        if (Integer.valueOf(installed_Arr[i]) < Integer.valueOf(posted_Arr[i])) {
		            update = true;
		            break;
		        } else if (Integer.valueOf(installed_Arr[i]) > Integer.valueOf(posted_Arr[i]))
		            break;
		    } catch (Exception e) { //This treats the _.#._ as though it is an alphabetical string
		        if (posted_Arr[i].compareToIgnoreCase(installed_Arr[i]) > 0) {
		            update = true;
		            break;
		        } else if (posted_Arr[i].compareToIgnoreCase(installed_Arr[i]) < 0)
		            break;
		    }
		}
		if (update) {
			// Delay it or the message isn't actually sent to console for some reason. Haven't looked into why.
	        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {public void run() {
	        	plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + plugin.getDescription().getName() + " has an update ready [" + installed + " -> " + posted + "]!");
	        	plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "https://www.spigotmc.org/resources/" + spigotID);
	        	
			}}, 40);
	    }
	}
	private void updateConfig(JavaPlugin plugin){
		YamlConfiguration compareTo = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("config.yml")));
		boolean update = false;
		PluginFile pf = new PluginFile(plugin, "config.yml");
		String oldVersion;
		if(!pf.contains("VERSION")) {
			oldVersion = "0";
		} else {
			oldVersion = plugin.getConfig().getString("VERSION");
		}
		String[] oldVersion_Arr = oldVersion.split("[.]");
		String newVersion = compareTo.getString("VERSION"); newVersion = newVersion == null ? "0" : newVersion;
		String[] newVersion_Arr = newVersion.split("[.]");
		for(int i = 0;i < newVersion_Arr.length;i++){
			if(oldVersion_Arr.length <= i || Integer.valueOf(oldVersion_Arr[i]) < Integer.valueOf(newVersion_Arr[i])){
				update = true;
				break;
			}
		}
		if(update){
			File file = new File("plugins/" + plugin.getDescription().getName() + "/config.yml");
			try {
				FileUtils.copyFile(file, new File("plugins/" + plugin.getDescription().getName() + "/configBACKUP_" + oldVersion + ".yml"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(file.exists())
				file.delete();
			plugin.saveDefaultConfig();
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + plugin.getDescription().getName() + " config.yml has been updated [" + oldVersion + " -> " + newVersion + "] and a backup created of old configuration!");
		}
	}
	private String getSpigotVersion(String spigotID) {
        try {
        	HttpClient client = HttpClient.newHttpClient();
        	HttpRequest request = HttpRequest.newBuilder()
        		    .uri(URI.create("https://api.spiget.org/v2/resources/" + spigotID + "/versions/latest"))
        		    .build();
        	HttpResponse<String> response = client.send(request,
        		    HttpResponse.BodyHandlers.ofString());

        	JSONObject object = (JSONObject) JSONValue.parse(response.body());
        	return (String) object.get("name");
        } catch (Exception ex) {
            
        }
        return null;
    }
}
