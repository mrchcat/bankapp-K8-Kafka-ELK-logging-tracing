package com.github.mrchcat.front.controller;

import com.github.mrchcat.front.dto.NewClientRegisterDto;
import com.github.mrchcat.front.service.FrontService;
import com.github.mrchcat.shared.utils.log.TracingLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class RegistrationController {
    private final FrontService frontService;
    private final TracingLogger tracingLogger;

    @GetMapping({"/registration", "/signup"})
    String registerNewClient(Model model) {
        return "signup";
    }

    /**
     * Контроллер для регистрации нового пользователя
     */
    @PostMapping("/registration")
    String registerNewClient(@ModelAttribute @Valid NewClientRegisterDto newClientRegisterDto,
                             BindingResult bindingResult,
                             Model model,
                             Principal principal) {
        tracingLogger.info("Получен запрос от пользователя {} на регистрацию нового клиента {}", principal.getName(), newClientRegisterDto);

        List<String> errors = new ArrayList<>();

        model.addAttribute("errors", errors);
        model.addAttribute("isNewClientRegistered", false);

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .forEach(errors::add);
            tracingLogger.info("Найдены ошибки при регистрации нового клиента пользователем {} Ошибки: {}", principal.getName(), errors);
            return "signup";
        }
        try {
            UserDetails newUserDetails = frontService.registerNewClient(newClientRegisterDto);
            model.addAttribute("isNewClientRegistered", true);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                var headers = ex.getResponseHeaders();
                if (headers != null && !headers.isEmpty()) {
                    var xUniqueHeaders = headers.get("X-not-unique");
                    if (xUniqueHeaders != null && !xUniqueHeaders.isEmpty()) {
                        String notUniqueProperties = xUniqueHeaders.get(0);
                        errors.add("Ошибка, указанные свойства не уникальны: " + notUniqueProperties);
                    }
                }
            }
        } catch (Exception ex) {
            errors.add(ex.getMessage());
        }
        if (!errors.isEmpty()) {
            tracingLogger.info("Найдены ошибки при регистрации нового клиента пользователем {} Ошибки: {}", principal.getName(), errors);
        }
        return "signup";
    }
}
