package org.proxies.spring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

@Slf4j
@RequiredArgsConstructor
public class SpringLoggingExampleProxyHandler implements MethodInterceptor {

    private final Object proxiedClass;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        Method method = invocation.getMethod();
        Object[] args = invocation.getArguments();

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
