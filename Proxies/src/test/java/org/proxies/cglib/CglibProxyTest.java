package org.proxies.cglib;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.proxies.core.ContractedServiceImpl;
import org.proxies.core.IContract;
import org.proxies.core.NotContractedService;

@Slf4j
class CglibProxyTest {

    /*
    При JDK >= 17 версии из-за системы модулей у cglib начинаются проблемы: https://github.com/cglib/cglib/issues/191,
    нужно добавить в JVM OPTS:
    --add-exports=java.naming/com.sun.jndi.ldap=ALL-UNNAMED
    --add-opens=java.base/java.lang=ALL-UNNAMED
    --add-opens=java.base/java.lang.invoke=ALL-UNNAMED
    --add-opens=java.base/java.io=ALL-UNNAMED
    --add-opens=java.base/java.security=ALL-UNNAMED
    --add-opens=java.base/java.util=ALL-UNNAMED
    --add-opens=java.management/javax.management=ALL-UNNAMED
    --add-opens=java.naming/javax.naming=ALL-UNNAMED
     */

    @Test
    void getProxyForNotContracted() {
        NotContractedService target = new NotContractedService();

        NotContractedService proxy = CglibProxyFactory.getProxyForNotContracted(target);

        proxy.doLogic();

        log.warn("""
                        === Info about proxy:
                        Proxy class = {},
                        superclass = {},
                        interfaces = {}""",
                proxy.getClass(),
                proxy.getClass().getSuperclass(),
                proxy.getClass().getInterfaces()
        );

        // AopUtils.getTargetClass(proxy) тут не сработает, так как это ванильный cglib, а не спринговый.
        // Спринговый дообогащен разными контрактами вспомогательными
    }

    @Test
    void getProxyForContracted() {
        IContract target = new ContractedServiceImpl();

        IContract proxy = CglibProxyFactory.getProxyForContracted(target);

        proxy.doLogic();

        log.warn("""
                        === Info about proxy:
                        Proxy class = {},
                        superclass = {},
                        interfaces = {}""",
                proxy.getClass(),
                proxy.getClass().getSuperclass(),
                proxy.getClass().getInterfaces()
        );

        // AopUtils.getTargetClass(proxy) тут не сработает, так как это ванильный cglib, а не спринговый.
        // Спринговый дообогащен разными контрактами вспомогательными
    }
}