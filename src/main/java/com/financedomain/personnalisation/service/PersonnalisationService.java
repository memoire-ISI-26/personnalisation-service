package com.financedomain.personnalisation.service;

import com.financedomain.personnalisation.dto.DefaultServicesDto;
import com.financedomain.personnalisation.model.DefaultServiceConfig;
import com.financedomain.personnalisation.proxy.PythonPersonalizationProxy;
import com.financedomain.personnalisation.repository.DefaultServiceConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PersonnalisationService {

    @Autowired
    private PythonPersonalizationProxy pythonProxy;

    @Autowired
    private DefaultServiceConfigRepository configRepository;
    
    @Cacheable(value = "personalisationUsages", key = "#msisdn")
    public Object getClientUsages(String msisdn) {
        System.out.println("====== [Personnalisation Cache Miss] Calling HDFS Python API for MSISDN: " + msisdn + " ======");
        return pythonProxy.getClientUsages(msisdn);
    }

    public List<DefaultServicesDto> getDefaultServices() {
        List<DefaultServicesDto> result = new ArrayList<>();
        
        // TELCO
        Optional<DefaultServiceConfig> telcoOpt = configRepository.findByUniverse("TELCO");
        if (telcoOpt.isPresent()) {
            DefaultServiceConfig config = telcoOpt.get();
            result.add(new DefaultServicesDto(
                "TELCO", 
                config.getServiceId1(), 
                config.getServiceId2(),
                config.getAdvServiceId1() != null ? config.getAdvServiceId1() : "TELCO.SERVICES.PASS.VOICE",
                config.getAdvServiceId2() != null ? config.getAdvServiceId2() : "TELCO.SERVICES.PASS.DATA",
                config.getAdvServiceId3() != null ? config.getAdvServiceId3() : "TELCO.SERVICES.PASS.ILLIMIX",
                config.getAdvServiceId4() != null ? config.getAdvServiceId4() : "TELCO.SERVICES.PASS.ILLIFLEX",
                config.getAdvServiceId5() != null ? config.getAdvServiceId5() : "TELCO.SERVICES.PASS.VOICE"
            ));
        } else {
            result.add(new DefaultServicesDto(
                "TELCO", 
                "TELCO.SERVICES.PASS.VOICE", 
                "TELCO.SERVICES.PASS.DATA",
                "TELCO.SERVICES.PASS.VOICE",
                "TELCO.SERVICES.PASS.DATA",
                "TELCO.SERVICES.PASS.ILLIMIX",
                "TELCO.SERVICES.PASS.ILLIFLEX",
                "TELCO.SERVICES.PASS.VOICE"
            ));
        }

        // OMY
        Optional<DefaultServiceConfig> omyOpt = configRepository.findByUniverse("OMY");
        if (omyOpt.isPresent()) {
            DefaultServiceConfig config = omyOpt.get();
            result.add(new DefaultServicesDto(
                "OMY", 
                config.getServiceId1(), 
                config.getServiceId2(),
                config.getAdvServiceId1() != null ? config.getAdvServiceId1() : "OMY.SERVICES.TRANSFERT",
                config.getAdvServiceId2() != null ? config.getAdvServiceId2() : "OMY.SERVICES.VOICEBUNDLE",
                config.getAdvServiceId3() != null ? config.getAdvServiceId3() : "DEPOT",
                config.getAdvServiceId4() != null ? config.getAdvServiceId4() : "OMY.SERVICES.RETRAIT",
                config.getAdvServiceId5() != null ? config.getAdvServiceId5() : "RAPIDO"
            ));
        } else {
            result.add(new DefaultServicesDto(
                "OMY", 
                "OMY.SERVICES.TRANSFERT", 
                "OMY.SERVICES.VOICEBUNDLE",
                "OMY.SERVICES.TRANSFERT",
                "OMY.SERVICES.VOICEBUNDLE",
                "DEPOT",
                "OMY.SERVICES.RETRAIT",
                "RAPIDO"
            ));
        }

        return result;
    }

    public void saveDefaultServices(List<DefaultServicesDto> dtos) {
        for (DefaultServicesDto dto : dtos) {
            if (dto.getUniverse() == null || dto.getUniverse().trim().isEmpty()) {
                continue;
            }
            DefaultServiceConfig config = configRepository.findByUniverse(dto.getUniverse())
                    .orElse(new DefaultServiceConfig());
            
            config.setUniverse(dto.getUniverse());
            config.setServiceId1(dto.getServiceId1());
            config.setServiceId2(dto.getServiceId2());
            config.setAdvServiceId1(dto.getAdvServiceId1());
            config.setAdvServiceId2(dto.getAdvServiceId2());
            config.setAdvServiceId3(dto.getAdvServiceId3());
            config.setAdvServiceId4(dto.getAdvServiceId4());
            config.setAdvServiceId5(dto.getAdvServiceId5());
            configRepository.save(config);
        }
    }
}