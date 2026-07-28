package com.financedomain.personnalisation.repository;

import com.financedomain.personnalisation.model.DefaultServiceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DefaultServiceConfigRepository extends JpaRepository<DefaultServiceConfig, Long> {
    Optional<DefaultServiceConfig> findByUniverse(String universe);
}
