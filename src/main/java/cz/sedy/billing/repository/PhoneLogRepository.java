package cz.sedy.billing.repository;

public interface PhoneLogRepository {

    boolean isMostCalledNumber(String number);

    void addCallToNumber(String number);
}
