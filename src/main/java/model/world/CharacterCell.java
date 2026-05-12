package dev.lynx.tloupixelverse.model.world;

import dev.lynx.tloupixelverse.model.characters.Character;

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
