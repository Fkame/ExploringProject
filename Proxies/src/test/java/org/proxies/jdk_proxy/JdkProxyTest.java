package org.proxies.jdk_proxy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.proxies.core.ContractedServiceImpl;
import org.proxies.core.IContract;

@Slf4j
class JdkProxyTest {

    @Test
    void getProxyForContracted() {

        IContract target = new ContractedServiceImpl();

        IContract proxy = JdkProxyFactory.getProxyForContracted(target);

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

        // Не умеет проксировать через наследование - только через имплементацию интерфейсов

        // AopUtils.getTargetClass(proxy) тут не сработает, так как это ванильный cglib, а не спринговый.
        // Спринговый дообогащен разными контрактами вспомогательными
    }
}