package com.skalak.jakub.springdatacassandrastackoverflow.api;

import com.skalak.jakub.springdatacassandrastackoverflow.errors.ErrorService;
import com.skalak.jakub.springdatacassandrastackoverflow.errors.model.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/errors")
@RequiredArgsConstructor
public class ErrorController {

    private final ErrorService errorService;

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Error>> findError(@PathVariable("id") Long id) {
        return errorService.findError(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Error>> registerNewError(@PathVariable("id") Long id) {
        return errorService.registerError(id)
                .map(ResponseEntity::ok);
    }

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
