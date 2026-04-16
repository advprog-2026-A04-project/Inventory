package id.ac.ui.cs.advprog.inventory.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void shouldHandleNotFound() {
        var response = handler.handleNotFound(new ProductNotFoundException(UUID.randomUUID()));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("PRODUCT_NOT_FOUND", response.getBody().code());
    }

    @Test
    void shouldHandleInsufficientStock() {
        var response = handler.handleInsufficientStock(new InsufficientStockException(UUID.randomUUID(), 2, 1));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("INSUFFICIENT_STOCK", response.getBody().code());
    }

    @Test
    void shouldHandleWarConflict() {
        var response = handler.handleWarConflict(new OptimisticLockingFailureException("conflict"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("WAR_CONFLICT", response.getBody().code());
    }

    @Test
    void shouldHandleForbidden() {
        var response = handler.handleForbidden(new AccessDeniedException("no"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("FORBIDDEN", response.getBody().code());
    }

    @Test
    void shouldHandleValidation() throws Exception {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "request");
        result.addError(new FieldError("request", "quantity", "must be greater than 0"));
        Method method = SampleController.class.getDeclaredMethod("sample", String.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, 0),
                result
        );

        var response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().code());
    }

    @Test
    void shouldHandleIllegalArgument() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("bad quantity"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().code());
    }

    @SuppressWarnings("unused")
    private static final class SampleController {
        public void sample(String payload) {
        }
    }
}
