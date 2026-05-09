package exceptions;

/** Thrown when a hero attempts an illegal move (out of bounds or into an occupied cell). */
public class MovementException extends GameActionException {

	public MovementException() {
	}

	public MovementException(String message) {
		super(message);
	}

}
