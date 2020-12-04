package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.entity.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationEntity, String> {

    List<InvitationEntity> findByOrganizationIdAndStatus(String organizationId, String status);

    Optional<InvitationEntity> findByAcceptToken(String token);
}
