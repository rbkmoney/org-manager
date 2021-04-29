package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String> {

    @Query(value =
            " SELECT * FROM org_manager.organization AS o " +
                    " WHERE o.id IN " +
                    " ( " +
                    "  SELECT mo.organization_id FROM org_manager.member_to_organization AS mo WHERE mo.member_id = ?1 " +
                    "   UNION " +
                    "  SELECT id FROM org_manager.organization WHERE owner = ?1 " +
                    " ) " +
                    " ORDER BY o.id DESC",
            nativeQuery = true)
    List<OrganizationEntity> findAllByMember(String userId);

    @Query(value =
            " SELECT * FROM org_manager.organization AS o " +
                    " WHERE o.id IN " +
                    "  ( " +
                    "    SELECT mo.organization_id FROM org_manager.member_to_organization AS mo WHERE mo.member_id = ?1 " +
                    "      UNION " +
                    "    SELECT id FROM org_manager.organization WHERE owner = ?1 " +
                    "   ) " +
                    " AND o.id < ?2 " +
                    " ORDER BY o.id DESC",
            nativeQuery = true)
    List<OrganizationEntity> findAllByMember(String userId, String continuationId);

    Set<OrganizationEntity> findAllByOwner(String owner);

}
