package com.littlepay.tapfare.controller;

import com.littlepay.tapfare.service.TapsProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TapsProcessController.class)
class TapsProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TapsProcessor tapsProcessor;

    @Test
    void testProcessTaps_ReturnsCreatedStatusAndMessage() throws Exception {
        final String expectedResponse = "Processing completed, output saved to src/main/resources/trips.csv";
        when(tapsProcessor.processTaps()).thenReturn(expectedResponse);

        mockMvc.perform(post("/taps/process")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string(expectedResponse));
    }
}

