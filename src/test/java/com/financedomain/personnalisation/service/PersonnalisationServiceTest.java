package com.financedomain.personnalisation.service;

import com.financedomain.personnalisation.dto.DefaultServicesDto;
import com.financedomain.personnalisation.model.DefaultServiceConfig;
import com.financedomain.personnalisation.proxy.PythonPersonalizationProxy;
import com.financedomain.personnalisation.repository.DefaultServiceConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonnalisationServiceTest {

    @Mock
    private PythonPersonalizationProxy pythonProxy;

    @Mock
    private DefaultServiceConfigRepository configRepository;

    @InjectMocks
    private PersonnalisationService personnalisationService;

    private DefaultServiceConfig mockTelcoConfig;
    private DefaultServiceConfig mockOmyConfig;

    @BeforeEach
    void setUp() {
        mockTelcoConfig = new DefaultServiceConfig();
        mockTelcoConfig.setUniverse("TELCO");
        mockTelcoConfig.setServiceId1("SERVICE_CUSTOM_1");
        mockTelcoConfig.setServiceId2("SERVICE_CUSTOM_2");
        mockTelcoConfig.setAdvServiceId1("ADV_1");

        mockOmyConfig = new DefaultServiceConfig();
        mockOmyConfig.setUniverse("OMY");
        mockOmyConfig.setServiceId1("OMY_CUSTOM_1");
        mockOmyConfig.setServiceId2("OMY_CUSTOM_2");
    }

    @Test
    @DisplayName("Devrait récupérer les usages client via le proxy Python")
    void shouldFetchClientUsagesFromPythonProxy() {
        String msisdn = "771234567";
        List<Map<String, Object>> mockUsages = new ArrayList<>();
        when(pythonProxy.getClientUsages(msisdn)).thenReturn(mockUsages);

        Object result = personnalisationService.getClientUsages(msisdn);

        assertNotNull(result);
        assertEquals(mockUsages, result);
        verify(pythonProxy).getClientUsages(msisdn);
    }

    @Test
    @DisplayName("Devrait récupérer les statistiques globales via le proxy Python")
    void shouldFetchGlobalStatsFromPythonProxy() {
        Map<String, Object> mockStats = new HashMap<>();
        when(pythonProxy.getGlobalStats()).thenReturn(mockStats);

        Object result = personnalisationService.getGlobalStats();

        assertNotNull(result);
        assertEquals(mockStats, result);
        verify(pythonProxy).getGlobalStats();
    }

    @Test
    @DisplayName("Devrait retourner la configuration des services par défaut enregistrée en base")
    void shouldReturnDefaultServicesWhenConfigPresentInDb() {
        when(configRepository.findByUniverse("TELCO")).thenReturn(Optional.of(mockTelcoConfig));
        when(configRepository.findByUniverse("OMY")).thenReturn(Optional.of(mockOmyConfig));

        List<DefaultServicesDto> result = personnalisationService.getDefaultServices();

        assertNotNull(result);
        assertEquals(2, result.size());

        DefaultServicesDto telcoDto = result.stream()
                .filter(d -> "TELCO".equals(d.getUniverse()))
                .findFirst()
                .orElse(null);
        assertNotNull(telcoDto);
        assertEquals("SERVICE_CUSTOM_1", telcoDto.getServiceId1());
        assertEquals("ADV_1", telcoDto.getAdvServiceId1());

        DefaultServicesDto omyDto = result.stream()
                .filter(d -> "OMY".equals(d.getUniverse()))
                .findFirst()
                .orElse(null);
        assertNotNull(omyDto);
        assertEquals("OMY_CUSTOM_1", omyDto.getServiceId1());
    }

    @Test
    @DisplayName("Devrait retourner les valeurs par défaut de secours si la base de données est vide")
    void shouldReturnDefaultFallbackServicesWhenDbEmpty() {
        when(configRepository.findByUniverse("TELCO")).thenReturn(Optional.empty());
        when(configRepository.findByUniverse("OMY")).thenReturn(Optional.empty());

        List<DefaultServicesDto> result = personnalisationService.getDefaultServices();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Devrait enregistrer les services par défaut avec succès")
    void shouldSaveDefaultServices() {
        List<DefaultServicesDto> dtos = new ArrayList<>();
        DefaultServicesDto dto = new DefaultServicesDto();
        dto.setUniverse("TELCO");
        dto.setServiceId1("NEW_SERVICE_1");
        dtos.add(dto);

        when(configRepository.findByUniverse("TELCO")).thenReturn(Optional.of(mockTelcoConfig));
        when(configRepository.save(any(DefaultServiceConfig.class))).thenReturn(mockTelcoConfig);

        personnalisationService.saveDefaultServices(dtos);

        verify(configRepository).findByUniverse("TELCO");
        verify(configRepository).save(any(DefaultServiceConfig.class));
    }
}
