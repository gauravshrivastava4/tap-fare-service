package com.littlepay.tapfare.controller;

import com.littlepay.tapfare.service.TapsProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/taps")
@RequiredArgsConstructor
@Slf4j
public class TapsProcessController {

    private final TapsProcessor tapsProcessor;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("process")
    public String processTaps() {
        return tapsProcessor.processTaps();
    }
}
