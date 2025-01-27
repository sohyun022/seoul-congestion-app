package com.example.seoulcongestion.exception;

import lombok.Getter;

@Getter
public enum CustomErrorInfo {

    // 400 BAD_REQUEST
    FAILED_TO_FETCH_DATA(400,"Seoul API 호출 실패"),
    FAILED_TO_PARSE_DATA(400,"Seoul API 데이터 처리 실패"),

    // 409 CONFLICT
    INVALID_AREA_NAME(409, "유효하지 않은 장소명입니다."),
    INVALID_JSON_STRUCTURE(409,"유효하지 않은 json 데이터입니다.");

    private final int statusCode;
    private final String message;

    CustomErrorInfo(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}
