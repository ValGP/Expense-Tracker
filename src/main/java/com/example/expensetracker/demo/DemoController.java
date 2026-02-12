package com.example.expensetracker.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/demo")
public class DemoController {

    private final DemoService demoService;

    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        demoService.resetDemo();
        return ResponseEntity.noContent().build();
    }
}
