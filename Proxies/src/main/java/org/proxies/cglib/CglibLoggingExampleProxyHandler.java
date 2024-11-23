package org.proxies.cglib;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

@Slf4j
@RequiredArgsConstructor
public class CglibLoggingExampleProxyHandler implements MethodInterceptor {

    private final Object proxiedClass;

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

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
