package me.dragonflyer.rltrading;

import java.util.ArrayList;

public class Offer {
	ArrayList<Item> has, wants;
	String steamname, rlgusername, notes, time, tradeurl;

	Offer(ArrayList<Item> has, ArrayList<Item> wants, String steamname, String rlgusername, String notes, String time, String tradeurl) {
		this.has = has;
		this.wants = wants;
		this.steamname = steamname;
		this.rlgusername = rlgusername;
		this.notes = notes;
		this.time = time;
		this.tradeurl = tradeurl;
	}
}
