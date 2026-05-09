package model.characters;

import java.awt.Point;

import engine.GameConstants;
import model.world.CharacterCell;
import engine.Game;
import exceptions.InvalidTargetException;
import exceptions.NotEnoughActionsException;

/**
 * Autonomous enemy unit. Each zombie attacks one adjacent hero at the end of
 * every player turn. Zombies spawn with 40 HP and 10 attack damage. They
 * respawn automatically on death (a replacement is placed at a random empty
 * cell), so the map always has at least as many zombies as turns that have
 * passed. Curing a zombie with a {@link model.collectibles.Vaccine} removes it
 * and recruits an available hero in its place.
 */
public class Zombie extends Character {

	static int ZOMBIES_COUNT;

	public Zombie() {
		super("Zombie " + ++ZOMBIES_COUNT, 40, 10);
	}

	public void attack() throws NotEnoughActionsException, InvalidTargetException {
		Point p = getLocation();
		for (int i = -1; i <= 1; i++) {
			int cx = p.x + i;
			if (cx >= 0 && cx < GameConstants.GRID_SIZE) {
				for (int j = -1; j <= 1; j++) {
					int cy = p.y + j;
					if (cy >= 0 && cy < GameConstants.GRID_SIZE) {
						if (!(i == 0 && j == 0) && Game.map[cx][cy] instanceof CharacterCell
								&& ((CharacterCell) Game.map[cx][cy]).getCharacter() instanceof Hero) {
							setTarget(((CharacterCell) Game.map[cx][cy]).getCharacter());
							super.attack();
							return;
						}
					}
				}
			}
		}
	}
}
