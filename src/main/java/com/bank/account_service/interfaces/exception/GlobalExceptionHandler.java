package com.bank.account_service.interfaces.exception;

import com.bank.account_service.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.net.URI;
import java.util.List;

/**
 * Global exception handler for all controllers.
 *
 * Spring MVC interview topics covered:
 *
 *  @RestControllerAdvice — applies @ExceptionHandler methods globally to ALL controllers.
 *    Internally combines @ControllerAdvice + @ResponseBody.
 *    @ControllerAdvice alone is used when returning views (Thymeleaf error pages).
 *    @RestControllerAdvice is used for REST APIs that return JSON.
 *
 *  Flow when an exception escapes a controller method:
 *    Exception thrown
 *      → HandlerExceptionResolverComposite
 *        → ExceptionHandlerExceptionResolver
 *          → finds matching @ExceptionHandler by exception type (most specific wins)
 *            → executes handler method
 *              → HttpMessageConverter serializes ProblemDetail → JSON response
 *
 *  ProblemDetail (RFC 9457 / RFC 7807) — Spring 6+ standard error response format.
 *    Fields: type (URI), title, status, detail, instance.
 *    Can be extended with setProperty() for extra fields (e.g. validation errors list).
 *
 *  Handler resolution — Spring picks the most specific exception type.
 *    InsufficientFundsException handler wins over RuntimeException handler
 *    when an InsufficientFundsException is thrown, even though it's a RuntimeException.
 *
 *  @ExceptionHandler(local) vs @RestControllerAdvice(global):
 *    @ExceptionHandler inside a controller class = handles exceptions ONLY from that controller.
 *    @RestControllerAdvice = handles exceptions from ALL controllers in the application.
 */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // 400 Bad Request — client sent invalid data
    // -------------------------------------------------------------------------

    /**
     * Handles Bean Validation failures triggered by @Valid on @RequestBody.
     *
     * This fires when Jackson successfully deserializes the body BUT constraint
     * annotations (@NotNull, @NotBlank, @ValidCurrency, etc.) fail.
     *
     * MethodArgumentNotValidException contains a BindingResult with all field errors.
     * We extract them and add as a custom "errors" property on ProblemDetail.
     *
     * MISSING in original code — validation errors fell through to the 500 handler.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                // "firstName: First name is required"
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setDetail("One or more fields failed validation");
        pd.setType(URI.create("https://api.bank.com/errors/validation"));

        // ProblemDetail.setProperty() extends the JSON with extra fields — valid per RFC 9457.
        pd.setProperty("errors", errors);

        return pd; // 400
    }

    /**
     * Handles type conversion failures in @RequestParam and @PathVariable.
     *
     * Example: GET /accounts/search?status=INVALID_VALUE
     * Spring tries to convert "INVALID_VALUE" → AccountStatus enum → fails here.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = String.format("Parameter '%s' has invalid value '%s'", ex.getName(), ex.getValue());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail); // 400
    }

    /**
     * Handles insufficient funds — a domain business rule violation.
     * The account exists and is active, but the balance is too low.
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
        log.info("Insufficient funds: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(URI.create("https://api.bank.com/errors/insufficient-funds"));
        return pd; // 400
    }

    /**
     * Handles currency mismatch — client sent amount in the wrong currency.
     */
    @ExceptionHandler(CurrencyMismatchException.class)
    public ProblemDetail handleCurrencyMismatch(CurrencyMismatchException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage()); // 400
    }

    // -------------------------------------------------------------------------
    // 404 Not Found — the requested resource does not exist
    // -------------------------------------------------------------------------

    /**
     * Handles missing accounts.
     *
     * MISSING in original code — the service threw a raw RuntimeException("Account not found")
     * which fell through to the 500 handler below. This was wrong: a missing resource is a
     * client error (404), not a server error (500).
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFound(AccountNotFoundException ex) {
        log.info("Account not found: {}", ex.getAccountId());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("https://api.bank.com/errors/account-not-found"));
        pd.setProperty("accountId", ex.getAccountId());
        return pd; // 404
    }

    // -------------------------------------------------------------------------
    // 409 Conflict — client request conflicts with server state
    // -------------------------------------------------------------------------

    /**
     * Handles duplicate transaction IDs (idempotency violation).
     * The transactionId was already processed — returning the same 409 on retry
     * signals to the caller that the operation was already applied.
     */
    @ExceptionHandler(DuplicateTransactionException.class)
    public ProblemDetail handleDuplicateTransaction(DuplicateTransactionException ex) {
        log.warn("Duplicate transaction: {}", ex.getTransactionId());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setProperty("transactionId", ex.getTransactionId());
        return pd; // 409
    }

    // -------------------------------------------------------------------------
    // 422 Unprocessable Entity — request is valid but cannot be processed
    //     given the current state of the resource
    // -------------------------------------------------------------------------

    /**
     * Handles operations on inactive/blocked/closed accounts.
     *
     * MISSING in original code — fell through to 500.
     * 422 is semantically correct: the request itself is valid (correct body, valid account ID),
     * but the business rule says we cannot debit a non-active account.
     */
    @ExceptionHandler(AccountNotActiveException.class)
    public ProblemDetail handleAccountNotActive(AccountNotActiveException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage()); // 422
    }

    // -------------------------------------------------------------------------
    // 500 Internal Server Error — catch-all for unexpected exceptions
    // -------------------------------------------------------------------------

    /**
     * Catch-all for anything not handled above.
     *
     * Important: log the full stack trace here so you can debug. Never expose
     * internal details (stack traces, DB errors) in the response body — security risk.
     *
     * Note: this catches RuntimeException, NOT Exception. Checked exceptions that
     * bubble up to the controller are typically a design smell in Spring apps.
     */
    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleUnexpected(RuntimeException ex) {
        log.error("Unexpected error", ex); // full stack trace in logs
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."); // 500
        // Never put ex.getMessage() here — it may leak internal implementation details.
    }
}
