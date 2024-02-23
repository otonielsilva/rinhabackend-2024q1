package org.otonielsilva;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.otonielsilva.entities.ClienteDB;
import org.otonielsilva.entities.TransacaoDB;
import org.otonielsilva.models.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Path("/clientes")
public class ClienteResource {

    @Inject
    Mapper mapper;

    @Inject
    DataSource ds;

    @GET
    @Path("/{id}/extrato")
    public Response extrato(@PathParam("id") int id) {

        try (var conn = ds.getConnection()) {
            ClienteDB cliente = findClient(conn, id, false);
            if (cliente == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("mensagem", "Cliente nao encontrado")).build();
            }

            List<TransacaoDB> transacaoDBS = getTransacaoDBS(conn, id);

            Extrato extrato = Extrato.builder()
                    .saldo(new Saldo(LocalDateTime.now(), cliente.getSaldo(), cliente.getLimite()))
                    .ultimasTransacoes(transacaoDBS.stream().map(mapper::toTransacao).toList())
                    .build();

            return Response.ok(extrato).build();
        } catch (Exception ex) {
            System.err.println(ex);
            return Response.serverError().build();
        }
    }



    @POST
    @Path("/{id}/transacoes")
    public Response transacao(@PathParam("id") Integer id, TransacaoRequest request) {

        if (!"c".equals(request.getTipo()) && !"d".equals(request.getTipo())) {
            return Response.status(422).entity(Map.of("mensagem", "Tipo invalido")).build();
        }
        if (request.getValor() == null  || request.getValor() <= 0) {
            return Response.status(422).entity(Map.of("mensagem", "Valor invalido")).build();
        }
        if (request.getDescricao() == null || request.getDescricao().length() < 1 || request.getDescricao().length() > 10  ) {
            return Response.status(422).entity(Map.of("mensagem", "Descricao invalido")).build();
        }

        if (id <1  || id  > 5) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("mensagem", "Cliente nao encontrado")).build();
        }

        return getResponse(id, request);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Response getResponse(Integer id, TransacaoRequest request) {
        try (var conn = ds.getConnection()) {

            try (var statement = conn.prepareStatement("select * from update_saldo_cliente(?, ?, ?, ?) ")) {
                statement.setInt(1, id);
                statement.setLong(2, request.getValor());
                statement.setString(3, request.getTipo());;
                statement.setString(4, request.getDescricao());;
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    if (resultSet.getString("erro") == null)
                        return Response.ok( new SaldoResultado( resultSet.getLong("new_saldo"), resultSet.getLong("limite"))).build();
                    return Response.status(422).entity(Map.of("mensagem", "Transacao invalida devido ao tamanho")).build();
                }
            }

            return Response.serverError().build();

        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            return Response.serverError().build();
        }
    }


    public SaldoResultado efetuarTransacao(Connection connection, ClienteDB client, TransacaoRequest request) throws SQLException {

        if (request.getTipo().equals("c")) {
            client.setSaldo(client.getSaldo() + request.getValor());
        }
        if (request.getTipo().equals("d")) {
            client.setSaldo(client.getSaldo() - request.getValor());
        }
        TransacaoDB transacao = new TransacaoDB();
        transacao.setTipo(request.getTipo());
        transacao.setDescricao(request.getDescricao());
        transacao.setRealizadaEm(LocalDateTime.now());
        transacao.setValor(request.getValor());
        transacao.setClienteId(client.getId());

        insereTransacao(connection, transacao);
        salvaNovoSaldo(connection, client);

        return new SaldoResultado(client.getSaldo(), client.getLimite());
    }

    private void insereTransacao(Connection connection, TransacaoDB t) throws SQLException {
        try (var insertStatement = connection.prepareStatement("INSERT INTO transacoes (cliente_id, valor, tipo, descricao) values (?, ?, ?, ?) ")) {
            insertStatement.setLong(1, t.getClienteId());
            insertStatement.setLong(2, t.getValor());
            insertStatement.setString(3, t.getTipo());;
            insertStatement.setString(4, t.getDescricao());;
            insertStatement.executeUpdate();
        }
    }


    private void salvaNovoSaldo(Connection connection, ClienteDB client) throws SQLException {
        try (var insertStatement = connection.prepareStatement("UPDATE clientes SET saldo = ?, limite = ? WHERE id = ?")) {
            insertStatement.setLong(1, client.getSaldo());
            insertStatement.setLong(2, client.getLimite());
            insertStatement.setInt(3, client.getId());
            insertStatement.executeUpdate();
        }
    }

    private List<TransacaoDB> getTransacaoDBS(Connection connec, int id) throws SQLException {
        try (var statement = connec.prepareStatement("SELECT valor, tipo, descricao, realizada_em FROM transacoes WHERE cliente_id = ? ORDER BY realizada_em DESC LIMIT 10")) {
            statement.setInt(1, id);
            try (var rs = statement.executeQuery()) {
                List<TransacaoDB> transacaoDBs = new ArrayList<>();
                while (rs.next()) {
                    TransacaoDB t = new TransacaoDB();
                    t.setTipo(rs.getString("tipo"));
                    t.setDescricao(rs.getString("descricao"));
                    t.setRealizadaEm(rs.getTimestamp("realizada_em").toLocalDateTime());
                    t.setValor(rs.getLong("valor"));
                    transacaoDBs.add(t);
                }
                return transacaoDBs;

            }
        }
    }

    private ClienteDB findClient(Connection connec, int id, boolean lock) throws SQLException {
        try (var statement = connec.prepareStatement("SELECT limite, saldo from clientes WHERE id = ? " + (lock ? " FOR UPDATE" : ""))) {
            statement.setInt(1, id);
            try (var rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ClienteDB clienteDB = new ClienteDB();
                clienteDB.setId(id);
                clienteDB.setLimite(  rs.getLong("limite") );
                clienteDB.setSaldo(  rs.getLong("saldo") );
                return clienteDB;
            }
        }
    }


}
