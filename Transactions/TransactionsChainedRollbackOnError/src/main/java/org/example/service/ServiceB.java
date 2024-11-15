package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dao.TestTableDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceB {

    public static final int SERVICE_ID = 2;
    public static final String serviceText = "ServiceB";

    private final TestTableDao testTableDao;

    @Transactional
    public void doLogic() throws RuntimeException{
        testTableDao.insert(SERVICE_ID, serviceText);
        throw new RuntimeException("I am krisa-service!");
    }
}
