package com.example.seoulcongestion.domain;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CongestionData {

    private String populationTime;
    private String areaName;

    private String areaCongestLevel;
    private String areaCongestMsg;

    private List<String> forecastTimes;
    private List<String> forecastCongestions;

    private String roadCongestMsg;
    private String roadCongestIdx;

    private double latitude;
    private double longitude;


}
