package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dao.TestTableDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceA {

    public static final int SERVICE_ID = 1;
    public static final String serviceText = "ServiceA";

    private final TestTableDao testTableDao;
    private final ServiceB serviceB;
    private final ServiceC serviceC;
    private final ServiceD serviceD;

    @Transactional
    public void doLogic() {
        // Это изменение откатится из-за того, что ServiceB отравит состояние транзакции своим ролбеком
        testTableDao.insert(SERVICE_ID, serviceText);

        // Это изменение откатится из-за того, что ServiceB выбросит ошибку, по итогу транзакционный аспект запустит rollback
        // и отравит состояние внешней транзакции ServiceA
        try {
            serviceB.doLogic();
        } catch (RuntimeException ex) { }

        // Это изменение успешно применится, т.к. тип пропагации - REQUIRED_NEW, следовательно у него будет своя независимая транзакция
        serviceC.doLogic();

        // Это изменение откатится из-за того, что ServiceB отравит состояние транзакции своим ролбеком
        serviceD.doLogic();
    }
}
