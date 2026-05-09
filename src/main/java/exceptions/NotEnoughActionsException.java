package exceptions;

/** Thrown when a hero attempts an action without sufficient action points. */
public class NotEnoughActionsException extends GameActionException {

	public NotEnoughActionsException() {
	}

	public NotEnoughActionsException(String message) {
		super(message);
	}

}
