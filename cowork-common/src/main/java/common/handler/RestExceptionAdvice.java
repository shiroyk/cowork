package common.handler;

import common.constants.ErrorMessage;
import common.exception.ApiException;
import common.utils.ServletUtilsKt;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Optional;

/**
 * Handle the REST exception
 */
@RestControllerAdvice
public class RestExceptionAdvice {
    private static final Logger logger = LoggerFactory.getLogger(RestExceptionAdvice.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorMessage> apiExceptionHandler(ApiException e) {
        return new ResponseEntity<>(new ErrorMessage(e.getMessage()), e.getCode());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage bindExceptionHandler(BindException e) {
        return new ErrorMessage(e.getAllErrors().stream().findFirst()
                .map(ObjectError::getDefaultMessage)
                .orElse("invalid"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage notValidExceptionHandler(MethodArgumentNotValidException e) {
        return new ErrorMessage(Optional.ofNullable(e.getBindingResult().getFieldError())
                .map(err -> err.isBindingFailure() ? String.format("invalid field [%s] type", err.getField()) : err.getDefaultMessage())
                .orElse("invalid argument"));
    }

    @ExceptionHandler(value = NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage exception(NoHandlerFoundException e) {
        return new ErrorMessage(String.format("Not found path %s", StringUtils.delete(e.getRequestURL(), "/api")));
    }

    @ExceptionHandler({RuntimeException.class, Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage runtimeExceptionHandler(Exception e) {
        HttpServletRequest req = ServletUtilsKt.getRequest();
        if (req != null) {
            logger.error(String.format("request_id %s user_id %s URI %s method %s",
                    ServletUtilsKt.getRequestId(), ServletUtilsKt.getUserId(), req.getMethod(), req.getRequestURI()), e);
        } else {
            logger.error(String.format("request_id %s user_id %s ", ServletUtilsKt.getRequestId(), ServletUtilsKt.getUserId()), e);
        }
        return new ErrorMessage(e.getMessage());
    }

}
