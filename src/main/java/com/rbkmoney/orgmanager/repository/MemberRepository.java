package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.service.dto.MemberWithRoleDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, String> {


    @Query(value = "SELECT m.id, " +
            "              m.email,  " +
            "              mr.id as memberRoleId, " +
            "              mr.organization_id as organizationId, " +
            "              mr.role_id as roleId, " +
            "              mr.scope_id as scopeId, " +
            "              mr.resource_id as resourceId" +
            " FROM org_manager.member_to_member_role mtmr,  " +
            "     org_manager.member_to_organization mto,  " +
            "     org_manager.member_role mr, " +
            "     org_manager.member m  " +
            " WHERE  " +
            "         mto.organization_id = ?1 " +
            "     AND mto .member_id = m.id " +
            "     AND mr.active = 'true' " +
            "     AND mr.id = mtmr.member_role_id " +
            "     AND mr.organization_id = mto.organization_id " +
            "     AND m.id = mtmr.member_id ", nativeQuery = true)
    List<MemberWithRoleDto> getOrgMemberList(String orgId);


}
