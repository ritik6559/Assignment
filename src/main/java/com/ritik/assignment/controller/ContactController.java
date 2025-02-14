package com.ritik.assignment.controller;


import com.ritik.assignment.request.IdentifyRequest;
import com.ritik.assignment.response.ContactResponse;
import com.ritik.assignment.services.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/identify")
    public ResponseEntity<ContactResponse> identify(@RequestBody IdentifyRequest request) {
        try {
            return ResponseEntity.ok(contactService.saveRequest(request));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
