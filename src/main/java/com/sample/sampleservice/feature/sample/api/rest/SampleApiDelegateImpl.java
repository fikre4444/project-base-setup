package com.sample.sampleservice.feature.sample.api.rest;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.sample.sampleservice.sample.infrastructure.primary.api.rest.v1.SampleApiDelegate;


@RestController
public class SampleApiDelegateImpl implements SampleApiDelegate{

    @Override
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello World");
    }
    
}
