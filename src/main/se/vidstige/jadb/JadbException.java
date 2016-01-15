package se.vidstige.jadb;

public class JadbException extends Exception {

	private static final long serialVersionUID = -3879283786835654165L;

	public JadbException(String message) {
		super(message);

	}

	public JadbException(String message, Throwable cause) {
		super(message, cause);
	}

}
