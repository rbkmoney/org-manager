CREATE SCHEMA IF NOT EXISTS org_manager;

CREATE TABLE IF NOT EXISTS org_manager.organization
(
    id         CHARACTER VARYING           NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name       CHARACTER VARYING           NOT NULL,
    owner      CHARACTER VARYING           NOT NULL,
    metadata   CHARACTER VARYING,
    CONSTRAINT organization_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS org_manager.invitation
(
    id                    CHARACTER VARYING           NOT NULL,
    organization_id       CHARACTER VARYING           NOT NULL,
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expires_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    accept_token          CHARACTER VARYING           NOT NULL,
    invitee_contact_type  CHARACTER VARYING           NOT NULL,
    invitee_contact_email CHARACTER VARYING           NOT NULL,
    status                CHARACTER VARYING           NOT NULL,
    revocation_reason     CHARACTER VARYING,
    metadata              CHARACTER VARYING,
    CONSTRAINT invitation_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS org_manager.member_role
(
    id              CHARACTER VARYING NOT NULL,
    organization_id CHARACTER VARYING NOT NULL,
    role_id         CHARACTER VARYING NOT NULL,
    scope_id        CHARACTER VARYING NOT NULL,
    resource_id     CHARACTER VARYING NOT NULL,
    CONSTRAINT member_role_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS org_manager.member
(
    id    CHARACTER VARYING NOT NULL,
    email CHARACTER VARYING NOT NULL,
    CONSTRAINT member_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS org_manager.organization_role
(
    id              CHARACTER VARYING NOT NULL,
    organization_id CHARACTER VARYING NOT NULL,
    role_id         CHARACTER VARYING NOT NULL,
    name            CHARACTER VARYING NOT NULL,
    CONSTRAINT organization_role_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS org_manager.organization_role
(
    id              CHARACTER VARYING NOT NULL,
    organization_id CHARACTER VARYING NOT NULL,
    role_id         CHARACTER VARYING NOT NULL,
    CONSTRAINT organization_role_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS org_manager.scope
(
    id CHARACTER VARYING NOT NULL,
    CONSTRAINT scope_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS org_manager.invitation_to_member_role
(
    invitation_id  CHARACTER VARYING NOT NULL,
    member_role_id CHARACTER VARYING NOT NULL,
    CONSTRAINT invitation_to_member_role_pkey PRIMARY KEY (invitation_id, member_role_id),
    CONSTRAINT invitation_to_member_role_invitation_fkey FOREIGN KEY (invitation_id) REFERENCES org_manager.invitation (id),
    CONSTRAINT invitation_to_member_role_member_role_fkey FOREIGN KEY (member_role_id) REFERENCES org_manager.member_role (id)
);

CREATE TABLE IF NOT EXISTS org_manager.member_to_member_role
(
    member_id      CHARACTER VARYING NOT NULL,
    member_role_id CHARACTER VARYING NOT NULL,
    CONSTRAINT member_to_member_role_pkey PRIMARY KEY (member_id, member_role_id),
    CONSTRAINT member_to_member_role_member_fkey FOREIGN KEY (member_id) REFERENCES org_manager.member (id),
    CONSTRAINT member_to_member_role_member_role_fkey FOREIGN KEY (member_role_id) REFERENCES org_manager.member_role (id)
);

CREATE TABLE IF NOT EXISTS org_manager.member_to_organization
(
    member_id       CHARACTER VARYING NOT NULL,
    organization_id CHARACTER VARYING NOT NULL,
    CONSTRAINT organization_to_member_pkey PRIMARY KEY (member_id, organization_id),
    CONSTRAINT organization_to_member_member_fkey FOREIGN KEY (member_id) REFERENCES org_manager.member (id),
    CONSTRAINT organization_to_member_organization_fkey FOREIGN KEY (organization_id) REFERENCES org_manager.organization (id)
);

CREATE TABLE IF NOT EXISTS org_manager.organization_role_to_scope
(
    organization_role_id CHARACTER VARYING NOT NULL,
    scope_id             CHARACTER VARYING NOT NULL,
    CONSTRAINT organization_role_to_scope_pkey PRIMARY KEY (organization_role_id, scope_id),
    CONSTRAINT organization_role_to_scope_organization_role_fkey FOREIGN KEY (organization_role_id) REFERENCES org_manager.organization_role (id),
    CONSTRAINT organization_role_to_scope_scope_fkey FOREIGN KEY (scope_id) REFERENCES org_manager.scope (id)
);
