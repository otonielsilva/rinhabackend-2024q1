package org.otonielsilva.entities;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@RegisterForReflection
public class TransacaoDB implements Serializable {

    private Long id;

    private int clienteId;

    private String tipo;

    private String descricao;

    private LocalDateTime realizadaEm;

    private Long valor;

}
