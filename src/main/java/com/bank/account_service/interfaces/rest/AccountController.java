package com.bank.account_service.interfaces.rest;

import com.bank.account_service.application.command.DebitAccountCommand;
import com.bank.account_service.application.command.OpenAccountCommand;
import com.bank.account_service.application.command.UpdateAccountStatusCommand;
import com.bank.account_service.application.service.AccountService;
import com.bank.account_service.domain.exception.AccountNotFoundException;
import com.bank.account_service.domain.model.AccountId;
import com.bank.account_service.domain.model.enums.AccountStatus;
import com.bank.account_service.interfaces.dto.AccountDetailResponse;
import com.bank.account_service.interfaces.dto.DebitRequest;
import com.bank.account_service.interfaces.dto.OpenAccountRequest;
import com.bank.account_service.interfaces.dto.UpdateStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;


/**
 * REST controller for account lifecycle and balance operations.
 *
 * Spring MVC topics covered in this class:
 *
 *  @RestController        — combines @Controller + @ResponseBody.
 *                           Every method's return value is serialized to JSON by Jackson.
 *                           Use @Controller instead when returning Thymeleaf view names.
 *
 *  @RequestMapping        — base path shared by all methods in this controller.
 *
 *  @PostMapping           — maps HTTP POST. Non-idempotent: two identical POSTs may create
 *                           two resources. Returns 201 Created with a Location header.
 *
 *  @GetMapping            — maps HTTP GET. Idempotent and safe (no side effects).
 *                           Returns 200 OK with the resource body, or 404 if not found.
 *
 *  @PatchMapping          — maps HTTP PATCH. Partial update — only the provided fields change.
 *                           Returns 204 No Content (success, no body needed).
 *
 *  @DeleteMapping         — maps HTTP DELETE. Idempotent: deleting a non-existent resource
 *                           should still return 2xx (or 404). Returns 204 No Content.
 *
 *  @PathVariable          — binds a URI template variable to a method parameter.
 *                           Used for unique identifiers: /accounts/{accountId}.
 *
 *  @RequestParam          — binds a query-string parameter to a method parameter.
 *                           Used for filters, sorting, pagination: ?status=ACTIVE.
 *
 *  @RequestBody           — tells Spring to deserialize the HTTP body into a Java object.
 *                           Jackson (ObjectMapper) handles the JSON → Java conversion.
 *
 *  @Valid                 — triggers Bean Validation on the annotated parameter.
 *                           On failure → MethodArgumentNotValidException → 400 Bad Request.
 *
 *  ResponseEntity<T>      — gives full control over status code, headers, and body.
 */

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Operations for account balance management")
public class AccountController {

    private final AccountService accountService;

    // =========================================================================
    // POST /api/v1/accounts/open — open a new account
    // HTTP method: POST (non-idempotent, creates a new resource)
    // Success: 201 Created + Location header pointing to the new resource
    // =========================================================================

    @PostMapping("/open")
    @Operation(summary = "Open a new account",
        description = "Creates a new bank account. Returns 201 with a Location header.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created"),
        @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<Void> openAccount(
            /*
             * @RequestBody — Jackson deserializes the JSON body into OpenAccountRequest.
             * @Valid       — runs Bean Validation (@NotBlank, @ValidCurrency, etc.).
             *               If any constraint fails, Spring throws MethodArgumentNotValidException
             *               before this method body even executes.
             */
            @Valid @RequestBody OpenAccountRequest request) {

        OpenAccountCommand command = new OpenAccountCommand(
            request.firstName(),
            request.lastName(),
            request.type(),
            request.initialBalance(),
            request.currency()
        );

        AccountId newId = accountService.openAccount(command);

        /*
         * Build the Location header: /api/v1/accounts/{newId}
         * ServletUriComponentsBuilder reads the current request's base URL automatically.
         * Best practice for POST responses that create a resource.
         */
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newId.value())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    // =========================================================================
    // GET /api/v1/accounts/{accountId} — get a single account
    // HTTP method: GET (idempotent, safe)
    // Success: 200 OK + body
    // Not found: 404 (thrown by service, caught by GlobalExceptionHandler)
    // =========================================================================

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account found"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountDetailResponse> getAccount(
            /*
             * @PathVariable — Spring's PathVariableMethodArgumentResolver extracts "accountId"
             * from the URI template and converts the String → UUID automatically.
             * Used for unique resource identifiers, NOT for filters.
             */
            @Parameter(description = "Account UUID") @PathVariable UUID accountId) {

        return accountService.findById(accountId)
                .map(ResponseEntity::ok)                                    // 200 + body
                .orElseThrow(() -> new AccountNotFoundException(accountId)); // let handler → 404
    }

    @PostMapping("/{accountId}/debit")
    @Operation(summary = "Debit an account")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Debit accepted"),
        @ApiResponse(responseCode = "400", description = "Validation or business rule failure"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "409", description = "Duplicate transactionId")
    })
    @PreAuthorize("hasRole('ADMIN') or #accountId.toString() == authentication.name")
    public ResponseEntity<Void> debitAccount(
        @Parameter(description = "Account UUID") @PathVariable UUID accountId,
        @Valid @RequestBody DebitRequest request
    ) {

        var command = new DebitAccountCommand(
            accountId,
            request.amount(),
            request.transactionId()
        );
        accountService.handleDebit(command);

        /*
         * 202 Accepted — the command was accepted and will be processed.
         * More accurate than 200 OK because the event is published to Pulsar asynchronously.
         * The caller should not assume the balance has already changed on the downstream side.
         */
        return ResponseEntity.accepted().build();
    }

    // =========================================================================
    // PATCH /api/v1/accounts/{accountId}/status — update account status
    // HTTP method: PATCH (partial update — only status changes)
    // Success: 204 No Content (operation succeeded, no body needed)
    // =========================================================================

    @PatchMapping("/{accountId}/status")
    @Operation(summary = "Update account status", description =
            "Partially updates the account — changes only the status field. " +
                    "PATCH vs PUT: PATCH sends only the changed fields; PUT replaces the whole resource.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID accountId,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        UpdateAccountStatusCommand command = new UpdateAccountStatusCommand(accountId, request.status());
        accountService.updateAccountStatus(command);

        /*
         * 204 No Content — success, but there is nothing to return.
         * Correct for PATCH / DELETE when the operation is "fire and done".
         */
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // DELETE /api/v1/accounts/{accountId} — close an account
    // HTTP method: DELETE (idempotent)
    // Success: 204 No Content
    // =========================================================================

    @DeleteMapping("/{accountId}")
    @Operation(summary = "Close an account", description =
        "Transitions the account to CLOSED status. Idempotent: calling DELETE " +
        "on an already-closed account returns the same 204.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Account closed"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> closeAccount(@PathVariable UUID accountId) {
        UpdateAccountStatusCommand command = new UpdateAccountStatusCommand(
            accountId,
            AccountStatus.CLOSED
        );
        accountService.updateAccountStatus(command);
        return ResponseEntity.noContent().build(); // 204
    }

}
