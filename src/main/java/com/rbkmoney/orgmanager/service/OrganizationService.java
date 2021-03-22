package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.controller.error.InviteExpiredException;
import com.rbkmoney.orgmanager.converter.MemberConverter;
import com.rbkmoney.orgmanager.converter.MemberRoleConverter;
import com.rbkmoney.orgmanager.converter.OrganizationConverter;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntityPageable;
import com.rbkmoney.orgmanager.exception.ResourceNotFoundException;
import com.rbkmoney.orgmanager.repository.InvitationRepository;
import com.rbkmoney.orgmanager.repository.MemberRepository;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.swag.organizations.model.InvitationStatusName;
import com.rbkmoney.swag.organizations.model.Member;
import com.rbkmoney.swag.organizations.model.MemberOrgListResult;
import com.rbkmoney.swag.organizations.model.MemberRole;
import com.rbkmoney.swag.organizations.model.Organization;
import com.rbkmoney.swag.organizations.model.OrganizationMembership;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public Optional<OrganizationEntity> findById(String orgId) {
        Optional<OrganizationEntity> organizationEntityOptional = organizationRepository.findById(orgId);
        if (organizationEntityOptional.isPresent()) {
            OrganizationEntity organizationEntity = organizationEntityOptional.get();
            Hibernate.initialize(organizationEntity.getMembers());
            for (MemberEntity member : organizationEntity.getMembers()) {
                Hibernate.initialize(member.getRoles());
            }
            Hibernate.initialize(organizationEntity.getRoles());

            return Optional.of(organizationEntity);
        }
        return Optional.empty();
    }

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
    public ResponseEntity<Void> assignMemberRole(String orgId, String userId, MemberRole memberRole) {
        Optional<MemberEntity> memberEntityOptional = memberRepository.findById(userId);

        if (memberEntityOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        MemberRoleEntity memberRoleEntity = memberRoleConverter.toEntity(memberRole, orgId);
        memberEntityOptional.get().getRoles().add(memberRoleEntity);

        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<Void> expelOrgMember(String orgId, String userId) {
        Optional<OrganizationEntity> organizationEntityOptional = organizationRepository.findById(orgId);

        if (organizationEntityOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        organizationEntityOptional.get().getMembers().removeIf(memberEntity -> memberEntity.getId().equals(userId));

        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<Void> removeMemberRole(String orgId, String userId, MemberRole memberRole) {
        Optional<MemberEntity> memberEntityOptional = memberRepository.findById(userId);

        if (memberEntityOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        memberEntityOptional.get().getRoles().removeIf(memberRoleEntity -> {
            return memberRoleConverter.toDomain(memberRoleEntity).equals(memberRole);
        });

        return ResponseEntity.ok().build();
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

    public OrganizationEntityPageable findAllOrganizations(Integer limit, String userId) {
        if (limit == null || limit == 0) {
            limit = DEFAULT_ORG_LIMIT;
        }
        List<OrganizationEntity> entities = organizationRepository.findAllByMember(userId);
        List<OrganizationEntity> limitEntities = limitOrganizations(limit, entities);
        String continuationToken = getContinuationId(entities, limitEntities);
        List<Organization> organizations = limitEntities.stream()
                .map(organizationConverter::toDomain)
                .collect(toList());
        return new OrganizationEntityPageable(
                continuationToken,
                limit,
                organizations);
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

    public OrganizationEntityPageable findAllOrganizations(String continuationId, int limit, String userId) {
        if (limit == 0) {
            limit = DEFAULT_ORG_LIMIT;
        }
        List<OrganizationEntity> entities = organizationRepository.findAllByMember(userId, continuationId);
        List<OrganizationEntity> limitEntities = limitOrganizations(limit, entities);
        String continuationToken = getContinuationId(entities, limitEntities);
        List<Organization> organizations = limitEntities
                .stream().map(organizationConverter::toDomain)
                .collect(toList());

        return new OrganizationEntityPageable(
                continuationToken,
                limit,
                organizations);
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

    public static void main(String[] args) {
        String s1 = "abc";
        String s2 = "abf";
        System.out.println(s1.compareTo(s2));
    }

}
