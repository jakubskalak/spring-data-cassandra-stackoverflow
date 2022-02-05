package com.skalak.jakub.springdatacassandrastackoverflow.errors.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.lang.NonNull;

import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;

@PrimaryKeyClass
public record ErrorKey(@NonNull
                       @PrimaryKeyColumn(name = "id", type = PARTITIONED)
                       Long id) {
}
