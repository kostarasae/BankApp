package gr.aueb.cf.restbankapp.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BankConfigurationSetup {

    @Value("${bank.fee.checking:0.5}")
    private BigDecimal checkingFee;

    @Value("${bank.fee.savings:1.0}")
    private BigDecimal savingsFee;

    @PostConstruct
    public void init() {
        BankConfiguration.getInstance().setDefaultCheckingFee(checkingFee);
        BankConfiguration.getInstance().setDefaultSavingsFee(savingsFee);
    }
}
