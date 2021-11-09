package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.entity.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationEntity, String> {

    List<InvitationEntity> findByOrganizationIdAndStatus(String organizationId, String status);

    List<InvitationEntity> findByOrganizationId(String organizationId);

    Optional<InvitationEntity> findByAcceptToken(String token);

    @QueryHints(value = {
            @QueryHint(name = HINT_FETCH_SIZE, value = "10")
    })
    @Query("select i from InvitationEntity i where i.status = 'Pending'")
    Stream<InvitationEntity> findAllPendingStatus();


    Optional<InvitationEntity> findByIdAndOrganizationId(String id, String orgId);

}
