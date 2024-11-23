package org.proxies.jdk_proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Slf4j
@RequiredArgsConstructor
public class JdkLoggingExampleProxyHandler implements InvocationHandler {

    private final Object proxiedClass;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("Prepared to call method = {} for class = {}",
                method.getName(),
                proxiedClass.getClass().getSimpleName()
        );

        Object result = method.invoke(proxiedClass, args);

        log.info("After calling method = {} for class = {}, got result = {}",
                method.getName(),
                proxiedClass.getClass().getSimpleName(),
                result
        );

        return result;
    }
}
