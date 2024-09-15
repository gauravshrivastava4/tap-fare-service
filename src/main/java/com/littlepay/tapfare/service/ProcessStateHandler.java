package com.littlepay.tapfare.service;

import com.littlepay.tapfare.constant.ProcessState;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class ProcessStateHandler {

    private ProcessState processState = ProcessState.NOT_STARTED;
    
    public boolean isProcessingAlreadyRunning() {
        return processState.equals(ProcessState.STARTED);
    }
}
