package com.igrowker.nativo.controllers;

import com.igrowker.nativo.dtos.user.ResponseUserDto;
import com.igrowker.nativo.dtos.user.UpdateUserDto;
import com.igrowker.nativo.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/update")
    public ResponseEntity<ResponseUserDto> updateAccount(@Valid @RequestBody UpdateUserDto updateUserDto) {
        System.out.println(updateUserDto);
        ResponseUserDto responseUserDto = userService.updateAccount(updateUserDto);
        return ResponseEntity.ok(responseUserDto);

    }

    @PostMapping("/assign")
    public ResponseEntity<ResponseUserDto> assignAccountToUser(@RequestBody UpdateUserDto updateUserDto) {

        ResponseUserDto responseUserDto = userService.assignAccountToUser(updateUserDto);
        return ResponseEntity.ok(responseUserDto);
       
    }
}
