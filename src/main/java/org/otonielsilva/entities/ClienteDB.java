package org.otonielsilva.entities;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.io.Serializable;

@Data
@RegisterForReflection
public class ClienteDB  implements Serializable {

    private Integer id;

    private String nome;

    private Long saldo;

    private Long limite;

}
