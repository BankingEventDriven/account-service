package com.bank.account_service.infrastructure.persistence.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class OwnerEmbeddable {

    @Column(name = "owner_first_name", nullable = false)
    private String firstName;

    @Column(name = "owner_last_name", nullable = false)
    private String lastName;

    public OwnerEmbeddable() {

    }

    public OwnerEmbeddable(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
