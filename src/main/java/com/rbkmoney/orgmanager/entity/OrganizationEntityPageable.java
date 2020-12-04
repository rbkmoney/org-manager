package com.rbkmoney.orgmanager.entity;

import com.rbkmoney.swag.organizations.model.Organization;
import lombok.Data;

import java.util.List;

@Data
public class OrganizationEntityPageable {
    private final String continuationToken;
    private final int limit;
    private final List<Organization> organizations;
}
