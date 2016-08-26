package org.wipro.svm.utils;

import java.io.FileInputStream;

import javazoom.jl.player.Player;

public class SimplePlayer {
	public SimplePlayer(String fileName) {
		try {
			FileInputStream fis = new FileInputStream(fileName);
			Player playMP3 = new Player(fis);
			playMP3.play();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
