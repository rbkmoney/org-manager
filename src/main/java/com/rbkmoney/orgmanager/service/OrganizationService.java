package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.controller.error.InviteExpiredException;
import com.rbkmoney.orgmanager.converter.MemberConverter;
import com.rbkmoney.orgmanager.converter.MemberRoleConverter;
import com.rbkmoney.orgmanager.converter.OrganizationConverter;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.exception.ResourceNotFoundException;
import com.rbkmoney.orgmanager.repository.InvitationRepository;
import com.rbkmoney.orgmanager.repository.MemberRepository;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.swag.organizations.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    public static final Integer DEFAULT_ORG_LIMIT = 20;

    private final OrganizationConverter organizationConverter;
    private final OrganizationRepository organizationRepository;
    private final MemberConverter memberConverter;
    private final MemberRoleConverter memberRoleConverter;
    private final MemberRepository memberRepository;
    private final InvitationRepository invitationRepository;

    // TODO [a.romanov]: idempotency
    public ResponseEntity<Organization> create(
            String ownerId,
            Organization organization,
            String xIdempotencyKey) {
        OrganizationEntity entity = organizationConverter.toEntity(organization, ownerId);
        OrganizationEntity savedEntity = organizationRepository.save(entity);

        Organization savedOrganization = organizationConverter.toDomain(savedEntity);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedOrganization);
    }

    @Transactional
    public ResponseEntity<Organization> modify(String orgId, String orgName) {
        OrganizationEntity organizationEntity = findById(orgId);
        organizationEntity.setName(orgName);
        Organization savedOrganization = organizationConverter.toDomain(organizationEntity);

        return ResponseEntity.ok(savedOrganization);
    }

    public ResponseEntity<Organization> get(String orgId) {
        Optional<OrganizationEntity> entity = organizationRepository.findById(orgId);

        if (entity.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Organization organization = organizationConverter.toDomain(entity.get());

        return ResponseEntity.ok(organization);
    }


    @Transactional(readOnly = true)
    public Member getOrgMember(String userId, String orgId) {
        OrganizationEntity organization = findById(orgId);
        MemberEntity memberEntity = getMember(userId, organization);
        List<MemberRoleEntity> rolesInOrg = memberEntity.getRoles().stream()
                .filter(memberRole -> isActiveOrgMemberRole(orgId, memberRole))
                .collect(toList());
        return memberConverter.toDomain(memberEntity, rolesInOrg);
    }

    private boolean isActiveOrgMemberRole(String orgId, MemberRoleEntity memberRole) {
        return memberRole.getOrganizationId().equals(orgId) && memberRole.isActive();
    }


    private MemberEntity getMember(String userId, OrganizationEntity organization) {
        return organization.getMembers().stream()
                .filter(memberEntity -> memberEntity.getId().equals(userId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public MemberRole assignMemberRole(String orgId, String userId, MemberRole memberRole) {
        OrganizationEntity organization = findById(orgId);
        MemberEntity memberEntity = getMember(userId, organization);
        MemberRoleEntity memberRoleEntity = memberRoleConverter.toEntity(memberRole, orgId);
        memberEntity.getRoles().add(memberRoleEntity);
        return memberRoleConverter.toDomain(memberRoleEntity);
    }

    @Transactional
    public void expelOrgMember(String orgId, String userId) {
        OrganizationEntity organization = findById(orgId);
        MemberEntity member = getMember(userId, organization);
        deactivateOrgMemberRole(orgId, member);
        member.getRoles()
                .removeIf(memberRoleEntity -> memberRoleEntity.getOrganizationId().equals(orgId));
        organization.getMembers().remove(member);
    }

    private void deactivateOrgMemberRole(String orgId, MemberEntity member) {
        member.getRoles()
                .stream()
                .filter(memberRoleEntity -> memberRoleEntity.getOrganizationId().equals(orgId))
                .forEach(memberRoleEntity -> memberRoleEntity.setActive(Boolean.FALSE));
    }

    @Transactional
    public void removeMemberRole(String orgId, String userId, String memberRoleId) {
        OrganizationEntity organization = findById(orgId);
        MemberEntity member = getMember(userId, organization);
        member.getRoles()
                .removeIf(memberRoleEntity -> memberRoleEntity.getId().equals(memberRoleId));
    }

    @Transactional(readOnly = true)
    public MemberOrgListResult listMembers(String orgId) {
        OrganizationEntity entity = findById(orgId);
        List<Member> members = getOrgMembersWithActiveRole(entity);
        return new MemberOrgListResult()
                .result(members);
    }

    private List<Member> getOrgMembersWithActiveRole(OrganizationEntity entity) {
        return entity.getMembers().stream()
                .map(memberEntity -> {
                    List<MemberRoleEntity> rolesInOrg = memberEntity.getRoles().stream()
                            .filter(memberRole -> isActiveOrgMemberRole(entity.getId(), memberRole))
                            .collect(toList());
                    return memberConverter.toDomain(memberEntity, rolesInOrg);
                })
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public OrganizationSearchResult findAllOrganizations(String userId, Integer limit, String continuationId) {
        if (limit == null || limit == 0) {
            limit = DEFAULT_ORG_LIMIT;
        }
        List<OrganizationEntity> entities = getOrganizationsByUser(continuationId, userId);
        List<OrganizationEntity> limitEntities = limitOrganizations(limit, entities);
        String continuationToken = getContinuationId(entities, limitEntities);
        List<Organization> organizations = limitEntities
                .stream().map(organizationConverter::toDomain)
                .collect(toList());

        return new OrganizationSearchResult()
                .continuationToken(continuationToken)
                .result(organizations);
    }

    private List<OrganizationEntity> getOrganizationsByUser(String continuationId, String userId) {
        if (StringUtils.isEmpty(continuationId)) {
            return organizationRepository.findAllByMember(userId);
        }
        return organizationRepository.findAllByMember(userId, continuationId);
    }

    private List<OrganizationEntity> limitOrganizations(Integer limit,
                                                        List<OrganizationEntity> entities) {
        if (limit >= entities.size()) {
            return entities;
        }
        return entities.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private String getContinuationId(List<OrganizationEntity> entities, List<OrganizationEntity> limitEntities) {
        if (limitEntities.size() == entities.size()) {
            return null;
        }
        return limitEntities.get(limitEntities.size() - 1).getId();
    }

    @Transactional
    public ResponseEntity<Void> cancelOrgMembership(String orgId, String userId, String userEmail) {
        Optional<OrganizationEntity> organizationEntityOptional = organizationRepository.findById(orgId);

        if (organizationEntityOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<MemberEntity> memberEntityOptional = memberRepository.findById(userId);

        if (memberEntityOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        organizationEntityOptional.get().getMembers()
                .removeIf(memberEntity -> memberEntity.getId().equals(memberEntityOptional.get().getId()));

        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<OrganizationMembership> getMembership(String orgId, String userId, String userEmail) {
        Optional<OrganizationEntity> organizationEntityOptional = organizationRepository.findById(orgId);

        if (organizationEntityOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<MemberEntity> memberEntityOptional = memberRepository.findById(userId);

        if (memberEntityOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OrganizationMembership organizationMembership = new OrganizationMembership();
        organizationMembership.setMember(memberConverter.toDomain(memberEntityOptional.get()));
        organizationMembership.setOrg(organizationConverter.toDomain(organizationEntityOptional.get()));

        return ResponseEntity.ok(organizationMembership);
    }

    @Transactional
    public OrganizationMembership joinOrganization(String token, String userId, String userEmail) {
        InvitationEntity invitationEntity = getInvitationByToken(token);
        OrganizationEntity organizationEntity = findById(invitationEntity.getOrganizationId());
        MemberEntity memberEntity = findOrCreateMember(userId, userEmail);
        memberEntity.getRoles().addAll(invitationEntity.getInviteeRoles());
        organizationEntity.getMembers().add(memberEntity);
        acceptInvitation(userId, invitationEntity);
        OrganizationMembership organizationMembership = new OrganizationMembership();
        organizationMembership
                .setMember(memberConverter.toDomain(memberEntity, new ArrayList<>(invitationEntity.getInviteeRoles())));
        organizationMembership.setOrg(organizationConverter.toDomain(organizationEntity));
        return organizationMembership;
    }

    private InvitationEntity getInvitationByToken(String token) {
        InvitationEntity invitationEntity = invitationRepository.findByAcceptToken(token)
                .orElseThrow(ResourceNotFoundException::new);
        if (invitationEntity.isExpired()) {
            throw new InviteExpiredException(invitationEntity.getExpiresAt().toString());
        }
        return invitationEntity;
    }

    private MemberEntity findOrCreateMember(String userId, String userEmail) {
        return memberRepository.findById(userId)
                .orElseGet(() -> {
                    MemberEntity entity = new MemberEntity();
                    entity.setId(userId);
                    entity.setEmail(userEmail);
                    return entity;
                });

    }

    private void acceptInvitation(String userId, InvitationEntity invitationEntity) {
        invitationEntity.setAcceptedAt(LocalDateTime.now());
        invitationEntity.setAcceptedMemberId(userId);
        invitationEntity.setStatus(InvitationStatusName.ACCEPTED.getValue());
    }

    public String getOrgIdByInvitationToken(String token) {
        InvitationEntity invitationEntity = getInvitationByToken(token);
        OrganizationEntity organizationEntity = findById(invitationEntity.getOrganizationId());
        return organizationEntity.getId();
    }

    public OrganizationEntity findById(String orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(ResourceNotFoundException::new);
    }

}
