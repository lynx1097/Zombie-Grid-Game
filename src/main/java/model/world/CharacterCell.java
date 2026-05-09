package model.world;

import model.characters.Character;

/**
 * Grid cell that may hold a {@link model.characters.Character} (hero or zombie).
 * An empty {@code CharacterCell} (character == null) is the default passable
 * terrain. Movement replaces the source cell with an empty CharacterCell and
 * the destination cell with a new CharacterCell holding the moving character.
 */
public class CharacterCell extends Cell {

	private Character character;
	private boolean isSafe;
	
	public CharacterCell(Character character, boolean isSafe) {
		this.character = character;
		this.isSafe=isSafe;
	}
	
	public CharacterCell(Character character) {
		this.character = character;
	}

	public Character getCharacter() {
		return character;
	}

	public void setCharacter(Character character) {
		this.character = character;
	}

	public boolean isSafe() {
		return isSafe;
	}

	public void setSafe(boolean isSafe) {
		this.isSafe = isSafe;
	}

}
