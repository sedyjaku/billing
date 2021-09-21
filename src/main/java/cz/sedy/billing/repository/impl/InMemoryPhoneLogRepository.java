package cz.sedy.billing.repository.impl;

import cz.sedy.billing.repository.PhoneLogRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPhoneLogRepository implements PhoneLogRepository {

    @Override
    public boolean isMostCalledNumber(String number) {
        return false;
    }
}
