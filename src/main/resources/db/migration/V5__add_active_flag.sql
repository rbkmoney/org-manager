-- member_role
ALTER TABLE org_manager.member_role
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT false;