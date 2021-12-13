package me.TheTealViper.farmcraft.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.md_5.bungee.api.ChatColor;

public class StringUtils {
	
	private final static Pattern HEXPAT = Pattern.compile("&#[a-fA-F0-9]{6}");
	public String makeColors(String s){
		//&x is a new "random color" variable
        s =  ChatColor.translateAlternateColorCodes('&', s).replaceAll("&x", randomColor(new ArrayList<Integer>()));
        
        //Handle custom hex codes (1.16 and up)
        Matcher match = HEXPAT.matcher(s);
        while(match.find()) {
        	String color = s.substring(match.start(), match.end());
        	s = s.replace(color, ChatColor.of(color.replace("&", "")) + "");
        }
        return s;
    }
    
    public String randomColor(List<Integer> blacklistedColors) {
    	//10 = a
    	//11 = b
    	//12 = c
    	//13 = d
    	//14 = e
    	//15 = f
		Random random = new Random();
		int i = 0;
		while(blacklistedColors.contains(i))
			i = random.nextInt(16);
		String cdata = i + "";
		if(i == 10)
			cdata = "a";
		else if(i == 11)
			cdata = "b";
		else if(i == 12)
			cdata = "c";
		else if(i == 13)
			cdata = "d";
		else if(i == 14)
			cdata = "e";
		String color = ChatColor.translateAlternateColorCodes('&', "&" + cdata);
		return color;
	}
    
    public String toLocString(Location loc, boolean detailed, boolean extended, String[] args){
    	String locString = loc.getWorld().getName() + "_";
    	if(detailed)
    		locString += loc.getX() + "_" + loc.getY() + "_" + loc.getZ();
    	else
    		locString += loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    	if(extended){
    		if(detailed)
    			locString += "_" + loc.getYaw() + "_" + loc.getPitch();
    		else
    			locString += "_" + ((int) loc.getYaw()) + "_" + ((int) loc.getPitch());
    	}
    	if(args != null){
	    	for(String s : args)
	    		locString += "_" + s;
    	}
    	return locString;
    }
    
    public Location fromLocString(String locString, boolean extended){
    	String[] s = locString.split("_");
    	if(!extended)
    		return new Location(Bukkit.getWorld(s[0]), Double.valueOf(s[1]), Double.valueOf(s[2]), Double.valueOf(s[3]));
    	else
    		return new Location(Bukkit.getWorld(s[0]), Double.valueOf(s[1]), Double.valueOf(s[2]), Double.valueOf(s[3]), Float.valueOf(s[4]), Float.valueOf(s[5]));
    }
    
}
