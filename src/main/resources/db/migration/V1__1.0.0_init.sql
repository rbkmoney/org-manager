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
    name            CHARACTER VARYING NOT NULL,
    CONSTRAINT role_pkey PRIMARY KEY (id)
);

CREATE INDEX role_organization_id on org_manager.role (organization_id);

CREATE TABLE IF NOT EXISTS org_manager.invitee_role
(
    invitation_id CHARACTER VARYING NOT NULL,
    role_id    CHARACTER VARYING NOT NULL,
    CONSTRAINT invitee_role_pkey PRIMARY KEY (invitation_id, role_id),
    CONSTRAINT invitee_role_invitation_fkey FOREIGN KEY (invitation_id) REFERENCES org_manager.invitation(id),
    CONSTRAINT invitee_role_role_fkey FOREIGN KEY (role_id) REFERENCES org_manager.role(id)
);