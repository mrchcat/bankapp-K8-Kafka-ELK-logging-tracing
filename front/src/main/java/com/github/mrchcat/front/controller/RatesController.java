package com.github.mrchcat.front.controller;

import com.github.mrchcat.front.dto.FrontRate;
import com.github.mrchcat.front.service.FrontService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequiredArgsConstructor
public class RatesController {
    private final FrontService frontService;

    /**
     * Контроллер для получения данных о курсах
     */
    @GetMapping("/front/rates")
    List<FrontRate> getAllRates() throws AuthException {
        return frontService.getAllRates();
    }
}
