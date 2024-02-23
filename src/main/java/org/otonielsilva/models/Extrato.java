package org.otonielsilva.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@RegisterForReflection
public class Extrato {

    private Saldo saldo;

    @JsonProperty("ultimas_transacoes")
    private List<Transacao> ultimasTransacoes = new ArrayList<>();


}
