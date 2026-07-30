package com.financedomain.personnalisation.proxy;

import com.financedomain.personnalisation.proxy.fallback.PythonPersonalizationProxyFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "python-personalization-api", url = "${python.api.url}", fallback = PythonPersonalizationProxyFallback.class)
public interface PythonPersonalizationProxy {

    @GetMapping("/api/v1/usages/{msisdn}")
    Object getClientUsages(@PathVariable("msisdn") String msisdn);

    @GetMapping("/api/v1/usages/stats/global")
    Object getGlobalStats();
}
