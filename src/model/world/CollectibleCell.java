package model.world;

import model.collectibles.Collectible;

/**
 * Grid cell containing a {@link model.collectibles.Collectible} item
 * ({@link model.collectibles.Vaccine} or {@link model.collectibles.Supply}).
 * When a hero moves onto this cell, {@link model.collectibles.Collectible#pickUp}
 * is called and the cell is replaced with an empty {@link CharacterCell}.
 */
public class CollectibleCell extends Cell {

	private Collectible collectible;

	public CollectibleCell(Collectible collectible) {
		this.collectible = collectible;
	}

	public Collectible getCollectible() {
		return collectible;
	}
	

}
