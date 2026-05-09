package exceptions;

/** Thrown when a hero tries to use a vaccine or supply with an empty inventory. */
public class NoAvailableResourcesException extends GameActionException {

	public NoAvailableResourcesException() {
	}

	public NoAvailableResourcesException(String message) {
		super(message);
	}

}
