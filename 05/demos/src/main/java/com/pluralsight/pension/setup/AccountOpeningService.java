package com.pluralsight.pension.setup;

import com.pluralsight.pension.AccountRepository;

import java.io.IOException;
import java.time.LocalDate;

public class AccountOpeningService {

    public static final String UNACCEPTABLE_RISK_PROFILE = "HIGH";
    private BackgroundCheckService backgroundCheckService;
    private ReferenceIdsManager referenceIdsManager;
    private AccountRepository accountRepository;
    private AccountOpeningEventPublisher eventPublisher;


    public AccountOpeningService(BackgroundCheckService backgroundCheckService,
                                 ReferenceIdsManager referenceIdsManager,
                                 AccountRepository accountRepository,
                                 AccountOpeningEventPublisher eventPublisher) {
        this.backgroundCheckService = backgroundCheckService;
        this.referenceIdsManager = referenceIdsManager;
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

     //open account method
    // ilk background check service confirm methodunu cagiriyor
    // sonra null kontrolu yapip null degilse risk profile kontrolu yapip
    // risk profile high degilse account olusturuyor
    // account olusturulduktan sonra reference id managerdan bir id aliyor
    // account olusturulduktan sonra account repositoryden save ediyor
    // account olusturulduktan sonra event publisherdan publish ediyor
    // account olusturulduktan sonra account opening statusu opened olarak donduruyor
    public AccountOpeningStatus openAccount(String firstName, String lastName, String taxId, LocalDate dob)
            throws IOException {

        final BackgroundCheckResults backgroundCheckResults = backgroundCheckService.confirm(firstName,
                lastName,
                taxId,
                dob);

        if (backgroundCheckResults == null || backgroundCheckResults.getRiskProfile().equals(UNACCEPTABLE_RISK_PROFILE)) {
            return AccountOpeningStatus.DECLINED;
        } else {
            final String id = referenceIdsManager.obtainId(firstName, "", lastName, taxId, dob);
            if (id != null) {
                accountRepository.save(id, firstName, lastName, taxId, dob, backgroundCheckResults);
                eventPublisher.notify(id);
                return AccountOpeningStatus.OPENED;
            } else {
                return AccountOpeningStatus.DECLINED;
            }
        }
    }
}