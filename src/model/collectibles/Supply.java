package model.collectibles;

import model.characters.Hero;
import model.world.CharacterCell;
import engine.Game;
import model.characters.Character;

/**
 * Collectible that fuels a hero's special ability. Picking up a supply adds it
 * to the hero's supply inventory; using one (via {@link model.characters.Hero#useSpecial()})
 * removes it and activates the role-specific ability of the consuming hero.
 */
public class Supply implements Collectible {

	@Override
	public void pickUp(Hero h) {
		h.getSupplyInventory().add(this);
	}

	@Override
	public void use(Hero h) {
		h.getSupplyInventory().remove(this);
	}

}
