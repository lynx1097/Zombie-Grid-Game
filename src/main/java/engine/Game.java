package dev.lynx.tloupixelverse.engine;

import dev.lynx.tloupixelverse.util.Point;
// import java.io.BufferedReader;
// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.nio.charset.StandardCharsets;
// import java.io.IOException;
import java.util.ArrayList;

import dev.lynx.tloupixelverse.view.GUI;
import dev.lynx.tloupixelverse.model.characters.Explorer;
import dev.lynx.tloupixelverse.model.characters.Fighter;
import dev.lynx.tloupixelverse.model.characters.Hero;
import dev.lynx.tloupixelverse.model.characters.Medic;
import dev.lynx.tloupixelverse.model.characters.Zombie;
import dev.lynx.tloupixelverse.model.collectibles.Supply;
import dev.lynx.tloupixelverse.model.collectibles.Vaccine;
import dev.lynx.tloupixelverse.model.world.Cell;
import dev.lynx.tloupixelverse.model.world.CharacterCell;
import dev.lynx.tloupixelverse.model.world.CollectibleCell;
import dev.lynx.tloupixelverse.model.world.TrapCell;
import dev.lynx.tloupixelverse.exceptions.InvalidTargetException;
import dev.lynx.tloupixelverse.exceptions.NotEnoughActionsException;

public class Game {

	public static ArrayList<Hero> availableHeroes = new ArrayList<Hero>();
	public static ArrayList<Hero> heroes = new ArrayList<Hero>();
	public static ArrayList<Zombie> zombies = new ArrayList<Zombie>();
	public static Cell[][] map = new Cell[15][15];
	private static void addHero(String name, String role, int maxHp, int actions, int dmg) {
		Hero h;
		if ("EXP".equals(role))       h = new Explorer(name, maxHp, dmg, actions);
		else if ("FIGH".equals(role)) h = new Fighter(name, maxHp, dmg, actions);
		else                          h = new Medic(name, maxHp, dmg, actions);
		availableHeroes.add(h);
	}

	public static void loadHeroes() {
		availableHeroes = new ArrayList<>();
		addHero("Joel Miller",  "FIGH", 140, 5, 30);
		addHero("Ellie Williams", "MED", 110, 6, 15);
		addHero("Johann Grime", "EXP", 80, 6, 20);
		addHero("Riley Abel", "EXP", 90, 5, 25);
		addHero("Tommy Miller", "EXP", 95, 5, 25);
		addHero("Bill Jones", "MED", 100, 7, 10);
		addHero("David Eldredge", "FIGH", 150, 4, 35);
		addHero("Henry Burell", "MED", 105, 6, 15);
	}
	public static void endTurn() throws NotEnoughActionsException, InvalidTargetException {
		for (Zombie zombie : zombies) {
			zombie.attack();
			zombie.setTarget(null);
		}
		spawnNewZombie();
		for (int i = 0; i < map.length; i++)
			for (int j = 0; j < map[i].length; j++)
				map[i][j].setVisible(false);
		for (Hero hero : heroes) {
			hero.setActionsAvailable(hero.getMaxActions());
			hero.setTarget(null);
			hero.setSpecialAction(false);
			adjustVisibility(hero);
		}
		GUI.updateMap();
	}

	public static void adjustVisibility(Hero h) {
		Point p = h.getLocation();
		for (int i = -1; i <= 1; i++) {
			int cx = p.x + i;
			if (cx >= 0 && cx <= 14) {
				for (int j = -1; j <= 1; j++) {
					int cy = p.y + j;
					if (cy >= 0 && cy <= 14) {
						if (cy >= 0 && cy <= map.length - 1) {
							map[cx][cy].setVisible(true);
						}
					}
				}
			}
		}
	}

	public static void spawnNewZombie() {
		Zombie z = new Zombie();
		zombies.add(z);
		int x, y;
		do {
			x = ((int) (Math.random() * map.length));
			y = ((int) (Math.random() * map[x].length));
		} while ((map[x][y] instanceof CharacterCell && ((CharacterCell) map[x][y]).getCharacter() != null)
				|| (map[x][y] instanceof CollectibleCell) || (map[x][y] instanceof TrapCell));
		z.setLocation(new Point(x, y));
		map[x][y] = new CharacterCell(z);
	}

	public static boolean checkWin() {
		int remainingVaccines = 0;
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				if (map[i][j] instanceof CollectibleCell
						&& ((CollectibleCell) map[i][j]).getCollectible() instanceof Vaccine)
					remainingVaccines++;
			}
		}
		for (Hero hero : heroes) {
			remainingVaccines += hero.getVaccineInventory().size();
		}
		return heroes.size() >= 5 && remainingVaccines == 0;
	}

	public static boolean checkGameOver() {
		if (heroes.size() > 0) {
			for (int i = 0; i < map.length; i++) {
				for (int j = 0; j < map[i].length; j++) {
					if (map[i][j] instanceof CollectibleCell
							&& ((CollectibleCell) map[i][j]).getCollectible() instanceof Vaccine)
						return false;
				}
			}
			for (Hero hero : heroes) {
				if (hero.getVaccineInventory().size() > 0)
					return false;
			}
		}
		return true;
	}

	public static void startGame(Hero h) {
		heroes.add(h);
		availableHeroes.remove(h);
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				map[i][j] = new CharacterCell(null);
			}
		}

		((CharacterCell) map[0][0]).setCharacter(h);
		h.setLocation(new Point(0, 0));

		spawnCollectibles();
		for (int i = 0; i < 10; i++) {
			spawnNewZombie();
		}
		spawnTraps();
		adjustVisibility(h);
	}

	public static void spawnCollectibles() {
		for (int i = 0; i < 5; i++) {
			Vaccine v = new Vaccine();
			int x, y;
			do {
				x = ((int) (Math.random() * map.length));
				y = ((int) (Math.random() * map[x].length));
			} while ((map[x][y] instanceof CharacterCell && ((CharacterCell) map[x][y]).getCharacter() != null)
					|| (map[x][y] instanceof CollectibleCell) || (map[x][y] instanceof TrapCell));
			map[x][y] = new CollectibleCell(v);
		}
		for (int i = 0; i < 5; i++) {
			Supply v = new Supply();
			int x, y;
			do {
				x = ((int) (Math.random() * map.length));
				y = ((int) (Math.random() * map[x].length));
			} while ((map[x][y] instanceof CharacterCell && ((CharacterCell) map[x][y]).getCharacter() != null)
					|| (map[x][y] instanceof CollectibleCell) || (map[x][y] instanceof TrapCell));
			map[x][y] = new CollectibleCell(v);
		}
	}

	public static void spawnTraps() {
		for (int i = 0; i < 5; i++) {
			int x, y;
			do {
				x = ((int) (Math.random() * map.length));
				y = ((int) (Math.random() * map[x].length));
			} while ((map[x][y] instanceof CharacterCell && ((CharacterCell) map[x][y]).getCharacter() != null)
					|| (map[x][y] instanceof CollectibleCell) || (map[x][y] instanceof TrapCell));
			map[x][y] = new TrapCell();
		}
	}
}
