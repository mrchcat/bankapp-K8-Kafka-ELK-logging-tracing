package com.github.mrchcat.profile.service;

import com.github.mrchcat.shared.blocker.BlockerResponseDto;
import com.github.mrchcat.shared.cash.CashTransactionDto;
import com.github.mrchcat.shared.enums.TransferDirection;
import com.github.mrchcat.shared.transfer.NonCashTransferDto;
import com.github.mrchcat.shared.utils.log.TracingLogger;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

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
        double randomDbl = ThreadLocalRandom.current().nextDouble();
        if (randomDbl > CONFIRM_PROBABILITY) {
            int answer = (int) (randomDbl * rejectReasons.size());
            return new BlockerResponseDto(false, rejectReasons.get(answer));
        }
        return new BlockerResponseDto(true, "операция подтверждена");
    }

    private void countBlockedTransactions(Object transactionDto, BlockerResponseDto response) {
        if (response.isConfirmed()) {
            return;
        }
        if (transactionDto instanceof CashTransactionDto cashDto) {
            Counter blocksCounter = Counter.builder("cash-blocks")
                    .description("Counter of blocked cash transactions")
                    .tag("username", Objects.requireNonNull(cashDto.username()))
                    .tag("cash", Objects.requireNonNull(cashDto.currency().name()))
                    .register(meterRegistry);
            blocksCounter.increment();
        } else if (transactionDto instanceof NonCashTransferDto nonCashDto) {
            var receiver = (nonCashDto.direction().equals(TransferDirection.YOURSELF)) ? nonCashDto.fromUsername() : nonCashDto.toUsername();
            Counter blocksCounter = Counter.builder("non-cash-blocks")
                    .description("Counter of blocked non-cash transactions")
                    .tag("username", Objects.requireNonNull(nonCashDto.fromUsername()))
                    .tag("receiver", Objects.requireNonNull(receiver))
                    .tag("from_cash", Objects.requireNonNull(nonCashDto.fromCurrency().name()))
                    .tag("to_cash", Objects.requireNonNull(nonCashDto.toCurrency().name()))
                    .register(meterRegistry);
            blocksCounter.increment();
        } else {
            throw new IllegalArgumentException("incorrect class of TransactionDto");
        }
    }

}
