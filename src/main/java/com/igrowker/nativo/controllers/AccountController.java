package com.igrowker.nativo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.igrowker.nativo.dtos.account.*;

import com.igrowker.nativo.services.AccountService;

import jakarta.persistence.NoResultException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    // @PostMapping("/create-deposit")
    // public ResponseEntity<ResponseAccountDto> createDeposit(@Valid @RequestBody AccountDto accountDto){
    //     System.out.println(accountDto);
    //     return ResponseEntity.ok(accountService.addAmount(accountDto));
    // }

    @PostMapping("/disable-account/{dni}")
    public ResponseEntity<?> disableAccount(@PathVariable Long dni) {
        try {
            accountService.disableAccount(dni);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoResultException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
}
