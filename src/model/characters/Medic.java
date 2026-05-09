package model.characters;

import exceptions.InvalidTargetException;
import exceptions.NoAvailableResourcesException;

/**
 * Support hero. Medics have moderate stats but an invaluable special action:
 * consuming a supply fully restores an adjacent ally hero to maximum HP.
 * Keeping Medics alive and supplied is critical for sustaining the team
 * through the later stages of a run.
 */
public class Medic extends Hero {

	public Medic(String name, int maxHp, int attackDamage, int maxActions) {
		super(name, maxHp, attackDamage, maxActions);
	}

	public void useSpecial() throws NoAvailableResourcesException, InvalidTargetException {
		if (getTarget() instanceof Zombie)
			throw new InvalidTargetException("You can only cure fellow heroes.");
		if (!checkDistance())
			throw new InvalidTargetException("You are only able to heal adjacent targets.");
		super.useSpecial();
		getTarget().setCurrentHp(getTarget().getMaxHp());
	}
}
