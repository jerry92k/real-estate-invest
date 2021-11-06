package kancho.realestate.comparingprices.exception;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import kancho.realestate.comparingprices.domain.dto.response.ExceptionResponse;

@RestControllerAdvice
public class CommonExceptionHandler {

	@ExceptionHandler(DuplicateUserAccountException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity handleDuplicateUserAccountException(HttpServletRequest request, Exception ex) {
		System.out.println(ex.getMessage());
		return new ResponseEntity<>(new ExceptionResponse(ex.getMessage(), ErrorCode.DUPLICATE_USER_ACCOUNT.getCode()),
			getHeader(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(InvalidLoginParameterException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity handleInvalidLoginParameterException(HttpServletRequest request, Exception ex) {
		System.out.println(ex.getMessage());
		return new ResponseEntity<>(new ExceptionResponse(ex.getMessage(), ErrorCode.DUPLICATE_USER_ACCOUNT.getCode()),
			getHeader(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(DuplicateLoginException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity handleDuplicateLoginException(HttpServletRequest request, Exception ex) {
		System.out.println(ex.getMessage());
		return new ResponseEntity<>(new ExceptionResponse(ex.getMessage(), ErrorCode.DUPLICATE_USER_ACCOUNT.getCode()),
			getHeader(), HttpStatus.BAD_REQUEST);
	}

	private HttpHeaders getHeader(){
		HttpHeaders responseHeader=new HttpHeaders();
		responseHeader.set("Content-Type","application/json; charset=utf8");
		return responseHeader;
	}
}
