package exceptions;

/** Base exception for all rule violations triggered by game actions. */
public abstract class GameActionException extends Exception {

	public GameActionException() {
	}

	public GameActionException(String message) {
		super(message);
	}

}
