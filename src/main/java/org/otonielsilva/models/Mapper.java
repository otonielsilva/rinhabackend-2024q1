package org.otonielsilva.models;

import org.otonielsilva.entities.TransacaoDB;

@org.mapstruct.Mapper(componentModel = "cdi")
public interface Mapper {

        Transacao toTransacao(TransacaoDB transacaoDB);
}
