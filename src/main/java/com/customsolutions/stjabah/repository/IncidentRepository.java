package com.customsolutions.stjabah.repository;

import com.customsolutions.stjabah.entity.Incident;
import com.customsolutions.stjabah.entity.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findFirstByStatusNotIn(Collection<IncidentStatus> status);
}
