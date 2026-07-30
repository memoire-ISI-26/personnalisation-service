package com.financedomain.personnalisation.service;

import com.financedomain.personnalisation.dto.DefaultServicesDto;
import com.financedomain.personnalisation.model.DefaultServiceConfig;
import com.financedomain.personnalisation.proxy.PythonPersonalizationProxy;
import com.financedomain.personnalisation.repository.DefaultServiceConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PersonnalisationService {

    private static final String TELCO = "TELCO";
    private static final String APPEL = "TELCO.SERVICES.PASS.VOICE";
    private static final String INTERNET = "TELCO.SERVICES.PASS.DATA";
    private static final String ILLIMIX = "TELCO.SERVICES.PASS.ILLIMIX";
    private static final String ILLIFLEX = "TELCO.SERVICES.PASS.ILLIFLEX";
    private static final String TRANSFERT = "OMY.SERVICES.TRANSFERT";
    private static final String RETRAIT = "OMY.SERVICES.RETRAIT";
    private static final String VOCAL = "OMY.SERVICES.VOICEBUNDLE";


    @Autowired
    private PythonPersonalizationProxy pythonProxy;

    @Autowired
    private DefaultServiceConfigRepository configRepository;
    
    public Object getClientUsages(String msisdn) {
        log.info("====== Calling HDFS Python API for MSISDN: {} ======", msisdn);
        return pythonProxy.getClientUsages(msisdn);
    }

    public Object getGlobalStats() {
        log.info("====== Calling HDFS Python API for Global Stats ======");
        return pythonProxy.getGlobalStats();
    }

    public List<DefaultServicesDto> getDefaultServices() {
        List<DefaultServicesDto> result = new ArrayList<>();
        
        // TELCO
        Optional<DefaultServiceConfig> telcoOpt = configRepository.findByUniverse(TELCO);
        if (telcoOpt.isPresent()) {
            DefaultServiceConfig config = telcoOpt.get();
            result.add(new DefaultServicesDto(
                TELCO,
                config.getServiceId1(), 
                config.getServiceId2(),
                config.getAdvServiceId1() != null ? config.getAdvServiceId1() : APPEL,
                config.getAdvServiceId2() != null ? config.getAdvServiceId2() : INTERNET,
                config.getAdvServiceId3() != null ? config.getAdvServiceId3() : ILLIMIX,
                config.getAdvServiceId4() != null ? config.getAdvServiceId4() : ILLIFLEX,
                config.getAdvServiceId5() != null ? config.getAdvServiceId5() : APPEL
            ));
        } else {
            result.add(new DefaultServicesDto(
                TELCO,
                APPEL,
                INTERNET,
                APPEL,
                INTERNET,
                ILLIMIX,
                ILLIFLEX,
                APPEL
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
                config.getAdvServiceId1() != null ? config.getAdvServiceId1() : TRANSFERT,
                config.getAdvServiceId2() != null ? config.getAdvServiceId2() : VOCAL,
                config.getAdvServiceId3() != null ? config.getAdvServiceId3() : "DEPOT",
                config.getAdvServiceId4() != null ? config.getAdvServiceId4() : RETRAIT,
                config.getAdvServiceId5() != null ? config.getAdvServiceId5() : "RAPIDO"
            ));
        } else {
            result.add(new DefaultServicesDto(
                "OMY", 
                TRANSFERT,
                VOCAL,
                TRANSFERT,
                VOCAL,
                "DEPOT",
                RETRAIT,
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