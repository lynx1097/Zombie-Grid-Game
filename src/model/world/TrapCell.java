package model.world;

/**
 * Grid cell that deals random damage to any hero who steps onto it.
 * Trap damage is randomised at spawn: 10, 20, or 30 HP (1d3 × 10).
 * A trap is not consumed after triggering; the hero just takes damage and
 * occupies the cell as a normal {@link CharacterCell}.
 */
public class TrapCell extends Cell {

	private int trapDamage;

	public TrapCell() {
		trapDamage = ((int) (Math.random() * 3 + 1)) * 10;
	}

	public int getTrapDamage() {
		return trapDamage;
	}

}
