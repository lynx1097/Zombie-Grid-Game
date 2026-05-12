package dev.lynx.tloupixelverse.model.collectibles;

import dev.lynx.tloupixelverse.model.characters.Hero;
import dev.lynx.tloupixelverse.model.world.CharacterCell;
import dev.lynx.tloupixelverse.engine.Game;
import dev.lynx.tloupixelverse.model.characters.Character;

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
