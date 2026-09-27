package com.pod.back.controllers;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @Autowired
    private Environment env;

    @GetMapping("/ping")
    public Map<String, Object> ping(){
        return Map.of(
                "status","ok22",
                "message",  env.getProperty("TEST","failed"),
                "timestamp", Instant.now().toString()
        );
    }
}
