package model.characters;

/**
 * High-damage melee hero. Fighters have the highest HP and attack damage
 * in the roster. Their special action (consuming a supply) deals a full
 * attack strike without spending an action point, making them efficient
 * when supplies are available.
 */
public class Fighter extends Hero {

	public Fighter(String name, int maxHp, int attackDamage, int maxActions) {
		super(name, maxHp, attackDamage, maxActions);
	}


}
