package org.otonielsilva.models;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.ws.rs.BadRequestException;
import lombok.Data;

@Data
@RegisterForReflection
public class TransacaoRequest {

    private String valor;

    private String tipo;

    private String descricao;


    public Long getValor() {
        try {
            long longValue = Long.parseLong(this.valor);
            return longValue;
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid value. Expected a long integer.");
        }
    }

}
