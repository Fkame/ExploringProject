package org.proxies.core;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContractedServiceImpl implements IContract {

    @Override
    public void doLogic() {
        log.info("{} called!", this.getClass().getSimpleName());
    }
}
