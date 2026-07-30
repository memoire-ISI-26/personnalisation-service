package com.financedomain.personnalisation.proxy.fallback;

import com.financedomain.personnalisation.proxy.PythonPersonalizationProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@Component
public class PythonPersonalizationProxyFallback implements PythonPersonalizationProxy {

    @Override
    public Object getClientUsages(String msisdn) {
        log.error("[Fallback] L'API Python de personnalisation est indisponible. Retour d'une liste vide par défaut pour le msisdn : {}", msisdn);
        return new ArrayList<>();
    }

    @Override
    public Object getGlobalStats() {
        log.error("[Fallback] L'API Python de personnalisation est indisponible pour les statistiques globales.");
        return new HashMap<>();
    }
}
