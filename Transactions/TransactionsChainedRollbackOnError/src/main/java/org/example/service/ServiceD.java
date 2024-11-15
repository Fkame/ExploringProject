package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dao.TestTableDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceD {

    public static final int SERVICE_ID = 4;
    public static final String serviceText = "ServiceD";

    private final TestTableDao testTableDao;

    @Transactional
    public void doLogic() {
        testTableDao.insert(SERVICE_ID, serviceText);
    }
}
