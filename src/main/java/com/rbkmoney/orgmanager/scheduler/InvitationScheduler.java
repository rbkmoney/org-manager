package com.rbkmoney.orgmanager.scheduler;

import com.rbkmoney.orgmanager.service.InvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "scheduler", name = "invitation.enabled", havingValue = "true")
public class InvitationScheduler {

    private InvitationService invitationService;

    @Scheduled(fixedDelayString = "${service.dominant.scheduler.checkStatusDelay}")
    @SchedulerLock(name = "invitationStatus")
    public void pollScheduler() {
        log.info("Performing verification of expired invites");
        invitationService.checkOnExpiredStatus();
    }

}
