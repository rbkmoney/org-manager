package com.rbkmoney.orgmanager.service;

import com.rbkmoney.damsel.message_sender.MailBody;
import com.rbkmoney.damsel.message_sender.Message;
import com.rbkmoney.damsel.message_sender.MessageMail;
import com.rbkmoney.damsel.message_sender.MessageSenderSrv;
import com.rbkmoney.swag.organizations.model.Invitation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("inviteMessageSender")
@RequiredArgsConstructor
@Slf4j
public class MailInviteMessageSender {

    @Value("${dashboard.url}")
    private String dashboardUrl;

    private final MessageSenderSrv.Iface dudoserClient;

    public void send(Invitation invitation) {
        try {
            MessageMail messageMail = new MessageMail();
            messageMail.setMailBody(new MailBody(dashboardUrl + invitation.getAcceptToken()));
            messageMail.setToEmails(List.of(invitation.getInvitee().getContact().getEmail()));
            messageMail.setSubject("Подтверждение вступления в организацию");
            messageMail.setFromEmail("no-reply@rbkmoney.com");

            dudoserClient.send(Message.message_mail(messageMail));
        } catch (Exception ex) {
            log.warn("dudoserClient error", ex);
        }
    }
}
