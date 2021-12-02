package com.practice.notify.repository;

import java.util.concurrent.CompletableFuture;

public interface LookAhead {

    CompletableFuture<String> nextId();
}
