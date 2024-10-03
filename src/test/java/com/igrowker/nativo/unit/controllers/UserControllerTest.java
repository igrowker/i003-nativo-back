package com.igrowker.nativo.unit.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igrowker.nativo.controllers.UserController;
import com.igrowker.nativo.services.UserService;
import com.igrowker.nativo.dtos.user.ResponseUpdateMailDto;
import com.igrowker.nativo.dtos.user.ResponseUpdateUserDto;
import com.igrowker.nativo.dtos.user.UpdateMailDto;
import com.igrowker.nativo.dtos.user.UpdateUserDto;
import com.igrowker.nativo.exceptions.InvalidDataException;
import com.igrowker.nativo.exceptions.ResourceNotFoundException;
import com.igrowker.nativo.security.JwtService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

@WebMvcTest(UserController.class)
@WithMockUser
public class UserControllerTest {

    @MockBean
    private UserService userService;
    
    @MockBean
    private JwtService jwtService;
 
    @Autowired
    private MockMvc mockMvc;

    ObjectMapper mapper = new ObjectMapper();

    @Nested
    class UpdateUserTests {
        @Test
        public void updateUserShouldBeOk() throws Exception {
            UpdateUserDto updateUserDto = new UpdateUserDto("randomuserid", "1234567890", "John", "Doe");
            ResponseUpdateUserDto responseUpdateUserDto = new ResponseUpdateUserDto("1234567890", "John", "Doe");

            when(userService.updateUser(updateUserDto)).thenReturn(responseUpdateUserDto);

            mockMvc.perform(post("/api/usuarios/editar-usuario")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(updateUserDto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone", Matchers.is(responseUpdateUserDto.phone())))
                .andExpect(jsonPath("$.name", Matchers.is(responseUpdateUserDto.name())))
                .andExpect(jsonPath("$.surname", Matchers.is(responseUpdateUserDto.surname())));
        }


        @Test
        public void updateUserShouldNotBeOk() throws Exception {
            UpdateUserDto updateUserDto = new UpdateUserDto("randomuserid", "1234567890", "John", "Doe");

            when(userService.updateUser(updateUserDto)).thenThrow(new ResourceNotFoundException("Usuario no encontrado"));

            mockMvc.perform(post("/api/usuarios/editar-usuario")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isNotFound());
        }

        @Nested
        class UpdateMailTests {
            @Test
            public void updateMailShouldBeOk() throws Exception {
                UpdateMailDto updateMailDto = new UpdateMailDto("randomuserid", "jhondoe@doe.com");
                ResponseUpdateMailDto responseUpdateMailDto = new ResponseUpdateMailDto("jhondoe@doe.com");

                when(userService.updateMail(updateMailDto)).thenReturn(responseUpdateMailDto);

                mockMvc.perform(post("/api/usuarios/editar-correo")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateMailDto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", Matchers.is(responseUpdateMailDto.email())));
            }

            @Test
            public void updateMailShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
                UpdateMailDto updateMailDto = new UpdateMailDto("non-existent-id", "newemail@example.com");
            
                when(userService.updateMail(any(UpdateMailDto.class)))
                    .thenThrow(new ResourceNotFoundException("No existe un usuario con ese id"));
            
                mockMvc.perform(post("/api/usuarios/editar-correo")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateMailDto)))
                    .andExpect(status().isNotFound());
            }
            
            @Test
            public void updateMailShouldReturnBadRequestWhenSameEmail() throws Exception {
                UpdateMailDto updateMailDto = new UpdateMailDto("existing-id", "same@email.com");
            
                when(userService.updateMail(any(UpdateMailDto.class)))
                    .thenThrow(new InvalidDataException("El nuevo correo electrónico es igual al actual."));
            
                mockMvc.perform(post("/api/usuarios/editar-correo")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateMailDto)))
                    .andExpect(status().isBadRequest());
            }
            
            @Test
            public void updateMailShouldReturnBadRequestWhenInvalidEmail() throws Exception {
                UpdateMailDto updateMailDto = new UpdateMailDto("existing-id", "invalid-email");
            
                when(userService.updateMail(any(UpdateMailDto.class)))
                    .thenThrow(new InvalidDataException("El formato del correo electrónico no es válido."));
            
                mockMvc.perform(post("/api/usuarios/editar-correo")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateMailDto)))
                    .andExpect(status().isBadRequest());
            }
        }
    }
}