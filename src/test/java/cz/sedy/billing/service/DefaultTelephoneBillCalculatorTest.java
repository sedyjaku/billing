package cz.sedy.billing.service;

import cz.sedy.billing.repository.PhoneLogRepository;
import cz.sedy.billing.service.impl.DefaultTelephoneBillCalculator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

public class DefaultTelephoneBillCalculatorTest {

    TelephoneBillCalculator telephoneBillCalculator;

    @BeforeEach
    public void setUp(){
        var phoneLogRepository = Mockito.mock(PhoneLogRepository.class);
        telephoneBillCalculator = new DefaultTelephoneBillCalculator(phoneLogRepository);
        Mockito.when(phoneLogRepository.isMostCalledNumber(Mockito.any())).thenReturn(false);
    }

    @Test
    public void shouldReturnCalculatedValue_WithCallDuringMainHours(){
        Assertions.assertThat(telephoneBillCalculator.calculate("420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57"))
                .isEqualTo(BigDecimal.valueOf(3));
    }
}
