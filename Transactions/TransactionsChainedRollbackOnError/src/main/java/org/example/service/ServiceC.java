package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dao.TestTableDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceC {

    public static final int SERVICE_ID = 3;
    public static final String serviceText = "ServiceC";

    private final TestTableDao testTableDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void doLogic() {
        testTableDao.insert(SERVICE_ID, serviceText);
    }
}
