package org.mgruszka.currency.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountDetailsDTO {
    private String id;
    private String firstName;
    private String lastName;
    private double balancePLN;
    private double balanceUSD;
}
