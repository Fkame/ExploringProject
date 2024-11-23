package org.proxies.spring;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.proxies.core.ContractedServiceImpl;
import org.proxies.core.IContract;
import org.proxies.core.NotContractedService;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;

@Slf4j
class ProxyBySpringTest {

    /*
    Spring CGLIB избавлен от проблемы ванильного в JDK >= 17.
    Его ProxyFactory содержит в себе обёртку над CGLIB и JDK proxy, а зависимости от входных данных предпочтет то, либо другое.
     */

    @Test
    void createProxyFromClass() {
        NotContractedService proxyTarget = new NotContractedService();
        SpringLoggingExampleProxyHandler handler = new SpringLoggingExampleProxyHandler(proxyTarget);

        ProxyFactory proxyFactory = new ProxyFactory(proxyTarget);
        proxyFactory.addAdvice(handler);

        NotContractedService proxy = (NotContractedService) proxyFactory.getProxy();

        proxy.doLogic();

        log.warn("""
                        === Info about proxy:
                        Proxy class = {},
                        superclass = {},
                        interfaces = {},
                        targetClass = {}""",
                proxy.getClass(),
                proxy.getClass().getSuperclass(),
                proxy.getClass().getInterfaces(),
                AopUtils.getTargetClass(proxy)
        );

        // В интерфейсах добавился interface org.springframework.aop.framework.Advised, который позволяет получать
        // доп. информацию о проксе и использовать AopUtils
    }

    @Test
    void createProxyFromInterface() {
        IContract proxyTarget = new ContractedServiceImpl();
        SpringLoggingExampleProxyHandler handler = new SpringLoggingExampleProxyHandler(proxyTarget);

        ProxyFactory proxyFactory = new ProxyFactory(proxyTarget);
        proxyFactory.addAdvice(handler);

        IContract proxy = (IContract) proxyFactory.getProxy();

        proxy.doLogic();

        log.warn("""
                        === Info about proxy:
                        Proxy class = {},
                        superclass = {},
                        interfaces = {},
                        targetClass = {}""",
                proxy.getClass(),
                proxy.getClass().getSuperclass(),
                proxy.getClass().getInterfaces(),
                AopUtils.getTargetClass(proxy)
        );

        // В интерфейсах добавился interface org.springframework.aop.framework.Advised, который позволяет получать
        // доп. информацию о проксе и использовать AopUtils
    }
}


