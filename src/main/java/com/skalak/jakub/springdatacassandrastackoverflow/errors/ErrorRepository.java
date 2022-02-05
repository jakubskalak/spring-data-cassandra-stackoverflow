package com.skalak.jakub.springdatacassandrastackoverflow.errors;

import com.skalak.jakub.springdatacassandrastackoverflow.errors.model.Error;
import com.skalak.jakub.springdatacassandrastackoverflow.errors.model.ErrorKey;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ErrorRepository extends ReactiveCassandraRepository<Error, ErrorKey> {

    @Query(
            "select * from stackoverflow.error " +
                    "where id = :id"
    )
    Mono<Error> findError(@NonNull Long id);

    @Query(
            "select * from stackoverflow.error"
    )
    Flux<Error> findAll();
}
