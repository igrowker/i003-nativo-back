package com.igrowker.nativo.dtos.account;

import java.math.*;
import com.igrowker.nativo.entities.User;

public record AccountDto(
    BigDecimal amount,
    User user
) 
{}