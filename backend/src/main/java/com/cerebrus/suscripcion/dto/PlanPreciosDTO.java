package com.cerebrus.suscripcion.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class PlanPreciosDTO {

    private final Map<String, Integer> limitesBase;
    private final Map<String, Double> preciosBase;

}


