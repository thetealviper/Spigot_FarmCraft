package me.TheTealViper.farmcraft.Utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.md_5.bungee.api.ChatColor;

//V1_1

public class StringUtils {
	private final static Pattern HEXPAT = Pattern.compile("&#([A-Fa-f0-9]{6})");
	private final static Pattern LCHGRADIENT_LEFTPAT = Pattern.compile("&>LCH#([A-Fa-f0-9]{6})");
	private final static Pattern LCHGRADIENT_RIGHTPAT = Pattern.compile("&<LCH#([A-Fa-f0-9]{6})");
	private final static Pattern RGBGRADIENT_LEFTPAT = Pattern.compile("&>RGB#([A-Fa-f0-9]{6})");
	private final static Pattern RGBGRADIENT_RIGHTPAT = Pattern.compile("&<RGB#([A-Fa-f0-9]{6})");
	
	public static String makeColors(String s){
        //Handle LCH gradient
        //	Load placeholders into db
        Map<Integer, String> leftDb = new HashMap<Integer, String>();
        Matcher gradLeftMatcher = LCHGRADIENT_LEFTPAT.matcher(s);
        while(gradLeftMatcher.find()) {
        	leftDb.put(gradLeftMatcher.start(), gradLeftMatcher.group());
        }
        Map<Integer, String> rightDb = new HashMap<Integer, String>();
        Matcher gradRightMatcher = LCHGRADIENT_RIGHTPAT.matcher(s);
        while(gradRightMatcher.find()) {
        	if(rightDb.size() >= leftDb.size())
        		continue;
        	rightDb.put(gradRightMatcher.start(), gradRightMatcher.group());
        }
        //	Convert to true indices. Go backwards so replacements don't change where index leads
        int maxDbIndex = (leftDb.size() > rightDb.size() ? leftDb.size() : rightDb.size()) - 1; //Just in case there are more openers or enders, only go as far as both db allow.
        for(int dbKeyIndex = maxDbIndex; dbKeyIndex >= 0; dbKeyIndex--) {
        	if(rightDb.size() < dbKeyIndex+1 || leftDb.size() < dbKeyIndex+1)
        		break;
        	int trueLeftTextIndex = leftDb.keySet().toArray(new Integer[leftDb.keySet().size()])[dbKeyIndex] + 12;
        	int trueRightTextIndex = rightDb.keySet().toArray(new Integer[rightDb.keySet().size()])[dbKeyIndex];
        	String txtToBeColored = ChatColor.stripColor(s.substring(trueLeftTextIndex, trueRightTextIndex));
        	String startingColorString = leftDb.values().toArray(new String[leftDb.keySet().size()])[dbKeyIndex].substring(5);
        	String endingColorString = rightDb.values().toArray(new String[rightDb.keySet().size()])[dbKeyIndex].substring(5);
        	Color startingColorRGB = Color.decode(startingColorString);
        	Color endingColorRGB = Color.decode(endingColorString);
        	double[][] gradientLCH = makeLCHGradient(startingColorRGB, endingColorRGB, txtToBeColored);
        	String txtLCHColored = "";
        	for(int i = 0;i < txtToBeColored.toCharArray().length;i++) {
        		txtLCHColored = txtLCHColored + ChatColor.of(new Color((float)gradientLCH[i][0]/255f, (float)gradientLCH[i][1]/255f, (float)gradientLCH[i][2]/255f)) + txtToBeColored.toCharArray()[i];
        	}
        	s = s.substring(0, leftDb.keySet().toArray(new Integer[leftDb.keySet().size()])[dbKeyIndex]) + txtLCHColored + s.substring(trueRightTextIndex+12);
        }
        
        //Handle RGB gradient
        //	Load placeholders into db
        leftDb = new HashMap<Integer, String>();
        gradLeftMatcher = RGBGRADIENT_LEFTPAT.matcher(s);
        while(gradLeftMatcher.find()) {
        	leftDb.put(gradLeftMatcher.start(), gradLeftMatcher.group());
        }
        rightDb = new HashMap<Integer, String>();
        gradRightMatcher = RGBGRADIENT_RIGHTPAT.matcher(s);
        while(gradRightMatcher.find()) {
        	if(rightDb.size() >= leftDb.size())
        		continue;
        	rightDb.put(gradRightMatcher.start(), gradRightMatcher.group());
        }
        //	Convert to true indices. Go backwards so replacements don't change where index leads
        maxDbIndex = (leftDb.size() > rightDb.size() ? leftDb.size() : rightDb.size()) - 1; //Just in case there are more openers or enders, only go as far as both db allow.
        for(int dbKeyIndex = maxDbIndex; dbKeyIndex >= 0; dbKeyIndex--) {
        	if(rightDb.size() < dbKeyIndex+1 || leftDb.size() < dbKeyIndex+1)
        		break;
        	int trueLeftTextIndex = leftDb.keySet().toArray(new Integer[leftDb.keySet().size()])[dbKeyIndex] + 12;
        	int trueRightTextIndex = rightDb.keySet().toArray(new Integer[rightDb.keySet().size()])[dbKeyIndex];
        	String txtToBeColored = ChatColor.stripColor(s.substring(trueLeftTextIndex, trueRightTextIndex));
        	String startingColorString = leftDb.values().toArray(new String[leftDb.keySet().size()])[dbKeyIndex].substring(5);
        	String endingColorString = rightDb.values().toArray(new String[rightDb.keySet().size()])[dbKeyIndex].substring(5);
        	Color startingColorRGB = Color.decode(startingColorString);
        	Color endingColorRGB = Color.decode(endingColorString);
        	double[][] gradientRGB = makeRGBGradient(startingColorRGB, endingColorRGB, txtToBeColored);
        	String txtRGBColored = "";
        	for(int i = 0;i < txtToBeColored.toCharArray().length;i++) {
        		txtRGBColored = txtRGBColored + ChatColor.of(new Color((float)gradientRGB[i][0]/255f, (float)gradientRGB[i][1]/255f, (float)gradientRGB[i][2]/255f)) + txtToBeColored.toCharArray()[i];
        	}
        	s = s.substring(0, leftDb.keySet().toArray(new Integer[leftDb.keySet().size()])[dbKeyIndex]) + txtRGBColored + s.substring(trueRightTextIndex+12);
        }
        
        //&x is a new "random color" variable
        s = s.replace("&?", randomColor(new ArrayList<Integer>()));
        s =  ChatColor.translateAlternateColorCodes('&', s);
        
        //Handle custom hex codes (1.16 and up)
        Matcher hexMatcher = HEXPAT.matcher(s);
        while(hexMatcher.find()) {
        	String color = hexMatcher.group();
        	s = s.replace(color, ChatColor.of(color.replace("&", "")) + "");
        }
        
        return s;
    }
	
	public static double[][] makeLCHGradient(Color startingColorRGB, Color endingColorRGB, String txtToBeColored) {
		double[] startingColorLCH = ColorSpaceConverter.RGB_to_LCH(startingColorRGB.getRed(), startingColorRGB.getGreen(), startingColorRGB.getBlue());
		double[] endingColorLCH = ColorSpaceConverter.RGB_to_LCH(endingColorRGB.getRed(), endingColorRGB.getGreen(), endingColorRGB.getBlue());
		double[] difference = {endingColorLCH[0]-startingColorLCH[0], endingColorLCH[1]-startingColorLCH[1], 0 /*Do below*/};
		//LCH can wrap circle both ways so check hue is shorter of the two directions
		double noWrapDistance = Math.abs(endingColorLCH[2]-startingColorLCH[2]);
		double wrapDistance = 360d - noWrapDistance;
		if(noWrapDistance < wrapDistance)
			difference[2] = endingColorLCH[2]-startingColorLCH[2];
		else
			difference[2] = endingColorLCH[2]-startingColorLCH[2] > 0 ? -wrapDistance : wrapDistance;
		double[] deltas = {difference[0] / (txtToBeColored.length()-1), difference[1] / (txtToBeColored.length()-1), difference[2] / (txtToBeColored.length()-1)};
		double[][] gradient = new double[txtToBeColored.length()][3];
		for(int i = 0;i < txtToBeColored.length();i++) {
			gradient[i][0] = startingColorLCH[0]+deltas[0]*i;
			gradient[i][1] = startingColorLCH[1]+deltas[1]*i;
			gradient[i][2] = startingColorLCH[2]+deltas[2]*i;
			if(gradient[i][2] > 360d)
				gradient[i][2] -= 360d;
			else if(gradient[i][2] < 0d)
				gradient[i][2] += 360d;
			gradient[i] = ColorSpaceConverter.LCH_to_RGB(gradient[i]);
		}
		return gradient;
	}
	
	private static double bufferDouble;
	public static double[][] makeRGBGradient(Color startingColor, Color endingColor, String txtToBeColored) {
		double[] startingColorRGB = {startingColor.getRed(), startingColor.getGreen(), startingColor.getBlue()};
		double[] endingColorRGB = {endingColor.getRed(), endingColor.getGreen(), endingColor.getBlue()};
		double[] difference = {endingColorRGB[0]-startingColorRGB[0], endingColorRGB[1]-startingColorRGB[1], endingColorRGB[2]-startingColorRGB[2]};
		double[] deltas = {difference[0] / (txtToBeColored.length()-1), difference[1] / (txtToBeColored.length()-1), difference[2] / (txtToBeColored.length()-1)};
		double[][] gradient = new double[txtToBeColored.length()][3];
		for(int i = 0;i < txtToBeColored.length();i++) {
			bufferDouble = startingColorRGB[0]+deltas[0]*i;
			gradient[i][0] = bufferDouble > 255 ? 255 : bufferDouble < 0 ? 0 : bufferDouble;
			bufferDouble = startingColorRGB[1]+deltas[1]*i;
			gradient[i][1] = bufferDouble > 255 ? 255 : bufferDouble < 0 ? 0 : bufferDouble;
			bufferDouble = startingColorRGB[2]+deltas[2]*i;
			gradient[i][2] = bufferDouble > 255 ? 255 : bufferDouble < 0 ? 0 : bufferDouble;
//			String hex = String.format("#%02x%02x%02x", gradient[i][0], gradient[i][1], gradient[i][2]);
//			Bukkit.broadcastMessage(hex);
		}
		return gradient;
	}
    
    public static String randomColor(List<Integer> blacklistedColors) {
    	//10 = a
    	//11 = b
    	//12 = c
    	//13 = d
    	//14 = e
    	//15 = f
		Random random = new Random();
		int i = 0;
		do {
			i = random.nextInt(16);
		} while(blacklistedColors.contains(i));
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
    
    public static String toLocString(Location loc, boolean detailed, boolean extended, String[] args){
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
    
    public static Location fromLocString(String locString, boolean extended){
    	String[] s = locString.split("_");
    	if(!extended)
    		return new Location(Bukkit.getWorld(s[0]), Double.valueOf(s[1]), Double.valueOf(s[2]), Double.valueOf(s[3]));
    	else
    		return new Location(Bukkit.getWorld(s[0]), Double.valueOf(s[1]), Double.valueOf(s[2]), Double.valueOf(s[3]), Float.valueOf(s[4]), Float.valueOf(s[5]));
    }
    
    public static String convertToInvisibleString(String s) {
        String hidden = "";
        for (char c : s.toCharArray()) hidden += ChatColor.COLOR_CHAR+""+c;
        return hidden;
    }
    public static String convertToVisibleString(String s){
        //String converted = ChatColor.stripColor(s);
        String converted = s.replaceAll("�", "");
        return converted;
    }
}
