package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.user.LoginUserDto;
import com.igrowker.nativo.dtos.user.LoginUserResponse;
import com.igrowker.nativo.dtos.user.RegisterUserDto;
import com.igrowker.nativo.dtos.user.UserDto;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceAlreadyExistsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.UserMapper;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.security.JwtService;
import com.igrowker.nativo.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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

}
