package org.proxies.core;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotContractedService {
    public void doLogic() {
        log.info("{} called!", this.getClass().getSimpleName());
    }
}
