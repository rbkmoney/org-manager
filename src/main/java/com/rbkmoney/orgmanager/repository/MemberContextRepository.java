package com.rbkmoney.orgmanager.repository;

import com.rbkmoney.orgmanager.entity.MemberContextEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberContextRepository extends JpaRepository<MemberContextEntity, String> {

    Optional<MemberContextEntity> findByMemberEntityId(String memberId);

}
