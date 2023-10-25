package me.TheTealViper.farmcraft;

import org.bukkit.entity.Player;

/*
 * This class is made purely for the sake of easily redirecting functionality for a one-off release
 */

public class XPHandler {
	public static void addXP(Player p, int XP) {
		ExperienceManager man = new ExperienceManager(p);
		man.changeExp(XP);
	}
}
