package org.otonielsilva.models;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@RegisterForReflection
public class Transacao {


    private String tipo;

    private String descricao;

    private LocalDateTime realizadaEm;

    private Long valor;




}
