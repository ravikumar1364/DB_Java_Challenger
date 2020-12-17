package com.db.awmd.challenge.exception;

public class InvalidTransferException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public InvalidTransferException(String message) {
		super(message);
	}
	
	public InvalidTransferException(String message, Throwable t) {
		super(message, t);
	}
}
