package com.financedomain.personnalisation.service;
import com.financedomain.personnalisation.proxy.PythonPersonalizationProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PersonnalisationService {

    @Autowired
    private PythonPersonalizationProxy pythonProxy;
    
    @Cacheable(value = "personalisationUsages", key = "#msisdn")
    public Object getClientUsages(String msisdn) {
        System.out.println("====== [Personnalisation Cache Miss] Calling HDFS Python API for MSISDN: " + msisdn + " ======");
        return pythonProxy.getClientUsages(msisdn);
    }
}