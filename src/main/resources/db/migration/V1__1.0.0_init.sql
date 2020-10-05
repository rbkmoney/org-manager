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
    metadata              CHARACTER VARYING,
    CONSTRAINT invitation_pkey PRIMARY KEY (id)
);

CREATE INDEX invitation_organization_id on org_manager.invitation (organization_id);

CREATE TABLE IF NOT EXISTS org_manager.role
(
    id              CHARACTER VARYING NOT NULL,
    organization_id CHARACTER VARYING NOT NULL,
    role_id         CHARACTER VARYING NOT NULL,
    scope_id        CHARACTER VARYING NOT NULL,
    resource_id     CHARACTER VARYING NOT NULL,
    CONSTRAINT role_pkey PRIMARY KEY (id)
);

CREATE INDEX role_organization_id on org_manager.role (organization_id);

CREATE TABLE IF NOT EXISTS org_manager.member
(
    id    CHARACTER VARYING NOT NULL,
    email CHARACTER VARYING NOT NULL,
    CONSTRAINT member_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS org_manager.invitee_role
(
    invitation_id CHARACTER VARYING NOT NULL,
    role_id       CHARACTER VARYING NOT NULL,
    CONSTRAINT invitee_role_pkey PRIMARY KEY (invitation_id, role_id),
    CONSTRAINT invitee_role_invitation_fkey FOREIGN KEY (invitation_id) REFERENCES org_manager.invitation (id),
    CONSTRAINT invitee_role_role_fkey FOREIGN KEY (role_id) REFERENCES org_manager.role (id)
);

CREATE TABLE IF NOT EXISTS org_manager.member_role
(
    member_id CHARACTER VARYING NOT NULL,
    role_id   CHARACTER VARYING NOT NULL,
    CONSTRAINT member_role_pkey PRIMARY KEY (member_id, role_id),
    CONSTRAINT member_role_member_fkey FOREIGN KEY (member_id) REFERENCES org_manager.member (id),
    CONSTRAINT member_role_role_fkey FOREIGN KEY (role_id) REFERENCES org_manager.role (id)
);

CREATE TABLE IF NOT EXISTS org_manager.organization_member
(
    organization_id CHARACTER VARYING NOT NULL,
    member_id       CHARACTER VARYING NOT NULL,
    CONSTRAINT organization_member_pkey PRIMARY KEY (organization_id, member_id),
    CONSTRAINT organization_member_organization_fkey FOREIGN KEY (organization_id) REFERENCES org_manager.organization (id),
    CONSTRAINT organization_member_member_fkey FOREIGN KEY (member_id) REFERENCES org_manager.member (id)
);
