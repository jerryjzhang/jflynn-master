package com.tencent.jflynn.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Requested object not found")
public class ObjectNotFoundException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5999790880381465245L;
}
