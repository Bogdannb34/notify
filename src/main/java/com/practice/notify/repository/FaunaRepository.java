package com.practice.notify.repository;

import com.practice.notify.model.ClientRC;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Repository
public interface FaunaRepository {

   CompletableFuture<ClientRC> create(ClientRC clientRC);
//   CompletableFuture<List<ClientRC>> findAll();
   CompletableFuture<Optional<ClientRC>> findByEmail(String email);
   CompletableFuture<Optional<ClientRC>> findById(String id);
   CompletableFuture<Optional<ClientRC>> remove(String id);




}
