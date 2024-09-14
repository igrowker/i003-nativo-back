package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.user.*;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceAlreadyExistsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.UserMapper;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.security.EmailService;
import com.igrowker.nativo.security.JwtService;
import com.igrowker.nativo.services.AuthenticationService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Override
    public UserDto signUp(@Valid RegisterUserDto registerUserDto) {
        if (userRepository.findByEmail(registerUserDto.email()).isPresent()) {
            throw new ResourceAlreadyExistsException("Ya hay una cuenta asociada con el email " + registerUserDto.email() + ".");
        }

        if (userRepository.findByDni(registerUserDto.dni()).isPresent()) {
            throw new ResourceAlreadyExistsException("Ya hay una cuenta asociada con el DNI " + registerUserDto.dni() + ".");
        }

        User user = userMapper.registerUsertoUser(registerUserDto);
        user.setPassword(passwordEncoder.encode(registerUserDto.password()));
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        User savedUser = userRepository.save(user);

        return userMapper.userToUserDTO(savedUser);
    }

    @Override
    public LoginUserResponse login(LoginUserDto loginUserDto) {
        User user = userRepository.findByEmail(loginUserDto.email()).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginUserDto.email(),
                            loginUserDto.password()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidUserCredentialsException("Email y/o contraseña inválidos.");
        }

        String jwtToken = jwtService.generateToken(user);

        return new LoginUserResponse(user.getId(), jwtToken, jwtService.getExpirationTime());
    }

    public void verifyUser(VerifyUserDto verifyUserDto) {
        Optional<User> optionalUser = userRepository.findByEmail(verifyUserDto.email());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Código de verificación vencido.");
            }
            if (user.getVerificationCode().equals(verifyUserDto.verificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Código de verificación incorrecto.");
            }
        } else {
            throw new RuntimeException("Usuario no encontrado.");
        }
    }

    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new RuntimeException("La cuenta ya se encuentra verificada.");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Usuario no encontrado.");
        }
    }

    private void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String verificationCode = user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">¡Bienvenido a Nativo!</h2>"
                + "<p style=\"font-size: 16px;\">Por favor ingresa el siguiente código debajo para continuar:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Código de Verificación:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

}
