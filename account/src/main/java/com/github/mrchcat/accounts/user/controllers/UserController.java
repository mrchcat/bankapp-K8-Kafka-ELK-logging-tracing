package com.github.mrchcat.accounts.user.controllers;

import com.github.mrchcat.accounts.user.service.UserService;
import com.github.mrchcat.shared.accounts.CreateNewClientDto;
import com.github.mrchcat.shared.accounts.UpdatePasswordRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Аутентификация пользователя
     */
    @GetMapping("/credentials/{username}")
    UserDetails getUserDetails(@PathVariable @NotNull @NotBlank String username) {
        return userService.getUserDetails(username);
    }

    /**
     * Обновление пароля
     */
    @PostMapping("/credentials/{username}")
    UserDetails updateUserPassword(@PathVariable @NotNull @NotBlank String username,
                                   @Valid @RequestBody UpdatePasswordRequestDto passwordUpdateDto) {
        return userService.updateUserDetails(username, passwordUpdateDto.password());
    }

    /**
     * Регистрация нового пользователя
     */
    @PostMapping("/registration")
    UserDetails registerNewClient(@RequestBody @Valid CreateNewClientDto newClientDto) {
        return userService.registerNewClient(newClientDto);
    }

}