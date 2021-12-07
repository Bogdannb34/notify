package com.practice.notify.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.notify.model.ClientRC;
import com.practice.notify.service.ClientRCService;
import lombok.AllArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.*;


@AllArgsConstructor
@RestController
public class ClientRCController {

    private final ClientRCService service;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/create")
    public CompletableFuture<ResponseEntity<?>> createClient(HttpEntity<String> httpEntity) throws IOException, SchedulerException {
        String requestBody = httpEntity.getBody();

        // create single client
        if (isClientRC(requestBody)) {
            ClientRC data = unmarshalClientRC(requestBody);
            return service.createClient(data)
                    .thenApply(client -> new ResponseEntity<>(client, CREATED));
        }
        return CompletableFuture.completedFuture(new ResponseEntity<>(BAD_REQUEST));
    }

    @GetMapping(value = "/clients/{id}")
    public CompletableFuture<ResponseEntity<?>> getClient(@PathVariable("id") String id) {
        return service.retrieveClient(id)
                .thenApply(optionalClient ->
                        optionalClient.map(
                                client -> new ResponseEntity<>(client, OK)
                        ).orElseGet(() -> new ResponseEntity<>(NOT_FOUND)));
    }

    @GetMapping(value = "/clients/{email}")
    public CompletableFuture<ResponseEntity<?>> getClientByEmail(@PathVariable("email") String email) {
        return service.retrieveClientByEmail(email)
                .thenApply(optionalClient ->
                        optionalClient.map(
                                client -> new ResponseEntity<>(client, OK)
                        ).orElseGet(() -> new ResponseEntity<>(NOT_FOUND)));
    }

    @PutMapping(value = "/clients/{id}")
    public CompletableFuture<ResponseEntity<?>> updateClient(@PathVariable("id") String id, @RequestBody ClientRC clientRC) {
        return service.updateClient(id, clientRC)
                .thenApply(optionalClient ->
                        optionalClient.map(client -> new ResponseEntity<>(client, ACCEPTED))
                                .orElseGet(() -> new ResponseEntity<>(NOT_FOUND)));
    }

    @DeleteMapping(value = "/clients/{id}")
    public CompletableFuture<ResponseEntity<?>> deleteClient(@PathVariable("id") String id) {
        return service.deleteClient(id)
                .thenApply(optionalClient ->
                        optionalClient.map(
                                client -> new ResponseEntity<>(client, GONE)
                        ).orElseGet(() -> new ResponseEntity<>(NOT_FOUND)));
    }


    private ClientRC unmarshalClientRC(String json) throws IOException {
        return objectMapper.readValue(json, ClientRC.class);
    }

    private boolean isClientRC(String json) throws IOException {
        try {
            objectMapper.readValue(json, ClientRC.class);
            return true;
        } catch (JsonParseException | JsonMappingException e) {
            return false;
        }
    }

}
