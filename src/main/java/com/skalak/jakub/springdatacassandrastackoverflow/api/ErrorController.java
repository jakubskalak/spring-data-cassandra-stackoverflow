package com.skalak.jakub.springdatacassandrastackoverflow.api;

import com.skalak.jakub.springdatacassandrastackoverflow.errors.ErrorService;
import com.skalak.jakub.springdatacassandrastackoverflow.errors.model.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/errors")
@RequiredArgsConstructor
public class ErrorController {

    private final ErrorService errorService;

    @PostMapping("/batch")
    public Mono<ResponseEntity<Void>> longRunningBatch() {
        errorService.findAll().subscribe();
        return Mono.just(ResponseEntity.accepted().build());
    }

    @PostMapping("/many/{amount}")
    public Flux<Error> registerManyErrors(@PathVariable("amount") Long amount) {
        return errorService.registerMultipleErrors(amount);
    }
}
