package com.littlepay.tapfare.service;

import com.littlepay.tapfare.constant.ProcessState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProcessStateHandlerTest {

    private ProcessStateHandler processStateHandler;

    @BeforeEach
    void setUp() {
        processStateHandler = new ProcessStateHandler();
    }

    @Test
    void testInitialState() {
        // The initial state should be NOT_STARTED
        assertEquals(ProcessState.NOT_STARTED, processStateHandler.getProcessState());
    }

    @Test
    void testSetProcessState() {
        // Set the process state to STARTED
        processStateHandler.setProcessState(ProcessState.STARTED);
        assertEquals(ProcessState.STARTED, processStateHandler.getProcessState());

        // Set the process state to COMPLETED
        processStateHandler.setProcessState(ProcessState.COMPLETED);
        assertEquals(ProcessState.COMPLETED, processStateHandler.getProcessState());
    }

    @Test
    void testIsProcessingAlreadyRunning() {
        // Initially, processing should not be running
        assertFalse(processStateHandler.isProcessingAlreadyRunning());

        // Set the process state to STARTED, now it should be running
        processStateHandler.setProcessState(ProcessState.STARTED);
        assertTrue(processStateHandler.isProcessingAlreadyRunning());

        // Set the process state to COMPLETED, it should no longer be running
        processStateHandler.setProcessState(ProcessState.COMPLETED);
        assertFalse(processStateHandler.isProcessingAlreadyRunning());

        // Set the process state to FAILED, it should no longer be running
        processStateHandler.setProcessState(ProcessState.FAILED);
        assertFalse(processStateHandler.isProcessingAlreadyRunning());
    }
}
