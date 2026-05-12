package dev.lynx.tloupixelverse.model.collectibles;

import dev.lynx.tloupixelverse.model.characters.Hero;
import dev.lynx.tloupixelverse.model.characters.Character;

public interface Collectible {
	
	void pickUp(Hero h);
	
	void use(Hero h);

}
