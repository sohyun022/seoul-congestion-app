package com.example.seoulcongestion.dto;

import com.example.seoulcongestion.domain.CongestionData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiResponse {

    private List<CongestionData> results;

}
