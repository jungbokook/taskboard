package com.taskboard.backend.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	private static final String SERVER_ERROR_MESSAGE = "서버 오류가 발생했습니다";

	// Spring MVC 표준 예외와 ResponseStatusException은 모두 여기를 거치므로 본문을 ApiError로 바꾼다.
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatusCode statusCode, WebRequest request) {
		if (statusCode.is5xxServerError()) {
			log.error("서버 오류 응답", ex);
		}
		return super.handleExceptionInternal(ex, new ApiError(messageFor(ex, statusCode)), headers, statusCode,
				request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
		log.error("처리하지 못한 예외", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError(SERVER_ERROR_MESSAGE));
	}

	private String messageFor(Exception ex, HttpStatusCode statusCode) {
		if (statusCode.is5xxServerError()) {
			return SERVER_ERROR_MESSAGE;
		}
		if (ex instanceof ResponseStatusException rse && rse.getReason() != null) {
			return rse.getReason();
		}
		if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
			return mismatch.getName() + " 값이 올바르지 않습니다";
		}
		if (ex instanceof HttpMessageNotReadableException) {
			return "요청 본문 형식이 올바르지 않습니다";
		}
		if (statusCode.value() == HttpStatus.NOT_FOUND.value()) {
			return "요청한 경로를 찾을 수 없습니다";
		}
		return "요청을 처리할 수 없습니다";
	}

}
