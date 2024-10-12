package com.igrowker.nativo.utils;

import com.igrowker.nativo.entities.Account;
import com.igrowker.nativo.entities.Microcredit;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.repositories.AccountRepository;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.security.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class NotificationService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public void sendPaymentNotification(String email, String fullname, BigDecimal amount, String subject, String transaction, String pd) throws MessagingException {
        String htmlMessage = buildHtmlMessage(fullname, amount, subject, transaction, pd);
        emailService.sendVerificationEmail(email, subject, htmlMessage);
    }

    public void sendContributionNotificationToBorrower(Microcredit microcredit, String lenderFullname, BigDecimal amount) throws MessagingException {
        Account borrowerAccount = accountRepository.findById(microcredit.getBorrowerAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrower account not found"));

        User borrowerUser = userRepository.findById(borrowerAccount.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrower user not found"));

        String borrowerEmail = borrowerUser.getEmail();
        String borrowerFullname = borrowerUser.getName() + " " + borrowerUser.getSurname();

        String subject = "Nueva contribución a tu microcrédito: " + microcredit.getId();
        String transaction = "Se ha recibido una contribución de " + lenderFullname + " a tu microcrédito.";
        String pd = "El dinero ya se encuentra acreditado en tu cuenta.";

        String htmlMessage = buildHtmlMessage(borrowerFullname, amount, subject, transaction, pd);
        emailService.sendVerificationEmail(borrowerEmail, subject, htmlMessage);
    }

    private String buildHtmlMessage(String fullname, BigDecimal amount, String subject, String transaction, String pd) {
        return "<html>"
                + "<body style=\"margin: 0; padding: 0; background-color: #f0f0f5; font-family: Arial, sans-serif;\">"
                + "<table width=\"100%\" bgcolor=\"#f5f5f5\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"
                + "<tr>"
                + "<td align=\"center\" style=\"padding: 30px 15px;\">"
                + "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);\">"
                + "<tr>"
                + "<td style=\"background-color: #8EC63F; padding: 20px; border-top-left-radius: 10px; border-top-right-radius: 10px; text-align: center;\">"
                + "<h1 style=\"color: #31342D; font-size: 24px; margin: 0; font-weight: 600;\">" + subject + "</h1>"
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"padding: 30px 40px;\">"
                + "<p style=\"font-size: 18px; color: #31342D; line-height: 1.5;\">Hola " + fullname + ",</p>"
                + "<p style=\"font-size: 16px; color: #31342D; line-height: 1.6;\">" + transaction + ":</p>"
                + "<div style=\"background-color: #f7f7f9; padding: 20px; margin: 20px 0; border-radius: 8px; text-align: center;\">"
                + "<h2 style=\"color: #DE0505; font-size: 28px; font-weight: bold; margin: 0;\">$" + amount.setScale(2) + "</h2>"
                + "</div>"
                + "<p style=\"font-size: 16px; color: #666666; line-height: 1.6;\">" + pd + "</p>"
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"background-color: #019444; padding: 20px 30px; text-align: center; border-bottom-left-radius: 10px; border-bottom-right-radius: 10px;\">"
                + "<p style=\"font-size: 12px; color: #F6FAFD;\">Este correo ha sido generado automáticamente. Por favor, no responda.</p>"
                + "<p style=\"font-size: 16px; color: #F6FAFD; line-height: 1.6;\"> Muchas gracias por confiar en Banco Nativo</p>"
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</body>"
                + "</html>";
    }
}