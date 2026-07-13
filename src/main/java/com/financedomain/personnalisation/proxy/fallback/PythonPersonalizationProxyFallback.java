package com.financedomain.personnalisation.proxy.fallback;

import com.financedomain.personnalisation.proxy.PythonPersonalizationProxy;
import org.springframework.stereotype.Component;
import java.util.ArrayList;

@Component
public class PythonPersonalizationProxyFallback implements PythonPersonalizationProxy {

    @Override
    public Object getClientUsages(String msisdn) {
        System.err.println("[Fallback] L'API Python de personnalisation est indisponible. Retour d'une liste vide par défaut pour le msisdn : " + msisdn);
        // Dégradation gracieuse : retourne une liste vide pour ne pas faire planter l'application cliente
        return new ArrayList<>();
    }
}
