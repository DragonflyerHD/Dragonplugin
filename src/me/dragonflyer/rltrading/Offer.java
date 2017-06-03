package me.dragonflyer.rltrading;

import java.util.ArrayList;

public class Offer {
	String steamname, rlgusername, tradeurl, notes;
	ArrayList<Item> has, wants;
	
	Offer(String steamname, String rlgusername, String tradeurl, String notes, ArrayList<Item> has, ArrayList<Item> wants) {
		this.steamname = steamname;
		this.rlgusername = rlgusername;
		this.tradeurl = tradeurl;
		this.notes = notes;
		this.has = has;
		this.wants = wants;
	}
}
