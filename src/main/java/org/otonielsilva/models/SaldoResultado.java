package org.otonielsilva.models;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
@RegisterForReflection
public class SaldoResultado {
    private Long saldo;
    private Long limite;
}
