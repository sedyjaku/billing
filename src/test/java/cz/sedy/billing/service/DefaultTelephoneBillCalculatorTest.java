package cz.sedy.billing.service;

import cz.sedy.billing.exception.InvalidFormatException;
import cz.sedy.billing.repository.PhoneLogRepository;
import cz.sedy.billing.service.impl.DefaultTelephoneBillCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultTelephoneBillCalculatorTest {

    TelephoneBillCalculator telephoneBillCalculator;

    @BeforeEach
    public void setUp() {
        var phoneLogRepository = Mockito.mock(PhoneLogRepository.class);
        telephoneBillCalculator = new DefaultTelephoneBillCalculator(phoneLogRepository);
        Mockito.when(phoneLogRepository.isMostCalledNumber(Mockito.any())).thenReturn(false);
    }

    @Test
    public void shouldReturnCalculatedValue_WithCallDuringPeakHoursShorterThan5() {
        Assertions.assertEquals(BigDecimal.valueOf(3.0), telephoneBillCalculator.calculate("420774577453,13-01-2020 08:10:15,13-01-2020 08:12:57"));
    }

    @Test
    public void shouldReturnCalculatedValue_WithCallDuringPeakHoursLongerThan5() {
        Assertions.assertEquals(BigDecimal.valueOf(9.2), telephoneBillCalculator.calculate("420774577453,13-01-2020 08:10:15,13-01-2020 08:35:57"));
    }

    @Test
    public void shouldReturnCalculatedValue_WithCallDuringNonPeakHoursShorterThan5() {
        Assertions.assertEquals( BigDecimal.valueOf(1.5), telephoneBillCalculator.calculate("420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57"));
    }

    @Test
    public void shouldReturnCalculatedValue_WithCallDuringNonPeakHoursLongerThan5() {
        Assertions.assertEquals(BigDecimal.valueOf(6.7), telephoneBillCalculator.calculate("420774577453,13-01-2020 18:10:15,13-01-2020 18:35:57"));
    }

    @Test
    public void shouldReturnCalculatedValue_WithStartNearPeakStartSmallerThan5() {
        Assertions.assertEquals(BigDecimal.valueOf(1.5), telephoneBillCalculator.calculate("420774577453,13-01-2020 07:59:15,13-01-2020 08:01:00"));
    }

    @Test
    public void shouldReturnCalculatedValue_WithStartNearPeakEndSmallerThan5() {
        Assertions.assertEquals(BigDecimal.valueOf(3.5), telephoneBillCalculator.calculate("420774577453,13-01-2020 15:57:15,13-01-2020 16:01:00"));
    }

    @Test
    public void shouldReturnZero_WithCallEndingBeforeStart() {
        Assertions.assertEquals(BigDecimal.ZERO, telephoneBillCalculator.calculate("420774577453,13-01-2020 18:10:15,13-01-2020 08:35:57"));
    }

    @Test
    public void shouldThrowException_WithLessInputFieldsThan3() {
        assertThrows(InvalidFormatException.class, (() -> telephoneBillCalculator.calculate("420774577453,13-01-2020 18:10:15")));
    }

    @Test
    public void shouldThrowException_WithMoreInputFieldsThan3() {
        assertThrows(InvalidFormatException.class, (() -> telephoneBillCalculator.calculate("420774577453,13-01-2020 18:10:15,13-01-2020 18:10:15,13-01-2020 18:10:15,13-01-2020 18:10:15")));
    }

    @Test
    public void shouldThrowException_WithNullInput() {
        assertThrows(NullPointerException.class, (() -> telephoneBillCalculator.calculate(null)));
    }

}
