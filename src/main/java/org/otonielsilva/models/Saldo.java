package org.otonielsilva.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@RegisterForReflection
public class Saldo {

    @JsonProperty("data_extrato")
    private LocalDateTime dataExtrato;

    private Long total;

    private Long limite;



}
