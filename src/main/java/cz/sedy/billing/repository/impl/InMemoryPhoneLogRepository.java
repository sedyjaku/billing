package cz.sedy.billing.repository.impl;

import cz.sedy.billing.repository.PhoneLogRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository
public class InMemoryPhoneLogRepository implements PhoneLogRepository {

    private SortedMap<String, Long> phoneLogMap = new TreeMap<String, Long>();

    @Override
    public boolean isMostCalledNumber(String number) {
        return false;
    }

    @Override
    public void addCallToNumber(String number) {
        phoneLogMap.computeIfPresent(number, (key, val) -> val + 1);
        phoneLogMap.putIfAbsent(number, 1L);
    }
}
