package com.littlepay.tapfare.model;

import com.littlepay.tapfare.constant.TapType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Tap {
    private long id;
    private LocalDateTime localDateTime;
    private TapType tapType;
    private String stopId;
    private String companyId;
    private String busId;
    private String pan;
}
