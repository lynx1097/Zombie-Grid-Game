package dev.lynx.tloupixelverse.model.characters;

import dev.lynx.tloupixelverse.view.GUI;
import dev.lynx.tloupixelverse.engine.Game;
import dev.lynx.tloupixelverse.exceptions.InvalidTargetException;
import dev.lynx.tloupixelverse.exceptions.NotEnoughActionsException;
import dev.lynx.tloupixelverse.util.Point;
import java.io.IOException;
import dev.lynx.tloupixelverse.model.world.CharacterCell;

public abstract class Character {

	private String name;
	private int maxHp;
	private int currentHp;
	private Point location;
	private int attackDmg;
	private Character target;

	public Character(String name, int maxHp, int attackDamage) {
		this.name = name;
		this.maxHp = maxHp;
		this.attackDmg = attackDamage;
		this.currentHp = maxHp;
	}

	public int getCurrentHp() {
		return currentHp;
	}

	public void setCurrentHp(int currentHp) {
		if (currentHp <= 0) {
			this.currentHp = 0;
			try {
				onCharacterDeath();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		} else if (currentHp > maxHp) {
			this.currentHp = maxHp;
		} else
			this.currentHp = currentHp;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public Character getTarget() {
		return target;
	}

	public void setTarget(Character target) {
		this.target = target;
	}

	public String getName() {
		return name;
	}

	public int getMaxHp() {
		return maxHp;
	}

	public int getAttackDmg() {
		return attackDmg;
	}

	public void attack() throws NotEnoughActionsException,
			InvalidTargetException {
		getTarget().setCurrentHp(getTarget().getCurrentHp() - getAttackDmg());
		getTarget().defend(this);
	}

	public void defend(Character c) {
		c.setCurrentHp(c.getCurrentHp() - getAttackDmg() / 2);
	}

	public void onCharacterDeath() throws IOException {
		Point p = this.getLocation();
		
		if (this instanceof Zombie) {
			Game.zombies.remove(this);
			Game.spawnNewZombie();
		} else if (this instanceof Hero) {
			Game.heroes.remove(this);
		}
		Game.map[p.x][p.y] = new CharacterCell(null);
		Game.map[p.x][p.y].setVisible(true);
		GUI.updateMap();
	}

}
