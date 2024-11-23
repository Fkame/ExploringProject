package org.proxies.cglib;

import net.sf.cglib.proxy.Enhancer;
import org.proxies.core.IContract;
import org.proxies.core.NotContractedService;

public class CglibProxyFactory {

    public static NotContractedService getProxyForNotContracted(NotContractedService proxyTarget) {
        CglibLoggingExampleProxyHandler callback = new CglibLoggingExampleProxyHandler(proxyTarget);

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(proxyTarget.getClass());
        enhancer.setCallback(callback);

        return (NotContractedService) enhancer.create();
    }

    public static IContract getProxyForContracted(IContract proxyTarget) {
        CglibLoggingExampleProxyHandler callback = new CglibLoggingExampleProxyHandler(proxyTarget);

        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(proxyTarget.getClass().getInterfaces());
        enhancer.setCallback(callback);

        return (IContract) enhancer.create();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getProxyGeneric(T proxyTarget) {
        CglibLoggingExampleProxyHandler callback = new CglibLoggingExampleProxyHandler(proxyTarget);

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(proxyTarget.getClass());
        enhancer.setCallback(callback);

        return (T) enhancer.create();
    }
}
