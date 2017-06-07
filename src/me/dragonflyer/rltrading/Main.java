package me.dragonflyer.rltrading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

public class Main {
	static ArrayList<Offer> offers;
	static Mode currentMode;
	private static UserInterface ui;

	public static void main(String[] args) {
		ui = new UserInterface();
	}

	static ArrayList<HashMap<String, String>> loadIDs() {
		ArrayList<HashMap<String, String>> ids = new ArrayList<HashMap<String, String>>();
		ArrayList<String> rawsource = getUrlSource("https://rocket-league.com/trading");
		ArrayList<Integer> starts = new ArrayList<Integer>();
		for (int i = 0; i < rawsource.size(); i++) {
			if (rawsource.get(i).contains("optgroup")) {
				rawsource.remove(i);
				i--;
			} else if (rawsource.get(i).startsWith("<select data-placeholder=")) {
				starts.add(i + 1);
				if (starts.size() == 4)
					break;
			}
		}
		if (starts.size() != 0) {
			ArrayList<List<String>> idparts = new ArrayList<List<String>>();
			idparts.add(rawsource.subList(starts.get(0), starts.get(1) - 5));
			idparts.add(rawsource.subList(starts.get(1), starts.get(2) - 5));
			idparts.add(rawsource.subList(starts.get(2), starts.get(3) - 7));
			for (List<String> idpart : idparts) {
				HashMap<String, String> map = new HashMap<String, String>();
				for (String line : idpart)
					map.put(line.split(">")[1].replace("</option", ""), line.split("\"")[1]);
				ids.add(map);
			}
		}
		return ids;
	}

	enum Mode {
		HasWants, WantsHas, ExactHas, ExactWants, ExactHasWants, ExactWantsHas;
	}

	static void getOffers(String url, int maxThreads, long timeout, Mode mode) {
		ArrayList<String> rawsource = getUrlSource(url);
		getOffers(url, maxThreads, timeout, mode, rawsource, getPages(rawsource));
	}

	static void getOffers(String url, int maxThreads, long timeout, Mode mode, ArrayList<String> rawsource, int pages) {
		ui.exactmatch.setEnabled(false);
		ui.start.setEnabled(false);
		ui.progress.setVisible(true);
		new ProgressBarUpdater(ui.progress);
		ProgressBarUpdater.setMax(pages);
		offers = getOffersFromSource(rawsource);
		ProgressBarUpdater.next();
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (pages > 1) {
					ExecutorService pool = Executors.newFixedThreadPool(maxThreads);
					for (int page = 2; page <= pages; page++)
						pool.submit(new OffersTask(url + "&p=" + page));
					pool.shutdown();
					try {
						pool.awaitTermination(timeout == 0 ? Long.MAX_VALUE : timeout, TimeUnit.SECONDS);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						ui.updateTable(filter(mode));
						ui.progress.setVisible(false);
						ui.exactmatch.setEnabled(true);
						ui.start.setEnabled(true);
					}
				});
			}
		}).start();
	}

	static ArrayList<Offer> filter(Mode mode) {
		currentMode = mode;
		ArrayList<Offer> filteredOffers = new ArrayList<Offer>(offers);
		Iterator<Offer> i = filteredOffers.iterator();
		switch (mode) {
		case HasWants:
			while (i.hasNext()) {
				Offer offer = i.next();
				if (!contains(offer.has, ui.item))
					i.remove();
			}
			break;
		case WantsHas:
			while (i.hasNext()) {
				Offer offer = i.next();
				if (!contains(offer.wants, ui.item))
					i.remove();
			}
			break;
		case ExactHas:
			while (i.hasNext()) {
				Offer offer = i.next();
				if (offer.wants.size() != 1)
					i.remove();
			}
			break;
		case ExactWants:
			while (i.hasNext()) {
				Offer offer = i.next();
				if (offer.has.size() != 1)
					i.remove();
			}
			break;
		case ExactHasWants:
			while (i.hasNext()) {
				Offer offer = i.next();
				if (offer.has.size() > 1 || offer.wants.size() > 1 || !equals(offer.has.get(0), ui.item))
					i.remove();
			}
			break;
		case ExactWantsHas:
			while (i.hasNext()) {
				Offer offer = i.next();
				if (offer.has.size() > 1 || offer.wants.size() > 1 || !equals(offer.wants.get(0), ui.item))
					i.remove();
			}
		}
		return filteredOffers;
	}

	private static boolean contains(ArrayList<Item> list, Item item) {
		for (Item i : list)
			if (equals(i, item))
				return true;
		return false;
	}

	private static boolean equals(Item item1, Item item2) {
		return item1.name.equals(item2.name) && item1.certification.equals(item2.certification)
				&& item1.paint.equals(item2.paint);
	}

	private static ArrayList<Item> combine(ArrayList<Item> items) {
		ArrayList<Item> newitems = new ArrayList<Item>();
		for (Item item : items)
			if (!contains(newitems, item))
				newitems.add(item);
			else
				for (Item newitem : newitems)
					if (equals(newitem, item))
						newitem.amount += item.amount;
		return newitems;
	}

	private static class OffersTask implements Runnable {
		private String url;

		private OffersTask(String url) {
			this.url = url;
		}

		@Override
		public void run() {
			offers.addAll(getOffersFromSource(getUrlSource(url)));
			ProgressBarUpdater.next();
		}
	}

	static ArrayList<String> getUrlSource(String url) {
		ArrayList<String> source = new ArrayList<String>();
		try {
			URLConnection con = new URL(url).openConnection();
			con.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null)
				if (!line.trim().isEmpty())
					source.add(line.trim());
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return source;
	}

	static int getPages(ArrayList<String> rawsource) {
		for (int i = 0; i < rawsource.size(); i++) {
			String line = rawsource.get(i);
			if (line.contains("<i class=\"fa fa-angle-right\" aria-hidden=\"true\"></i></a>")) {
				if (line.contains("style"))
					return 1;
				else {
					String s = rawsource.get(i - 1);
					if (s.equals("<!-- Go forward btn -->"))
						s = rawsource.get(i - 2);
					return Integer.valueOf(s.split(">")[1].replace("</a", ""));
				}
			}
		}
		return 1;
	}

	private static ArrayList<Offer> getOffersFromSource(ArrayList<String> rawsource) {
		ArrayList<Offer> offers = new ArrayList<Offer>();
		ArrayList<Integer> userstarts = new ArrayList<Integer>();
		int stop = 0;
		for (int i = 0; i < rawsource.size(); i++) {
			String line = rawsource.get(i);
			if (line.startsWith("<span>Steam:"))
				userstarts.add(i);
			else if (line.contains("Contact on steam"))
				stop = i + 1;
		}
		if (userstarts.size() != 0) {
			ArrayList<List<String>> users = new ArrayList<List<String>>();
			for (int i = 0; i < userstarts.size(); i++) {
				int userstart = userstarts.get(i);
				if (i == userstarts.size() - 1)
					users.add(rawsource.subList(userstart, stop));
				else
					users.add(rawsource.subList(userstart, userstarts.get(i + 1)));
			}
			for (List<String> user : users) {
				String tradeurl = "", notes = "";
				int hasstart = 0, wantsstart = 0, wantsend = 0;
				for (int j = 0; j < user.size(); j++) {
					String line = user.get(j);
					if (line.contains("\" href=\"/trade/"))
						tradeurl = line.split("/")[2].split("\"")[0];
					else if (line.contains("Read more"))
						notes = user.get(j + 1).replace("<p>", "").replace("</p>", "");
					else if (line.contains("id=\"rlg-youritems\""))
						hasstart = j + 1;
					else if (line.contains("id=\"rlg-theiritems\""))
						wantsstart = j + 1;
					else if (line.equals("<div style=\"clear: both\"></div>"))
						wantsend = j - 1;
				}
				offers.add(new Offer(user.get(0).split(">")[1].split("<")[0].replace("Steam: ", ""),
						user.get(1).replace("<span>RLG Username: ", "").replace("</span>", ""), tradeurl, notes,
						getItems(user.subList(hasstart, wantsstart - 2)),
						getItems(user.subList(wantsstart, wantsend))));
			}
		}
		return offers;
	}

	private static ArrayList<Item> getItems(List<String> lines) {
		ArrayList<Item> items = new ArrayList<Item>();
		ArrayList<Integer> itemstarts = new ArrayList<Integer>();
		for (int i = 0; i < lines.size(); i++)
			if (lines.get(i).startsWith("<a href"))
				itemstarts.add(i);
		if (itemstarts.size() != 0) {
			ArrayList<List<String>> itemparts = new ArrayList<List<String>>();
			for (int i = 0; i < itemstarts.size(); i++) {
				int itemstart = itemstarts.get(i);
				if (i == itemstarts.size() - 1)
					itemparts.add(lines.subList(itemstart, lines.size()));
				else
					itemparts.add(lines.subList(itemstart, itemstarts.get(i + 1)));
			}
			for (List<String> itempart : itemparts) {
				Item item = new Item();
				boolean amount = false;
				for (int i = 0; i < itempart.size(); i++) {
					String line = itempart.get(i);
					if (line.startsWith("<h2>"))
						item.name = line.replace("<h2>", "").replace("</h2>", "");
					else if (line.startsWith("<div class=\"rlg-trade-display-item-paint\""))
						item.paint = line.split("\"")[5];
					else if (line.startsWith("<span>"))
						item.certification = line.replace("<span>", "").replace("</span>", "").replace("</div>", "")
								.trim();
					else if (line.startsWith("<div class=\"rlg-trade-display-item__amount"))
						amount = true;
					else if (amount) {
						item.amount = Integer.valueOf(line.replace("</div>", "").trim());
						amount = false;
					}
				}
				items.add(item);
			}
		}
		return combine(items);
	}
}