package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String> {

    List<RoleEntity> findByOrganizationId(String organizationId);
}
