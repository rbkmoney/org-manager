package com.rbkmoney.orgmanager.service;

import com.rbkmoney.damsel.payment_processing.InvalidUser;
import com.rbkmoney.damsel.payment_processing.PartyExists;
import com.rbkmoney.damsel.payment_processing.PartyManagementSrv;
import com.rbkmoney.damsel.payment_processing.PartyParams;
import com.rbkmoney.damsel.payment_processing.UserInfo;
import com.rbkmoney.orgmanager.exception.PartyManagementException;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.rbkmoney.orgmanager.TestObjectFactory.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PartyManagementServiceImplTest {

    private PartyManagementService partyManagementService;

    @Mock
    private PartyManagementSrv.Iface partyManagementClient;

    @BeforeEach
    void setUp() {
        partyManagementService = new PartyManagementServiceImpl(partyManagementClient);
    }

    @Test
    void shouldThrowPartyManagementExceptionOnCreateParty() throws TException {
        doThrow(new InvalidUser())
                .when(partyManagementClient).create(any(UserInfo.class), anyString(), any(PartyParams.class));
        String partyId = randomString();
        String email = randomString();

        PartyManagementException partyManagementException =
                assertThrows(PartyManagementException.class, () -> partyManagementService.createParty(partyId, email));

        assertTrue(partyManagementException.getMessage()
                .contains(String.format("Exception during party creation. (partyId: %s, email: %s)", partyId, email)));
    }

    @Test
    void shouldCreatePartyIfPartyExistThrown() throws TException {
        doThrow(new PartyExists())
                .when(partyManagementClient).create(any(UserInfo.class), anyString(), any(PartyParams.class));
        String partyId = randomString();
        String email = randomString();

        partyManagementService.createParty(partyId, email);

        verify(partyManagementClient, times(1))
                .create(any(UserInfo.class), anyString(), any(PartyParams.class));
    }

    @Test
    void shouldCreateParty() throws TException {
        String partyId = randomString();
        String email = randomString();

        partyManagementService.createParty(partyId, email);

        ArgumentCaptor<UserInfo> userInfoCaptor = ArgumentCaptor.forClass(UserInfo.class);
        ArgumentCaptor<String> partyIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PartyParams> partyParamsCaptor = ArgumentCaptor.forClass(PartyParams.class);

        verify(partyManagementClient, times(1))
                .create(userInfoCaptor.capture(), partyIdCaptor.capture(), partyParamsCaptor.capture());

        assertEquals(1, userInfoCaptor.getAllValues().size());
        assertEquals(partyId, userInfoCaptor.getValue().getId());
        assertTrue(userInfoCaptor.getValue().getType().isSetExternalUser());

        assertEquals(1, partyIdCaptor.getAllValues().size());
        assertEquals(partyId, partyIdCaptor.getValue());

        assertEquals(1, partyParamsCaptor.getAllValues().size());
        assertEquals(email, partyParamsCaptor.getValue().getContactInfo().getEmail());
    }
}
