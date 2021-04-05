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
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private final MemberRoleService memberRoleService;

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
        OrganizationEntity organizationEntity = organizationRepository.getOne(orgId);
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


    @Transactional
    public ResponseEntity<Member> getMember(String userId) {
        Optional<MemberEntity> entity = memberRepository.findById(userId);

        if (entity.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Member member = memberConverter.toDomain(entity.get());

        return ResponseEntity.ok(member);
    }

    @Transactional
    public ResponseEntity<MemberRole> assignMemberRole(String orgId, String userId, MemberRole memberRole) {
        Optional<MemberEntity> memberEntityOptional = memberRepository.findById(userId);
        if (memberEntityOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        MemberRoleEntity memberRoleEntity = memberRoleConverter.toEntity(memberRole, orgId);
        memberEntityOptional.get().getRoles().add(memberRoleEntity);
        MemberRole assignedRole = memberRoleConverter.toDomain(memberRoleEntity);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assignedRole);
    }

    @Transactional
    public void expelOrgMember(String orgId, String userId) {
        OrganizationEntity organization = organizationRepository.findById(orgId)
                .orElseThrow(ResourceNotFoundException::new);
        MemberEntity member = organization.getMembers().stream()
                .filter(memberEntity -> memberEntity.getId().equals(userId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);

        Optional<MemberRoleEntity> memberRole = member.getRoles().stream()
                .filter(memberRoleEntity -> memberRoleEntity.getOrganizationId().equals(orgId))
                .findFirst();
        organization.getMembers().remove(member);
        memberRole.ifPresent(memberRoleEntity -> {
            member.getRoles().remove(memberRoleEntity);
            memberRoleService.delete(memberRoleEntity.getId());
        });
    }

    @Transactional
    public ResponseEntity<Void> removeMemberRole(String orgId, String userId, String memberRoleId) {
        Optional<MemberEntity> memberEntityOptional = memberRepository.findById(userId);
        if (memberEntityOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        MemberEntity memberEntity = memberEntityOptional.get();
        MemberRole memberRoleToDelete = memberRoleService.findById(memberRoleId);
        memberEntity.getRoles()
                .removeIf(
                        memberRoleEntity -> memberRoleConverter.toDomain(memberRoleEntity).equals(memberRoleToDelete));
        memberRoleService.delete(memberRoleId);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    public ResponseEntity<MemberOrgListResult> listMembers(String orgId) {
        Optional<OrganizationEntity> entity = organizationRepository.findById(orgId);

        if (entity.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        List<Member> members = entity.get().getMembers()
                .stream()
                .map(memberConverter::toDomain)
                .collect(toList());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new MemberOrgListResult()
                        .result(members));
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
    public ResponseEntity<OrganizationMembership> joinOrganization(String token, String userId, String userEmail) {
        Optional<InvitationEntity> invitationEntityOptional = invitationRepository.findByAcceptToken(token);

        if (invitationEntityOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        InvitationEntity invitationEntity = invitationEntityOptional.get();

        if (invitationEntity.isExpired()) {
            throw new InviteExpiredException(invitationEntity.getExpiresAt().toString());
        }

        Optional<OrganizationEntity> organizationEntityOptional =
                organizationRepository.findById(invitationEntity.getOrganizationId());

        if (organizationEntityOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }


        invitationEntity.setAcceptedAt(LocalDateTime.now());
        invitationEntity.setAcceptedMemberId(userId);
        invitationEntity.setStatus(InvitationStatusName.ACCEPTED.getValue());

        OrganizationEntity organizationEntity = organizationEntityOptional.get();

        MemberEntity memberEntity = findOrCreateMember(userId, userEmail, invitationEntity.getInviteeRoles());
        invitationEntity.getInviteeRoles().addAll(invitationEntity.getInviteeRoles());

        organizationEntity.getMembers().add(memberEntity);

        OrganizationMembership organizationMembership = new OrganizationMembership();
        organizationMembership.setMember(memberConverter.toDomain(memberEntity));
        organizationMembership.setOrg(organizationConverter.toDomain(organizationEntity));

        return ResponseEntity.ok(organizationMembership);
    }

    private MemberEntity findOrCreateMember(String userId, String userEmail, Set<MemberRoleEntity> inviteeRoles) {
        Optional<MemberEntity> memberEntityOptional = memberRepository.findById(userId);
        if (memberEntityOptional.isEmpty()) {
            return memberRepository.save(
                    MemberEntity.builder()
                            .id(userId)
                            .roles(inviteeRoles)
                            .email(userEmail)
                            .build());
        }
        return memberEntityOptional.get();
    }

    public String getOrgIdByInvitationToken(String token) {
        InvitationEntity invitationEntity = invitationRepository.findByAcceptToken(token)
                .orElseThrow(ResourceNotFoundException::new);
        if (invitationEntity.isExpired()) {
            throw new InviteExpiredException(invitationEntity.getExpiresAt().toString());
        }
        OrganizationEntity organizationEntity = organizationRepository.findById(invitationEntity.getOrganizationId())
                .orElseThrow(ResourceNotFoundException::new);
        return organizationEntity.getId();
    }

}
