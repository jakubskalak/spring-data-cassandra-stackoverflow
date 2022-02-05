package com.skalak.jakub.springdatacassandrastackoverflow.errors.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.lang.NonNull;

@Table("error")
public record Error(@PrimaryKey
                    ErrorKey key,
                    @NonNull
                    @Column("name")
                    String name) {
}
