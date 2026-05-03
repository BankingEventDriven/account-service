package com.bank.account_service.interfaces.rest;

import com.bank.account_service.application.service.AccountService;
import com.bank.account_service.interfaces.dto.AccountResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts/search")
@RequiredArgsConstructor
@Tag(name = "Search Accounts", description = "Operations for searching accounts")
public class SearchController {

    private final AccountService accountService;

    @GetMapping
    public Page<AccountResponse> getAll(Pageable pageable) {
        return accountService.findAll(pageable);
    }
}
