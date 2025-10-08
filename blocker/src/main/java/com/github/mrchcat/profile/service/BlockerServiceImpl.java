package com.github.mrchcat.profile.service;

import com.github.mrchcat.shared.blocker.BlockerResponseDto;
import com.github.mrchcat.shared.cash.CashTransactionDto;
import com.github.mrchcat.shared.transfer.NonCashTransferDto;
import com.github.mrchcat.shared.utils.log.TracingLogger;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockerServiceImpl implements BlockerService {
    @Value("#{new Double('${application.CONFIRM_PROBABILITY:0.95}')}")
    private double CONFIRM_PROBABILITY;
    private final TracingLogger tracingLogger;

    private final List<String> rejectReasons = List.of(
            "сомнительные источники поступлений",
            "подозрительная операция",
            "нарушение закона об отмывании средств",
            "ограничения на лимит перевода",
            "ограничения на определённые виды деятельности"
    );

    private final MeterRegistry meterRegistry;

    @Override
    public BlockerResponseDto checkCashTransaction(CashTransactionDto cashTransactionDto) {
        BlockerResponseDto response = getRandomResponse();
        tracingLogger.info("Поступил запрос на проверку операции с наличными {}. Результат проверки {}",
                cashTransactionDto, response);
        countBlockedTransactions(cashTransactionDto, response);
        return response;
    }

    @Override
    public BlockerResponseDto checkNonCashTransaction(NonCashTransferDto nonCashTransferDto) {
        BlockerResponseDto response = getRandomResponse();
        tracingLogger.info("Поступил запрос на проверку операции перевода средств {}. Результат проверки {}",
                nonCashTransferDto, response);
        countBlockedTransactions(nonCashTransferDto, response);
        return response;
    }

    private BlockerResponseDto getRandomResponse() {
        if (Math.random() > CONFIRM_PROBABILITY) {
            int answer = (int) (Math.random() * rejectReasons.size());
            return new BlockerResponseDto(false, rejectReasons.get(answer));
        }
        return new BlockerResponseDto(true, "операция подтверждена");
    }

    private void countBlockedTransactions(Object transactionDto, BlockerResponseDto response) {
        if (response.isConfirmed()) {
            return;
        }
        Counter blocksCounter;
        if (transactionDto instanceof CashTransactionDto cashDto) {
            blocksCounter = Counter.builder("blocks")
                    .description("Counter of blocked cash transactions")
                    .tag("type", "cash")
                    .tag("action", cashDto.action().name())
                    .tag("username", cashDto.username())
                    .tag("currency", cashDto.currency().name())
                    .register(meterRegistry);
        } else if (transactionDto instanceof NonCashTransferDto nonCashDto) {
            blocksCounter = Counter.builder("blocks")
                    .description("Counter of blocked non-cash transactions")
                    .tag("type", "non-cash")
                    .tag("direction", nonCashDto.direction().name())
                    .tag("sender", nonCashDto.fromUsername())
                    .tag("receiver", nonCashDto.toUsername())
                    .tag("sender_currency", nonCashDto.fromUsername())
                    .tag("receiver_currency", nonCashDto.toCurrency().name())
                    .register(meterRegistry);
        } else {
            throw new IllegalArgumentException("icorrect class of TransactionDto");
        }
        blocksCounter.increment();
    }

}
