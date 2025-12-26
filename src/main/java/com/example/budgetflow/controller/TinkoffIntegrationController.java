package com.example.budgetflow.controller;

import com.example.budgetflow.dto.TinkoffSyncRequest;
import com.example.budgetflow.dto.TinkoffSyncResponse;
import com.example.budgetflow.service.TinkoffIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/integrations/tinkoff")
@RequiredArgsConstructor
public class TinkoffIntegrationController {

    private final TinkoffIntegrationService tinkoffIntegrationService;

    @PostMapping("/sync")
    public ResponseEntity<TinkoffSyncResponse> sync(
            @RequestBody @Valid TinkoffSyncRequest request,
            @RequestParam Long userId) {
        TinkoffSyncResponse response = tinkoffIntegrationService.enqueueSync(request, userId);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}



