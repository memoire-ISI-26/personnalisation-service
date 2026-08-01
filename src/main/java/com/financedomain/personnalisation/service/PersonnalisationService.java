package com.financedomain.personnalisation.service;

import com.financedomain.personnalisation.dto.DefaultServicesDto;
import com.financedomain.personnalisation.model.DefaultServiceConfig;
import com.financedomain.personnalisation.proxy.PythonPersonalizationProxy;
import com.financedomain.personnalisation.repository.DefaultServiceConfigRepository;
import lombok.extern.slf4j.Slf4j;
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


    private final PythonPersonalizationProxy pythonProxy;

    private final DefaultServiceConfigRepository configRepository;

    public PersonnalisationService(PythonPersonalizationProxy pythonProxy, DefaultServiceConfigRepository configRepository) {
        this.pythonProxy = pythonProxy;
        this.configRepository = configRepository;
    }

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
        result.add(buildTelcoDto());
        result.add(buildOmyDto());
        return result;
    }

    private DefaultServicesDto buildTelcoDto() {
        Optional<DefaultServiceConfig> telcoOpt = configRepository.findByUniverse(TELCO);
        if (telcoOpt.isPresent()) {
            DefaultServiceConfig config = telcoOpt.get();
            return new DefaultServicesDto(
                TELCO,
                config.getServiceId1(), 
                config.getServiceId2(),
                getOrDefault(config.getAdvServiceId1(), APPEL),
                getOrDefault(config.getAdvServiceId2(), INTERNET),
                getOrDefault(config.getAdvServiceId3(), ILLIMIX),
                getOrDefault(config.getAdvServiceId4(), ILLIFLEX),
                getOrDefault(config.getAdvServiceId5(), APPEL)
            );
        }
        return new DefaultServicesDto(
            TELCO, APPEL, INTERNET, APPEL, INTERNET, ILLIMIX, ILLIFLEX, APPEL
        );
    }

    private DefaultServicesDto buildOmyDto() {
        Optional<DefaultServiceConfig> omyOpt = configRepository.findByUniverse("OMY");
        if (omyOpt.isPresent()) {
            DefaultServiceConfig config = omyOpt.get();
            return new DefaultServicesDto(
                "OMY", 
                config.getServiceId1(), 
                config.getServiceId2(),
                getOrDefault(config.getAdvServiceId1(), TRANSFERT),
                getOrDefault(config.getAdvServiceId2(), VOCAL),
                getOrDefault(config.getAdvServiceId3(), "DEPOT"),
                getOrDefault(config.getAdvServiceId4(), RETRAIT),
                getOrDefault(config.getAdvServiceId5(), "RAPIDO")
            );
        }
        return new DefaultServicesDto(
            "OMY", TRANSFERT, VOCAL, TRANSFERT, VOCAL, "DEPOT", RETRAIT, "RAPIDO"
        );
    }

    private String getOrDefault(String value, String defaultValue) {
        return value != null ? value : defaultValue;
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