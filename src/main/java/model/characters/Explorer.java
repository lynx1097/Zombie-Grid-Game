package model.characters;

import engine.Game;
import exceptions.InvalidTargetException;
import exceptions.NoAvailableResourcesException;

/**
 * Reconnaissance hero. Explorers have low HP but a high action-point budget,
 * allowing them to cover large distances in a single turn. Their special
 * action (consuming a supply) instantly reveals the entire 15×15 map,
 * removing all fog-of-war until the next turn reset.
 */
public class Explorer extends Hero {

	public Explorer(String name, int maxHp, int attackDamage, int maxActions) {
		super(name, maxHp, attackDamage, maxActions);
	}
	
	public void useSpecial() throws NoAvailableResourcesException, InvalidTargetException {
		super.useSpecial();
		for(int i = 0; i < Game.map.length; i++) {
			for(int j = 0; j < Game.map[i].length; j++) {
				Game.map[i][j].setVisible(true);
			}
		}
	}

}
