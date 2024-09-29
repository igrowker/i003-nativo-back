package com.igrowker.nativo.utils;

import com.igrowker.nativo.exceptions.InvalidDateFormatException;
import com.igrowker.nativo.exceptions.ValidationException;
import com.igrowker.nativo.validations.Validations;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DateFormatter {

    private final Validations validations;

    public List<LocalDateTime> getDateFromString(String date){
        List<LocalDateTime> transactionDate = new ArrayList<>();
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate formattedDate = LocalDate.parse(date, formatter);
            LocalDateTime startDate = formattedDate.atStartOfDay();
            LocalDateTime endDate = formattedDate.plusDays(1).atStartOfDay();
            transactionDate.add(startDate);
            transactionDate.add(endDate);

            return transactionDate;
        } catch (Exception e){
            throw new InvalidDateFormatException("Formato de fecha erroneo. Debe ingresar yyyy-MM-dd");
        }

    }

    public List<LocalDateTime> getDateFromString(String fromDate, String toDate){
        List<LocalDateTime> transactionDate = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime startDate;
        LocalDateTime endDate;

        try {
            LocalDate formattedFromDate = LocalDate.parse(fromDate, formatter);
            startDate = formattedFromDate.atStartOfDay();
            LocalDate formattedToDate = LocalDate.parse(toDate, formatter);
            endDate = formattedToDate.plusDays(1).atStartOfDay();
        } catch (Exception e){
            throw new InvalidDateFormatException("Formato de fecha erroneo. Debe ingresar yyyy-MM-dd");
        }

        if(validations.isSecondDateBefore(startDate, endDate)){
            throw new ValidationException("La fecha final no puede ser menor a la inicial.");
        }
        transactionDate.add(startDate);
        transactionDate.add(endDate);

        return transactionDate;
    }
}
