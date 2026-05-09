package model.world;

/**
 * Abstract grid cell. Each position in the 15×15 game map is a Cell subtype:
 * {@link CharacterCell} (hero or zombie), {@link CollectibleCell} (item), or
 * {@link TrapCell} (hazard). The {@code isVisible} flag drives fog-of-war:
 * only visible cells are rendered with their full content.
 */
public abstract class Cell {

	private boolean isVisible;

	public Cell() {

	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

}
