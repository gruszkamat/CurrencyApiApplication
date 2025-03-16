package org.mgruszka.currency.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreationDTO {
    private String firstName;
    private String lastName;
    @Schema(description = "Initial balance in PLN")
    private double initialBalance;
}
