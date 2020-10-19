package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.entity.OrganizationRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRoleRepository extends JpaRepository<OrganizationRoleEntity, String> {

    Optional<OrganizationRoleEntity> findByOrganizationIdAndRoleId(String organizationId, String roleId);
}