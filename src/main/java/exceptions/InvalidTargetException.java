package exceptions;

/** Thrown when an action targets an invalid or out-of-range character. */
public class InvalidTargetException extends GameActionException {

	public InvalidTargetException() {
	}

	public InvalidTargetException(String message) {
		super(message);
	}

}
