package com.financedomain.personnalisation.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "default_service_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultServiceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String universe; // "OMY" or "TELCO"

    @Column(name = "service_id_1", nullable = false)
    private String serviceId1;

    @Column(name = "service_id_2", nullable = false)
    private String serviceId2;

    @Column(name = "adv_service_id_1")
    private String advServiceId1;

    @Column(name = "adv_service_id_2")
    private String advServiceId2;

    @Column(name = "adv_service_id_3")
    private String advServiceId3;

    @Column(name = "adv_service_id_4")
    private String advServiceId4;

    @Column(name = "adv_service_id_5")
    private String advServiceId5;
}
