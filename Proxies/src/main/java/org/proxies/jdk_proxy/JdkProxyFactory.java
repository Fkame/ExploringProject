package org.proxies.jdk_proxy;

import org.proxies.core.IContract;

import java.lang.reflect.Proxy;

public class JdkProxyFactory {

    public static IContract getProxyForContracted(IContract proxyTarget) {
        JdkLoggingExampleProxyHandler callback = new JdkLoggingExampleProxyHandler(proxyTarget);

        Object proxy = Proxy.newProxyInstance(
                JdkProxyFactory.class.getClassLoader(),
                proxyTarget.getClass().getInterfaces(),
                callback
        );

        return (IContract) proxy;
    }
}
