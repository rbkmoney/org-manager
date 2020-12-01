package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.converter.MemberConverter;
import com.rbkmoney.orgmanager.converter.OrganizationConverter;
import com.rbkmoney.orgmanager.entity.InvitationEntity;
import com.rbkmoney.orgmanager.entity.MemberEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntity;
import com.rbkmoney.orgmanager.entity.OrganizationEntityPageable;
import com.rbkmoney.orgmanager.repository.InvitationRepository;
import com.rbkmoney.orgmanager.repository.MemberRepository;
import com.rbkmoney.orgmanager.repository.OrganizationRepository;
import com.rbkmoney.swag.organizations.model.InlineResponse2001;
import com.rbkmoney.swag.organizations.model.Member;
import com.rbkmoney.swag.organizations.model.Organization;
import com.rbkmoney.swag.organizations.model.OrganizationMembership;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    public static final Integer DEFAULT_ORG_LIMIT = 20;

    private final OrganizationConverter organizationConverter;
    private final OrganizationRepository organizationRepository;
    private final MemberConverter memberConverter;
    private final MemberRepository memberRepository;
    private final InvitationRepository invitationRepository;

    @Transactional(readOnly = true)
    public Optional<OrganizationEntity> findById(String orgId) {
        Optional<OrganizationEntity> organizationEntityOptional = organizationRepository.findById(orgId);
        if (organizationEntityOptional.isPresent()) {
            OrganizationEntity organizationEntity = organizationEntityOptional.get();
            Hibernate.initialize(organizationEntity.getMembers());
            Hibernate.initialize(organizationEntity.getRoles());

            return Optional.of(organizationEntity);
        }
        return Optional.empty();
    }

    // TODO [a.romanov]: idempotency
    public ResponseEntity<Organization> create(
            Organization organization,
            String xIdempotencyKey) {
        OrganizationEntity entity = organizationConverter.toEntity(organization);
        OrganizationEntity savedEntity = organizationRepository.save(entity);

        Organization savedOrganization = organizationConverter.toDomain(savedEntity);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedOrganization);
    }

    public ResponseEntity<Organization> get(String orgId) {
        Optional<OrganizationEntity> entity = organizationRepository.findById(orgId);

        if (entity.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Organization organization = organizationConverter.toDomain(entity.get());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(organization);
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
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(member);
    }

    @Transactional
    public ResponseEntity<InlineResponse2001> listMembers(String orgId) {
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
                .body(new InlineResponse2001()
                        .results(members));

    }

    public OrganizationEntityPageable findAllOrganizations(int limit) {
        if (limit == 0) {
            limit = DEFAULT_ORG_LIMIT;
        }
        Page<OrganizationEntity> organizationEntitiesPage = organizationRepository.findAll(PageRequest.of(0, limit, Sort.by("id").descending()));
        List<OrganizationEntity> organizationEntities = organizationEntitiesPage.getContent();
        String continuationToken = null;
        if (organizationEntitiesPage.hasNext()) {
            continuationToken = organizationEntities.get(organizationEntities.size() - 1).getId();
        }
        List<Organization> organizations = organizationEntities
                .stream().map(organizationConverter::toDomain)
                .collect(toList());


        return new OrganizationEntityPageable(
                continuationToken,
                limit,
                organizations);
    }

    public OrganizationEntityPageable findAllOrganization(String continuationId, int limit) {
        if (limit == 0) {
            limit = DEFAULT_ORG_LIMIT;
        }
        List<OrganizationEntity> organizationEntities = organizationEntities = organizationRepository.fetchAll(continuationId, limit);
        String continuationToken = null;
        if (organizationEntities.size() > 1 && organizationEntities.size() > limit) {
            continuationToken = organizationEntities.get(organizationEntities.size() - 1).getId();
        }
        List<Organization> organizations = organizationEntities
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

        if (organizationEntityOptional.isEmpty()) return ResponseEntity.notFound().build();

        Optional<MemberEntity> memberEntityOptional = memberRepository.findById(userId);

        if (memberEntityOptional.isEmpty()) return ResponseEntity.notFound().build();

        organizationEntityOptional.get().getMembers()
                .removeIf(memberEntity -> memberEntity.getId().equals(memberEntityOptional.get().getId()));

        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<OrganizationMembership> getMembership(String orgId, String userId, String userEmail) {
        Optional<OrganizationEntity> organizationEntityOptional = organizationRepository.findById(orgId);

        if (organizationEntityOptional.isEmpty()) return ResponseEntity.notFound().build();

        Optional<MemberEntity> memberEntityOptional = memberRepository.findById(userId);

        if (memberEntityOptional.isEmpty()) return ResponseEntity.notFound().build();

        OrganizationMembership organizationMembership = new OrganizationMembership();
        organizationMembership.setMember(memberConverter.toDomain(memberEntityOptional.get()));
        organizationMembership.setOrg(organizationConverter.toDomain(organizationEntityOptional.get()));

        return ResponseEntity.ok(organizationMembership);
    }

    @Transactional
    public ResponseEntity<OrganizationMembership> joinOrganization(String token, String userId, String userEmail) {
        Optional<InvitationEntity> invitationEntityOptional = invitationRepository.findByAcceptToken(token);

        if (invitationEntityOptional.isEmpty()) return ResponseEntity.notFound().build();

        InvitationEntity invitationEntity = invitationEntityOptional.get();

        Optional<OrganizationEntity> organizationEntityOptional = organizationRepository.findById(invitationEntity.getOrganizationId());

        if (organizationEntityOptional.isEmpty()) return ResponseEntity.notFound().build();

        OrganizationEntity organizationEntity = organizationEntityOptional.get();

        MemberEntity memberEntity = findOrCreateMember(userId, userEmail);

        organizationEntity.getMembers().add(memberEntity);

        OrganizationMembership organizationMembership = new OrganizationMembership();
        organizationMembership.setMember(memberConverter.toDomain(memberEntity));
        organizationMembership.setOrg(organizationConverter.toDomain(organizationEntity));

        return ResponseEntity.ok(organizationMembership);
    }

    private MemberEntity findOrCreateMember(String userId, String userEmail) {
        Optional<MemberEntity> memberEntityOptional = memberRepository.findById(userId);
        if (memberEntityOptional.isEmpty()) {
            return memberRepository.save(new MemberEntity(userId, Collections.emptySet(), userEmail));
        }
        return memberEntityOptional.get();
    }

}
