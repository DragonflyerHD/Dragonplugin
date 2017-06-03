package me.dragonflyer.rltrading;

import javax.swing.JProgressBar;

public class ProgressBarUpdater extends Thread {
	public static int value = 0;
	public static double max = 0.0D, calc;
	private JProgressBar progressBar;

	public ProgressBarUpdater(JProgressBar progressBar) {
		this.progressBar = progressBar;
		progressBar.setValue(0);
		calc = 0.0D;
		start();
	}

	public static void setMax(int m) {
		max = (double) m;
	}

	public static void next() {
		++calc;
		value = (int) Math.round(100.0D / max * calc);
	}

	public void run() {
		while (true) {
			if (value >= 100) {
				progressBar.setValue(100);
				return;
			}
			progressBar.setValue(value);
			try {
				synchronized (this) {
					wait(10);
				}
			} catch (InterruptedException e) {
			}
		}
	}
}
