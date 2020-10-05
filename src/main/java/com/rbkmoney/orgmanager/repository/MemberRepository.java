package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<OrganizationEntity, String> {
}
