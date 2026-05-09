package model.collectibles;

import model.characters.Hero;
import model.characters.Character;

/**
 * Contract for items that can be picked up and consumed by a {@link Hero}.
 * Implemented by {@link Vaccine} and {@link Supply}.
 */
public interface Collectible {

	/** Adds this item to the hero's inventory. Called when the hero walks onto a {@link model.world.CollectibleCell}. */
	void pickUp(Hero h);

	/** Consumes this item from the hero's inventory and applies its effect. */
	void use(Hero h);

}
