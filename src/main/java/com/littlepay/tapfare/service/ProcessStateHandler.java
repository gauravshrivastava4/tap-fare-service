package com.littlepay.tapfare.service;

import com.littlepay.tapfare.constant.ProcessState;
import lombok.Data;
import org.springframework.stereotype.Component;


/**
 * Handler for managing the current process state of an operation.
 * This class provides methods to check if a process is already running.
 */
@Component
@Data
public class ProcessStateHandler {

    private ProcessState processState = ProcessState.NOT_STARTED;

    public boolean isProcessingAlreadyRunning() {
        return processState.equals(ProcessState.STARTED);
    }
}
