package com.skalak.jakub.springdatacassandrastackoverflow.errors;

import com.skalak.jakub.springdatacassandrastackoverflow.errors.model.Error;
import com.skalak.jakub.springdatacassandrastackoverflow.errors.model.ErrorKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorService {

    private final ErrorRepository errorRepository;
    private final AtomicLong counter = new AtomicLong(0);

    public Flux<Error> findAll() {
        return errorRepository.findAll()
                .doOnNext(error -> counter.getAndIncrement())
                .doOnNext(error -> log.debug("Processed: {}", counter.get()))
                .doFinally(signal -> counter.set(0));
    }

    public Flux<Error> registerMultipleErrors(@NonNull Long amount) {
        return Flux.fromStream(LongStream.range(0L, amount).boxed())
                .flatMap(this::registerError);
    }

    private Mono<Error> registerError(@NonNull Long id) {
        return errorRepository.save(new Error(new ErrorKey(id), "ErrorName" + id));
    }
}
