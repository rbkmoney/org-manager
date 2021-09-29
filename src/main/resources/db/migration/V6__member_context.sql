CREATE TABLE IF NOT EXISTS org_manager.member_context
(
    id              BIGSERIAL         NOT NULL,
    member_id       CHARACTER VARYING NOT NULL,
    organization_id CHARACTER VARYING NOT NULL,
    CONSTRAINT member_context_pkey PRIMARY KEY (id),

    CONSTRAINT member_context_to_member_fkey FOREIGN KEY (member_id) REFERENCES org_manager.member (id),
    CONSTRAINT member_context_to_organization_fkey FOREIGN KEY (organization_id) REFERENCES org_manager.organization (id)
);
