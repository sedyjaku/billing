package cz.sedy.billing.service.impl;

import cz.sedy.billing.exception.InvalidFormatException;
import cz.sedy.billing.repository.PhoneLogRepository;
import cz.sedy.billing.service.TelephoneBillCalculator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Service
public class DefaultTelephoneBillCalculator implements TelephoneBillCalculator {

    private static final String SPLITTER = ",";
    private static final BigDecimal BASIC_FEE = BigDecimal.valueOf(0.2);
    private static final BigDecimal PEAK_RATE = BigDecimal.valueOf(1);
    private static final BigDecimal NON_PEAK_RATE = BigDecimal.valueOf(0.5);
    private static final BigDecimal INITIAL_FEE_LENGTH = BigDecimal.valueOf(5);
    private static final Long BEFORE_PEAK_START_HOUR = 7L;
    private static final Long BEFORE_PEAK_END_HOUR = 15L;
    private static final Long MAX_MINUTES = 60L;
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final PhoneLogRepository phoneLogRepository;

    @Override
    public BigDecimal calculate(@NonNull String phoneLog) {
        var splitLog = phoneLog.split(SPLITTER);
        if (splitLog.length != 3) {
            throw new InvalidFormatException("Invalid format of input");
        }
        var phoneNumber = splitLog[0];
        var callStart = LocalDateTime.parse(splitLog[1], formatter);
        var callEnd = LocalDateTime.parse(splitLog[2], formatter);


        phoneLogRepository.addCallToNumber(phoneNumber);
        
        if (phoneLogRepository.isMostCalledNumber(phoneNumber)) {
            return BigDecimal.ZERO;
        }

        return calculateCallPrice(callStart, callEnd);

    }

    private BigDecimal calculateCallPrice(LocalDateTime callStart, LocalDateTime callEnd) {
        var callLength = BigDecimal.valueOf(callEnd.toEpochSecond(ZoneOffset.UTC) - callStart.toEpochSecond(ZoneOffset.UTC));
        var callLengthInMinutes = callLength.divide(BigDecimal.valueOf(60L), RoundingMode.CEILING);
        if (startedNearRateChange(callStart)){
            BigDecimal peakLength = BigDecimal.ZERO;
            if (callStart.getHour() == BEFORE_PEAK_END_HOUR){
                peakLength = BigDecimal.valueOf(MAX_MINUTES - callStart.getMinute());
            }
            else if (callStart.getHour() == BEFORE_PEAK_START_HOUR){
                peakLength = INITIAL_FEE_LENGTH.min(callLengthInMinutes).subtract(BigDecimal.valueOf(MAX_MINUTES - callStart.getMinute()));
            }
            return calculateFeesWithInitialLength(callLengthInMinutes.subtract(peakLength), NON_PEAK_RATE, INITIAL_FEE_LENGTH.subtract(peakLength))
                    .add(calculateFeesWithInitialLength(peakLength, PEAK_RATE, peakLength));
        }
        else if (isInLowRateInterval(callStart)){
            return calculateFees(callLengthInMinutes, NON_PEAK_RATE);
        }
        else {
            return calculateFees(callLengthInMinutes, PEAK_RATE);
        }
    }

    private BigDecimal calculateFees(BigDecimal callLength, BigDecimal initialFee) {
        return calculateFeesWithInitialLength(callLength, initialFee, INITIAL_FEE_LENGTH);
    }

    private BigDecimal calculateFeesWithInitialLength(BigDecimal callInMinutes, BigDecimal initialFee, BigDecimal initialLength) {
        var initialFeeLength = callInMinutes.min(initialLength);
        var basicFeeLength = callInMinutes.subtract(INITIAL_FEE_LENGTH).max(BigDecimal.valueOf(0L));
        return BigDecimal.ZERO.max(initialFeeLength.multiply(initialFee).add(basicFeeLength.multiply(BASIC_FEE)));
    }

    private boolean isInLowRateInterval(LocalDateTime callStart) {
        return callStart.getHour() < 8 || callStart.getHour() >= 16;
    }

    private boolean startedNearRateChange(LocalDateTime callStart) {
        return (callStart.getHour() == BEFORE_PEAK_START_HOUR || callStart.getHour() == BEFORE_PEAK_END_HOUR) && callStart.getMinute() > 55;
    }
}
