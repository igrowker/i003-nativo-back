package com.igrowker.nativo.services.implementation;

import com.igrowker.nativo.dtos.user.RequestLoginDto;
import com.igrowker.nativo.dtos.user.ResponseLoginDto;
import com.igrowker.nativo.dtos.user.RequestRegisterDto;
import com.igrowker.nativo.dtos.user.ResponseUserDto;
import com.igrowker.nativo.entities.User;
import com.igrowker.nativo.exceptions.InvalidUserCredentialsException;
import com.igrowker.nativo.exceptions.ResourceAlreadyExistsException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.mappers.UserMapper;
import com.igrowker.nativo.repositories.UserRepository;
import com.igrowker.nativo.security.JwtService;
import com.igrowker.nativo.services.AuthenticationService;
import com.igrowker.nativo.services.UserService;
import jakarta.transaction.Transactional;
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

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public ResponseUserDto signUp(@Valid RequestRegisterDto requestRegisterDto) {
        if (userRepository.findByEmail(requestRegisterDto.email()).isPresent()) {
            throw new ResourceAlreadyExistsException("Ya hay una cuenta asociada con el email " + requestRegisterDto.email() + ".");
        }

        if (userRepository.findByDni(requestRegisterDto.dni()).isPresent()) {
            throw new ResourceAlreadyExistsException("Ya hay una cuenta asociada con el DNI " + requestRegisterDto.dni() + ".");
        }

        User user = userMapper.registerUsertoUser(requestRegisterDto);
        user.setPassword(passwordEncoder.encode(requestRegisterDto.password()));
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        User savedUser = userRepository.save(user);
        userService.assignAccountToUser(savedUser.getDni(), savedUser.getId());
        return userMapper.userToUserDTO(savedUser);
    }

    @Override
    @Transactional
    public ResponseLoginDto login(RequestLoginDto requestLoginDto) {
        User user = userRepository.findByEmail(requestLoginDto.email()).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestLoginDto.email(),
                            requestLoginDto.password()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidUserCredentialsException("Email y/o contraseña inválidos.");
        }

        String jwtToken = jwtService.generateToken(user);

        return new ResponseLoginDto(user.getId(), jwtToken, jwtService.getExpirationTime());
    }

}
