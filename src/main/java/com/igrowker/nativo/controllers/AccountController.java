package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.account.AddAmountAccountDto;
import com.igrowker.nativo.dtos.account.ResponseOtherAccountDto;
import com.igrowker.nativo.dtos.account.ResponseSelfAccountDto;
import com.igrowker.nativo.dtos.account.ResponseTransactionDto;
import com.igrowker.nativo.dtos.payment.ResponseRecordPayment;
import com.igrowker.nativo.services.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cuenta")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/agregar")
    public ResponseEntity<ResponseSelfAccountDto> addAmount(
            @RequestBody @Valid AddAmountAccountDto addAmountAccountDto){
        ResponseSelfAccountDto result = accountService.addAmount(addAmountAccountDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/consultar-saldo/{id}")
    public ResponseEntity<ResponseSelfAccountDto> readSelfAccount(
            @PathVariable String id){
        ResponseSelfAccountDto result = accountService.readSelfAccount(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/consultar/{id}")
    public ResponseEntity<ResponseOtherAccountDto> readOtherAccount(
            @PathVariable String id){
        ResponseOtherAccountDto result = accountService.readOtherAccount(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("historial/todo")
    public ResponseEntity<List<ResponseTransactionDto>> getAll(){
        List<ResponseTransactionDto> result = accountService.getAll();
        return ResponseEntity.ok(result);
    }

    @GetMapping("historial/estado/{status}")
    public ResponseEntity<List<ResponseTransactionDto>> getAllStatus(@PathVariable String status){
        List<ResponseTransactionDto> result = accountService.getAllStatus(status);
        return ResponseEntity.ok(result);
    }

    @GetMapping("historial/fechas/{fromDate}/{toDate}")
    public ResponseEntity<List<ResponseTransactionDto>> getAllStatus(@PathVariable String fromDate, @PathVariable String toDate){
        List<ResponseTransactionDto> result = accountService.getAllBetweenDates(fromDate, toDate);
        return ResponseEntity.ok(result);
    }



}
