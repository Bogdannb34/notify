package com.practice.notify.repository;

import com.faunadb.client.FaunaClient;
import com.faunadb.client.errors.NotFoundException;
import com.faunadb.client.query.Expr;
import com.faunadb.client.types.Value;
import com.practice.notify.model.ClientRC;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import java.util.function.Function;

import static com.faunadb.client.query.Language.*;
import static com.faunadb.client.query.Language.Value;

@Repository
@AllArgsConstructor
public class ClientRCRepo implements FaunaRepository, LookAhead {

    private final FaunaClient faunaClient;

    @Override
    public CompletableFuture<String> nextId() {
        return faunaClient.query(
                NewId()
        ).thenApply(value -> value.to(String.class).get());
    }

    @Override
    public CompletableFuture<ClientRC> create(ClientRC clientRC) {
        return faunaClient.query(
                saveQuery(Value(clientRC.getId()), Value(clientRC))
        ).thenApply(this::toClientRC);
    }

    @Override
    public CompletableFuture<Optional<ClientRC>> findByEmail(String email) {
        CompletableFuture<ClientRC> result = faunaClient.query(
                Select(
                        Value("data"),
                        Get(Ref(Collection("ClientsRC"), Value(email)))
                )
        ).thenApply(this::toClientRC);
        return toOptionalResult(result);
    }

    @Override
    public CompletableFuture<Optional<ClientRC>> findById(String id) {
        CompletableFuture<ClientRC> result = faunaClient.query(
                Select(
                        Value("data"),
                        Get(Ref(Collection("ClientsRC"), Value(id)))
                )
        ).thenApply(this::toClientRC);
        return toOptionalResult(result);
    }

    @Override
    public CompletableFuture<Optional<ClientRC>> remove(String id) {
        CompletableFuture<ClientRC> result = faunaClient.query(
                Select(
                        Value("data"),
                        Delete(Ref(Collection("ClientsRC"), Value(id)))
                )
        ).thenApply(this::toClientRC);
        return toOptionalResult(result);
    }

    protected CompletableFuture<Optional<ClientRC>> toOptionalResult(CompletableFuture<ClientRC> result) {
        return result.handle((v, t) -> {
            CompletableFuture<Optional<ClientRC>> rc = new CompletableFuture<>();
            if (v != null) {
                rc.complete(Optional.of(v));
            } else if (t != null && t.getCause() instanceof NotFoundException) {
                rc.complete(Optional.empty());
            } else {
                rc.completeExceptionally(t);
            }
            return rc;
        }).thenCompose(Function.identity());
    }

    protected ClientRC toClientRC(Value value) {
        return value.to(ClientRC.class).get();
    }

    protected Expr saveQuery(Expr id, Expr data) {
        return Select(Value("data"),
                If(
                        Exists(Ref(Collection("ClientsRC"), id)),
                        Replace(Ref(Collection("ClientsRC"), id), Obj("data", data)),
                        Create(Ref(Collection("ClientsRC"), id), Obj("data", data))
                ));
    }

}
