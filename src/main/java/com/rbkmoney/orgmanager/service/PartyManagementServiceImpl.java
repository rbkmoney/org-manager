package com.rbkmoney.orgmanager.service;

import com.rbkmoney.damsel.domain.PartyContactInfo;
import com.rbkmoney.damsel.payment_processing.ExternalUser;
import com.rbkmoney.damsel.payment_processing.PartyExists;
import com.rbkmoney.damsel.payment_processing.PartyManagementSrv;
import com.rbkmoney.damsel.payment_processing.PartyParams;
import com.rbkmoney.damsel.payment_processing.UserInfo;
import com.rbkmoney.damsel.payment_processing.UserType;
import com.rbkmoney.orgmanager.exception.PartyManagementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PartyManagementServiceImpl implements PartyManagementService {

    private final PartyManagementSrv.Iface partyManagementClient;

    @Override
    public void createParty(String partyId, String email) {
        UserInfo userInfo = new UserInfo(partyId, UserType.external_user(new ExternalUser()));
        PartyParams partyParams = new PartyParams(new PartyContactInfo(email));
        try {
            partyManagementClient.create(userInfo, partyId, partyParams);
        } catch (PartyExists ex) {
            log.warn("Party {} already exists", partyId);
        } catch (TException ex) {
            throw new PartyManagementException(
                    String.format("Exception during party creation. (partyId: %s, email: %s)", partyId, email), ex);
        }

        log.info("Created party with id {}, for user {}", partyId, email);
    }
}
