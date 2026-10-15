package no.aurel.backend.servicegroup;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceGroupRepository extends JpaRepository<ServiceGroup, Long> {

    long countByIdIn(Collection<Long> ids);
}
