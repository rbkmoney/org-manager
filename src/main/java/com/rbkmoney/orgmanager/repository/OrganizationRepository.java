package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String> {

    @Query(value = "SELECT * FROM org_manager.organization AS o WHERE o.id < ?1 ORDER BY o.id DESC LIMIT ?2",
            nativeQuery = true)
    List<OrganizationEntity> fetchAll(String continuationId, int limit);

}
