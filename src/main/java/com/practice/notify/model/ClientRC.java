package com.practice.notify.model;

import com.faunadb.client.types.FaunaConstructor;
import com.faunadb.client.types.FaunaField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class ClientRC {

    private String id;
    private String name;
    private String email;
    private LocalDate dateCreated;

    @FaunaConstructor
    public ClientRC(@FaunaField("id") String id,
                    @FaunaField("name") String name,
                    @FaunaField("email") String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.dateCreated = getDateCreated();
    }

    public LocalDate getDateCreated() {
        return LocalDate.now();
    }
}
