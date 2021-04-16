package com.rbkmoney.orgmanager.service;

import com.rbkmoney.orgmanager.converter.MemberRoleConverter;
import com.rbkmoney.orgmanager.entity.MemberRoleEntity;
import com.rbkmoney.orgmanager.exception.ResourceNotFoundException;
import com.rbkmoney.orgmanager.repository.MemberRoleRepository;
import com.rbkmoney.swag.organizations.model.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberRoleServiceImpl implements MemberRoleService {

    private final MemberRoleRepository repository;
    private final MemberRoleConverter converter;

    @Override
    @Transactional(readOnly = true)
    public MemberRole findById(String id) {
        return repository.findById(id)
                .map(converter::toDomain)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberRoleEntity getById(String id) {
        return repository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }

}
