package com.vector.engine.controller;

import com.vector.engine.service.BrainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/vector")
public class VectorController {

    private final BrainService brainService;

    public VectorController(BrainService brainService) {
        this. brainService = brainService;
    }

    @PostMapping("/generate")
    public CompletableFuture<ResponseEntity<String>> generate(@RequestBody String content) {
        return brainService.createEmbedding(content)
                .thenApply(note -> {
                    return ResponseEntity.status(HttpStatus.CREATED).body(note);
                });
    }
}
