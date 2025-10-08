package com.github.mrchcat.front.controller;

import com.github.mrchcat.front.config.ServiceUrl;
import com.github.mrchcat.front.dto.FrontAccountDto;
import com.github.mrchcat.front.dto.FrontBankUserDto;
import com.github.mrchcat.front.dto.FrontCashTransactionDto;
import com.github.mrchcat.front.dto.FrontEditUserAccountDto;
import com.github.mrchcat.front.dto.NonCashTransfer;
import com.github.mrchcat.front.dto.PasswordUpdateDto;
import com.github.mrchcat.front.service.FrontService;
import com.github.mrchcat.shared.enums.CashAction;
import com.github.mrchcat.shared.enums.UserRole;
import com.github.mrchcat.shared.utils.log.TracingLogger;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class MainController {
    private final FrontService frontService;
    private final String FRONT_GET_FRONT_RATES = "/front/rates";
    private final String ratesLink;
    private final MeterRegistry meterRegistry;
    private final TracingLogger tracingLogger;

    public MainController(FrontService frontService,
                          ServiceUrl serviceUrl,
                          MeterRegistry meterRegistry,
                          TracingLogger tracingLogger) {
        this.frontService = frontService;
        this.ratesLink = "http://" + serviceUrl.getFront() + FRONT_GET_FRONT_RATES;
        this.meterRegistry = meterRegistry;
        this.tracingLogger = tracingLogger;
    }

    /**
     * После авторизации загружаются разные страницы в зависимости от роли
     */
    @GetMapping("/defaultAfterLogin")
    String getDefaultUrlAfter(Authentication authentication, Principal principal) {
        tracingLogger.info("Авторизация пользователя {}", principal.getName());
        var authorities = authentication.getAuthorities();
        for (UserRole role : UserRole.values()) {
            if (authorities.contains(new SimpleGrantedAuthority(role.roleName))) {
                return "redirect:" + role.urlAfterSuccessLogin;
            }
        }
        return "redirect:/main";
    }

    /**
     * Основная страница
     */
    @GetMapping(path = {"/main", "/"})
    String getMain(Model model, Principal principal) throws AuthException, ServiceUnavailableException {
        if (principal == null) {
            return "unavailable";
        }
        String username = principal.getName();
        model.addAttribute("login", username);

        FrontBankUserDto clientDetailsAndAccounts = frontService.getClientDetailsAndAccounts(username);
        model.addAttribute("fullName", clientDetailsAndAccounts.fullName());
        model.addAttribute("birthDate", clientDetailsAndAccounts.birthDay());
        model.addAttribute("email", clientDetailsAndAccounts.email());
        model.addAttribute("accounts", clientDetailsAndAccounts.accounts());

        var clientsWithAccounts = frontService.getAllClientsWithActiveAccounts();
        model.addAttribute("clientsWithAccounts", clientsWithAccounts);
        model.addAttribute("ratesLink", ratesLink);
        return "main";
    }

    /**
     * Контроллер обновления пароля
     */
    @PostMapping("/user/{username}/editPassword")
    RedirectView editClientPassword(@PathVariable @NotNull @NotBlank String username,
                                    @ModelAttribute @Valid PasswordUpdateDto passwordDto,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Principal principal) {
        tracingLogger.info("Запрос на редактирование пароля от пользователя {}. Новый пароль - {}",principal.getName(), passwordDto.password());
        RedirectView redirectView = new RedirectView();
        redirectView.setContextRelative(true);
        redirectView.setUrl("/main");
        List<String> passwordErrors = new ArrayList<>();
        redirectAttributes.addFlashAttribute("passwordErrors", passwordErrors);
        redirectAttributes.addFlashAttribute("isPasswordUpdated", false);
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .forEach(passwordErrors::add);
            tracingLogger.info("Найдены ошибки при вводе пароля от пользователя {} Ошибки: {}", principal.getName(), passwordErrors);
            return redirectView;
        }
        try {
            UserDetails newUserDetails = frontService.editClientPassword(username, passwordDto.password());
            redirectAttributes.addFlashAttribute("isPasswordUpdated", true);
        } catch (RuntimeException ex) {
            passwordErrors.add("сервис не доступен");
        } catch (Exception ex) {
            passwordErrors.add(ex.getMessage());
        }
        return redirectView;
    }

    /**
     * Контроллер обновления личных данных и данных об аккаунтах
     */
    @PostMapping("/user/{username}/editUserAccounts")
    RedirectView editUserAccounts(@PathVariable @NotNull @NotBlank String username,
                                  @ModelAttribute @Valid FrontEditUserAccountDto frontEditUserAccountDto,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal
    ) {
        tracingLogger.info("Запрос на редактирование личных данных от пользователя {}. Новые данные - {}", principal.getName(), frontEditUserAccountDto);
        RedirectView redirectView = new RedirectView();
        redirectView.setContextRelative(true);
        redirectView.setUrl("/main");
        List<String> userAccountsErrors = new ArrayList<>();
        redirectAttributes.addFlashAttribute("userAccountsErrors", userAccountsErrors);
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .forEach(userAccountsErrors::add);
        }
        validateCheckBoxes(username, frontEditUserAccountDto, userAccountsErrors);
        if (!userAccountsErrors.isEmpty()) {
            tracingLogger.info("Найдены ошибки при редактировании данных пользователя {} Ошибки: {}", principal.getName(), userAccountsErrors);
            return redirectView;
        }
        try {
            frontService.editUserAccount(username, frontEditUserAccountDto);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                var headers = ex.getResponseHeaders();
                if (headers != null && !headers.isEmpty()) {
                    var xUniqueHeaders = headers.get("X-not-unique");
                    if (xUniqueHeaders != null && !xUniqueHeaders.isEmpty()) {
                        String notUniqueProperties = xUniqueHeaders.get(0);
                        userAccountsErrors.add("Ошибка, указанные свойства не уникальны: " + notUniqueProperties);
                    }
                }
            }
        } catch (Exception ex) {
            userAccountsErrors.add(ex.getMessage());
        }
        return redirectView;
    }

    private void validateCheckBoxes(String username, FrontEditUserAccountDto frontEditUserAccountDto, List<String> userAccountsErrors) {
        List<String> activeAccountsFromFront = (frontEditUserAccountDto.account() == null) ? Collections.emptyList() : frontEditUserAccountDto.account();
        try {
            List<FrontAccountDto> existingAccounts = frontService.getClientDetailsAndAccounts(username).accounts();
            List<String> notEmptyAccounts = frontService.getClientDetailsAndAccounts(username)
                    .accounts().stream()
                    .filter(account -> account.balance() != null)
                    .filter(account -> account.balance().compareTo(BigDecimal.ZERO) > 0)
                    .map(FrontAccountDto::currencyStringCode)
                    .filter(currencyStringCode -> !activeAccountsFromFront.contains(currencyStringCode))
                    .toList();
            if (notEmptyAccounts.isEmpty()) {
                return;
            }
            String message = "Ошибка: невозможно удалить следующие аккаунты, т.к. они баланс не пуст: "
                    + String.join(",", notEmptyAccounts);
            userAccountsErrors.add(message);
        } catch (Exception ex) {
            userAccountsErrors.add(ex.getMessage());
        }
    }


    /**
     * Контроллер для работы с наличными
     */
    @PostMapping(path = "/user/{username}/сash")
    RedirectView depositCash(@PathVariable @NotNull @NotBlank String username,
                             @ModelAttribute @Valid FrontCashTransactionDto cashOperationDto,
                             @RequestParam("action") @NotNull CashAction action,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Principal principal) {
        tracingLogger.info("Запрос на операцию с наличными от пользователя {}. Операция - {}",principal.getName(), cashOperationDto);
        RedirectView redirectView = new RedirectView();
        redirectView.setContextRelative(true);
        redirectView.setUrl("/main");
        List<String> cashErrors = new ArrayList<>();
        redirectAttributes.addFlashAttribute("cashErrors", cashErrors);
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .forEach(cashErrors::add);
            tracingLogger.info("Найдены ошибки при операции с наличными пользователя {} Ошибки: {}", principal.getName(), cashErrors);
            return redirectView;
        }
        try {
            frontService.processCashOperation(username, cashOperationDto, action);
            redirectAttributes.addFlashAttribute("isCashOperationSucceed", true);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                var details = ex.getResponseBodyAs(ProblemDetail.class);
                if (details != null && details.getDetail() != null) {
                    cashErrors.add(details.getDetail());
                }
            }
        } catch (Exception ex) {
            cashErrors.add(ex.getMessage());
        }
        return redirectView;
    }


    /**
     * Контроллер для перевода денег
     */
    @PostMapping(path = "/user/{username}/transfer")
    RedirectView depositCash(@PathVariable @NotNull @NotBlank String username,
                             @ModelAttribute @Valid NonCashTransfer nonCashTransaction,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Principal principal) {
        tracingLogger.info("Запрос на перевод средств от пользователя {}. Операция - {}",principal.getName(), nonCashTransaction);
        RedirectView redirectView = new RedirectView();
        redirectView.setContextRelative(true);
        redirectView.setUrl("/main");
        List<String> transferErrors = new ArrayList<>();
        switch (nonCashTransaction.direction()) {
            case YOURSELF -> redirectAttributes.addFlashAttribute("transferYourselfErrors", transferErrors);
            case OTHER -> redirectAttributes.addFlashAttribute("transferOtherErrors", transferErrors);
        }
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .forEach(transferErrors::add);
            tracingLogger.info("Найдены ошибки при операции перевода денег пользователя {} Ошибки: {}", principal.getName(), transferErrors);
            return redirectView;
        }
        try {
            frontService.processNonCashOperation(nonCashTransaction);
            switch (nonCashTransaction.direction()) {
                case YOURSELF -> redirectAttributes.addFlashAttribute("isTransferYourselfSucceed", true);
                case OTHER -> redirectAttributes.addFlashAttribute("isTransferOtherSucceed", true);
            }
        } catch (HttpClientErrorException ex) {
            countFailedTransferTransactions(nonCashTransaction);
            if (ex.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                var details = ex.getResponseBodyAs(ProblemDetail.class);
                if (details != null && details.getDetail() != null) {
                    transferErrors.add(details.getDetail());
                }
            }
        } catch (Exception ex) {
            countFailedTransferTransactions(nonCashTransaction);
            transferErrors.add(ex.getMessage());
        }
        return redirectView;
    }

    private void countFailedTransferTransactions(NonCashTransfer nct) {
        Counter failedNonCashTransactions = Counter.builder("transfer_transaction_fails")
                .description("Counter of failed transfer transactions")
                .tag("type", "non-cash")
                .tag("direction", (nct.direction() == null) ? "null" : nct.direction().name())
                .tag("sender", (nct.fromUsername() == null) ? "null" : nct.fromUsername())
                .tag("receiver", (nct.toUsername() == null) ? "null" : nct.toUsername())
                .tag("sender_currency", (nct.fromCurrency() == null) ? "null" : nct.fromCurrency().name())
                .tag("receiver_currency", (nct.toCurrency() == null) ? "null" : nct.toCurrency().name())
                .register(meterRegistry);
        failedNonCashTransactions.increment();
    }
}
