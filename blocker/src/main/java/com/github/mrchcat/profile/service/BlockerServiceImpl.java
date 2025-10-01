package com.github.mrchcat.profile.service;

import com.github.mrchcat.shared.blocker.BlockerResponseDto;
import com.github.mrchcat.shared.cash.CashTransactionDto;
import com.github.mrchcat.shared.enums.TransferDirection;
import com.github.mrchcat.shared.transfer.NonCashTransferDto;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockerServiceImpl implements BlockerService {
    @Value("#{new Double('${application.CONFIRM_PROBABILITY:0.9}')}")
    private double CONFIRM_PROBABILITY;

    private final List<String> rejectReasons = List.of(
            "сомнительные источники поступлений",
            "подозрительная операция",
            "нарушение закона об отмывании средств",
            "ограничения на лимит перевода",
            "ограничения на определённые виды деятельности"
    );

    private final Tracer tracer;
    private final String SPAN_NAME = "check-result";

    @Override
    public BlockerResponseDto checkCashTransaction(CashTransactionDto cashTransactionDto) {
        var newSpan = tracer.nextSpan().name(SPAN_NAME).start();
        newSpan.tag("type", "cash");
        newSpan.tag("action", cashTransactionDto.action().name());
        newSpan.tag("sender", cashTransactionDto.username());
        newSpan.tag("receiver", cashTransactionDto.username());
        newSpan.tag("currency", cashTransactionDto.currency().name());
        try {
            BlockerResponseDto response = getRandomResponse();
            newSpan.tag("is_confirmed", response.isConfirmed());
            return getRandomResponse();
        } finally {
            newSpan.end();
        }
    }

    @Override
    public BlockerResponseDto checkNonCashTransaction(NonCashTransferDto nonCashTransferDto) {
        var newSpan = tracer.nextSpan().name(SPAN_NAME).start();
        newSpan.tag("type", "non-cash");
        newSpan.tag("direction", nonCashTransferDto.direction().name());
        newSpan.tag("sender", nonCashTransferDto.fromUsername());
        String reciver;
        if(nonCashTransferDto.direction()==TransferDirection.YOURSELF){
            reciver=nonCashTransferDto.fromUsername();
        } else {
            reciver=nonCashTransferDto.toUsername();
        }
        newSpan.tag("receiver", reciver);
        newSpan.tag("sender_currency", nonCashTransferDto.fromCurrency().name());
        newSpan.tag("receiver_currency", nonCashTransferDto.toCurrency().name());
        try {
            BlockerResponseDto response = getRandomResponse();
            newSpan.tag("is_confirmed", response.isConfirmed());
            return response;
        } finally {
            newSpan.end();
        }
    }

    private BlockerResponseDto getRandomResponse() {
        if (Math.random() > CONFIRM_PROBABILITY) {
            int answer = (int) (Math.random() * rejectReasons.size());
            return new BlockerResponseDto(false, rejectReasons.get(answer));
        }
        return new BlockerResponseDto(true, "операция подтверждена");
    }

}
