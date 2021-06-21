package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.converter.MemberConverter;
import com.rbkmoney.orgmanager.converter.MemberRoleConverter;
import com.rbkmoney.orgmanager.converter.OrganizationConverter;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.exception.AccessDeniedException;
import com.rbkmoney.orgmanager.exception.LastRoleException;
import com.rbkmoney.orgmanager.exception.ResourceNotFoundException;
import com.rbkmoney.orgmanager.repository.MemberRepository;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.orgmanager.service.dto.MemberWithRoleDto;
import com.rbkmoney.swag.organizations.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    public static final Integer DEFAULT_ORG_LIMIT = 20;

    private final OrganizationConverter organizationConverter;
    private final OrganizationRepository organizationRepository;
    private final MemberConverter memberConverter;
    private final MemberRoleConverter memberRoleConverter;
    private final MemberRepository memberRepository;
    private final InvitationService invitationService;
    private final MemberRoleService memberRoleService;

    // TODO [a.romanov]: idempotency
    public ResponseEntity<Organization> create(
            String ownerId,
            Organization organization,
            String idempotencyKey) {
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
        List<MemberRoleEntity> rolesInOrg = getMemberRolesInOrg(orgId, memberEntity);
        return memberConverter.toDomain(memberEntity, rolesInOrg);
    }

    private List<MemberRoleEntity> getMemberRolesInOrg(String orgId, MemberEntity memberEntity) {
        return memberEntity.getRoles().stream()
                .filter(memberRole -> isActiveOrgMemberRole(orgId, memberRole))
                .collect(toList());
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
        deactivateOrgMemberRoles(orgId, member);
        member.getRoles()
                .removeIf(memberRoleEntity -> memberRoleEntity.getOrganizationId().equals(orgId));
        organization.getMembers().remove(member);
    }

    private void deactivateOrgMemberRoles(String orgId, MemberEntity member) {
        member.getRoles()
                .stream()
                .filter(memberRoleEntity -> memberRoleEntity.getOrganizationId().equals(orgId))
                .forEach(memberRoleEntity -> memberRoleEntity.setActive(Boolean.FALSE));
    }

    @Transactional
    public void removeMemberRole(String orgId, String userId, String memberRoleId) {
        OrganizationEntity organization = findById(orgId);
        MemberEntity member = getMember(userId, organization);
        if (getMemberRolesInOrg(orgId, member).size() == 1) {
            throw new LastRoleException();
        }
        MemberRoleEntity roleToRemove = memberRoleService.findEntityById(memberRoleId);
        roleToRemove.setActive(Boolean.FALSE);
        member.getRoles().remove(roleToRemove);
    }

    @Transactional(readOnly = true)
    public MemberOrgListResult listMembers(String orgId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException();
        }
        List<MemberWithRoleDto> orgMemberList = memberRepository.getOrgMemberList(orgId);
        List<Member> members = memberConverter.toDomain(orgMemberList);
        return new MemberOrgListResult()
                .result(members);
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
        if (StringUtils.hasLength(continuationId)) {
            return organizationRepository.findAllByMember(userId, continuationId);
        }
        return organizationRepository.findAllByMember(userId);
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
        InvitationEntity invitationEntity = invitationService.findByToken(token);
        if (!userEmail.equals(invitationEntity.getInviteeContactEmail())) {
            log.error("joinOrganization() - error: user email = {} doesn't equals invitee email = {}",
                    userEmail, invitationEntity.getInviteeContactEmail());
            throw new AccessDeniedException(
                    String.format("Access denied. User email %s doesn't match invite", userEmail));
        }
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

    @Transactional(readOnly = true)
    public String getOrgIdByInvitationToken(String token) {
        InvitationEntity invitationEntity = invitationService.findByToken(token);
        OrganizationEntity organizationEntity = findById(invitationEntity.getOrganizationId());
        return organizationEntity.getId();
    }

    public OrganizationEntity findById(String orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Set<OrganizationEntity> findByOwner(String ownerId) {
        return organizationRepository.findAllByOwner(ownerId);
    }

}
