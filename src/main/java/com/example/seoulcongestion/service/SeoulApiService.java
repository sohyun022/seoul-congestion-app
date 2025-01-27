package com.example.seoulcongestion.service;

import com.example.seoulcongestion.domain.CongestionData;
import com.example.seoulcongestion.exception.CustomErrorInfo;
import com.example.seoulcongestion.exception.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeoulApiService {

    private final WebClient webClient;

    @Value("${seoul.api.key}")
    private String apiKey;

    @Value("${seoul.api.base-url}")
    private String baseUrl;

    @Value("${seoul.api.request-type}")
    private String requestType;

    @Value("${seoul.api.service-name}")
    private String serviceName;

    @Value("${seoul.api.start-index}")
    private String startIndex;

    @Value("${seoul.api.end-index}")
    private String endIndex;

    public SeoulApiService() {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public CongestionData getCongestionInfo(String areaName){

        if(areaName == null || areaName.trim().isEmpty()){
            log.warn("Invalid area name parameter: {}", areaName);
            throw new CustomException(CustomErrorInfo.INVALID_AREA_NAME);
        }
        String endpoint = UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment(apiKey, requestType, serviceName, startIndex, endIndex, areaName)
                .toUriString();

        Mono<String> response = webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(String.class);

        if(Objects.requireNonNull(response.block()).isEmpty()) {
            log.error("Failed to fetch data from Seoul API");
            throw new CustomException(CustomErrorInfo.FAILED_TO_FETCH_DATA);
        }

        // 응답 데이터를 JSON으로 파싱
        CongestionData  parsedData = parseJsonData(response.block(), areaName);

        //지역의 위도와 경도를 저장
        setLatitudeAndLongitude(parsedData);

        return parsedData;

    }

    private CongestionData parseAreaNode(JsonNode node) {

        // CongestionData 객체 생성
        CongestionData congestionData = new CongestionData();
        congestionData.setAreaName(node.path("AREA_NM").asText());

        // LIVE_PPLTN_STTS 처리
        JsonNode livePopulationArray = node.path("LIVE_PPLTN_STTS");
        for (JsonNode liveNode : livePopulationArray) {

            congestionData.setPopulationTime(liveNode.path("PPLTN_TIME").asText());
            congestionData.setAreaCongestLevel(liveNode.path("AREA_CONGEST_LVL").asText());
            congestionData.setAreaCongestMsg(liveNode.path("AREA_CONGEST_MSG").asText());

            // 예측 데이터 처리
            JsonNode forecastNodeList =  liveNode.path("FCST_PPLTN");
            List<String> forecastTimes = new ArrayList<>();
            List<String> forecastCongestions = new ArrayList<>();

            for (JsonNode forecastNode : forecastNodeList) {
                String fcstTimeStr = forecastNode.path("FCST_TIME").asText();
                if (fcstTimeStr == null || fcstTimeStr.isEmpty()) {
                    log.warn("Invalid forecast time: {}", forecastNode);
                    continue;
                }

                LocalDateTime fcstTime = LocalDateTime.parse(fcstTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                forecastTimes.add(fcstTime.toLocalTime().toString());
                forecastCongestions.add(forecastNode.path("FCST_CONGEST_LVL").asText());
            }

            congestionData.setForecastTimes(forecastTimes);
            congestionData.setForecastCongestions(forecastCongestions);

        }

        // ROAD_TRAFFIC_STTS 처리
        JsonNode roadTraffic = node.path("ROAD_TRAFFIC_STTS").path("AVG_ROAD_DATA");
        if (!roadTraffic.isMissingNode()) {
            congestionData.setRoadCongestMsg(roadTraffic.path("ROAD_MSG").asText());
            congestionData.setRoadCongestIdx(roadTraffic.path("ROAD_TRAFFIC_IDX").asText());
        }

        return congestionData;
    }


    private CongestionData parseJsonData(String jsonData, String areaName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonData);

            // 데이터가 담긴 JSON 루트 노드 찾기
            JsonNode dataNode = root.get("CITYDATA");
            if (dataNode == null || !dataNode.isObject()) {
                log.error("Invalid JSON structure: Missing or invalid 'CITYDATA' field");
                throw new CustomException(CustomErrorInfo.INVALID_JSON_STRUCTURE);
            }

            log.debug("Parsing CITYDATA as Object.");

            String areaNm = dataNode.path("AREA_NM").asText();

            CongestionData congestionData = null;

            if (areaNm.equals(areaName.trim())){
                congestionData = parseAreaNode(dataNode);
            }
            else throw new CustomException(CustomErrorInfo.FAILED_TO_FETCH_DATA);

            return congestionData;

        } catch (JsonProcessingException e) {
            log.error("Error processing JSON data: {}", e.getMessage());
            throw new CustomException(CustomErrorInfo.FAILED_TO_PARSE_DATA);
        }
    }

    private void setLatitudeAndLongitude(CongestionData data) {
        switch (data.getAreaName()) {
            case "강남 MICE 관광특구":
                data.setLatitude(37.5133);
                data.setLongitude(127.0592);
                break;
            case "동대문 관광특구":
                data.setLatitude(37.5714);
                data.setLongitude(127.0090);
                break;
            case "명동 관광특구":
                data.setLatitude(37.5636);
                data.setLongitude(126.9827);
                break;
            case "이태원 관광특구":
                data.setLatitude(37.5345);
                data.setLongitude(126.9943);
                break;
            case "잠실 관광특구":
                data.setLatitude(37.5130);
                data.setLongitude(127.1025);
                break;
            case "종로·청계 관광특구":
                data.setLatitude(37.5705);
                data.setLongitude(126.9850);
                break;
            case "홍대 관광특구":
                data.setLatitude(37.5568);
                data.setLongitude(126.9237);
                break;
            case "경복궁":
                data.setLatitude(37.5796);
                data.setLongitude(126.9770);
                break;
            case "광화문·덕수궁":
                data.setLatitude(37.5664);
                data.setLongitude(126.9779);
                break;
            case "보신각":
                data.setLatitude(37.5704);
                data.setLongitude(126.9824);
                break;
            case "서울 암사동 유적":
                data.setLatitude(37.5513);
                data.setLongitude(127.1303);
                break;
            case "창덕궁·종묘":
                data.setLatitude(37.5794);
                data.setLongitude(126.9910);
                break;
            case "가산디지털단지역":
                data.setLatitude(37.4816);
                data.setLongitude(126.8828);
                break;
            case "강남역":
                data.setLatitude(37.4981);
                data.setLongitude(127.0276);
                break;
            case "건대입구역":
                data.setLatitude(37.5405);
                data.setLongitude(127.0701);
                break;
            case "고덕역":
                data.setLatitude(37.5545);
                data.setLongitude(127.1540);
                break;
            case "고속터미널역":
                data.setLatitude(37.5046);
                data.setLongitude(127.0046);
                break;
            case "교대역":
                data.setLatitude(37.4939);
                data.setLongitude(127.0148);
                break;
            case "구로디지털단지역":
                data.setLatitude(37.4854);
                data.setLongitude(126.9014);
                break;
            case "구로역":
                data.setLatitude(37.5030);
                data.setLongitude(126.8827);
                break;
            case "군자역":
                data.setLatitude(37.5573);
                data.setLongitude(127.0794);
                break;
            case "남구로역":
                data.setLatitude(37.4863);
                data.setLongitude(126.8877);
                break;
            case "대림역":
                data.setLatitude(37.4933);
                data.setLongitude(126.8965);
                break;
            case "동대문역":
                data.setLatitude(37.5714);
                data.setLongitude(127.0090);
                break;
            case "뚝섬역":
                data.setLatitude(37.5475);
                data.setLongitude(127.0471);
                break;
            case "미아사거리역":
                data.setLatitude(37.6132);
                data.setLongitude(127.0300);
                break;
            case "발산역":
                data.setLatitude(37.5585);
                data.setLongitude(126.8377);
                break;
            case "북한산우이역":
                data.setLatitude(37.6633);
                data.setLongitude(127.0122);
                break;
            case "사당역":
                data.setLatitude(37.4765);
                data.setLongitude(126.9816);
                break;
            case "삼각지역":
                data.setLatitude(37.5345);
                data.setLongitude(126.9733);
                break;
            case "서울대입구역":
                data.setLatitude(37.4813);
                data.setLongitude(126.9527);
                break;
            case "서울식물원·마곡나루역":
                data.setLatitude(37.5639);
                data.setLongitude(126.8328);
                break;
            case "서울역":
                data.setLatitude(37.5547);
                data.setLongitude(126.9706);
                break;
            case "선릉역":
                data.setLatitude(37.5045);
                data.setLongitude(127.0496);
                break;
            case "성신여대입구역":
                data.setLatitude(37.5926);
                data.setLongitude(127.0163);
                break;
            case "수유역":
                data.setLatitude(37.6387);
                data.setLongitude(127.0251);
                break;
            case "신논현역·논현역":
                data.setLatitude(37.5081);
                data.setLongitude(127.0250);
                break;
            case "신도림역":
                data.setLatitude(37.5086);
                data.setLongitude(126.8912);
                break;
            case "신림역":
                data.setLatitude(37.4848);
                data.setLongitude(126.9291);
                break;
            case "신촌·이대역":
                data.setLatitude(37.5597);
                data.setLongitude(126.9426);
                break;
            case "양재역":
                data.setLatitude(37.4848);
                data.setLongitude(127.0348);
                break;
            case "역삼역":
                data.setLatitude(37.5009);
                data.setLongitude(127.0367);
                break;
            case "연신내역":
                data.setLatitude(37.6190);
                data.setLongitude(126.9217);
                break;
            case "오목교역·목동운동장":
                data.setLatitude(37.5245);
                data.setLongitude(126.8750);
                break;
            case "왕십리역":
                data.setLatitude(37.5614);
                data.setLongitude(127.0370);
                break;
            case "용산역":
                data.setLatitude(37.5299);
                data.setLongitude(126.9646);
                break;
            case "이태원역":
                data.setLatitude(37.5345);
                data.setLongitude(126.9943);
                break;
            case "장지역":
                data.setLatitude(37.4780);
                data.setLongitude(127.1236);
                break;
            case "장한평역":
                data.setLatitude(37.5613);
                data.setLongitude(127.0643);
                break;
            case "천호역":
                data.setLatitude(37.5386);
                data.setLongitude(127.1230);
                break;
            case "총신대입구(이수)역":
                data.setLatitude(37.4860);
                data.setLongitude(126.9823);
                break;
            case "충정로역":
                data.setLatitude(37.5604);
                data.setLongitude(126.9634);
                break;
            case "합정역":
                data.setLatitude(37.5494);
                data.setLongitude(126.9132);
                break;
            case "혜화역":
                data.setLatitude(37.5823);
                data.setLongitude(127.0018);
                break;
            case "홍대입구역(2호선)":
                data.setLatitude(37.5568);
                data.setLongitude(126.9237);
                break;
            case "회기역":
                data.setLatitude(37.5895);
                data.setLongitude(127.0578);
                break;
            case "4·19 카페거리":
                data.setLatitude(37.6490);
                data.setLongitude(127.0137);
                break;
            case "가락시장":
                data.setLatitude(37.4922);
                data.setLongitude(127.1184);
                break;
            case "가로수길":
                data.setLatitude(37.5219);
                data.setLongitude(127.0236);
                break;
            case "광장(전통)시장":
                data.setLatitude(37.5701);
                data.setLongitude(126.9998);
                break;
            case "김포공항":
                data.setLatitude(37.5584);
                data.setLongitude(126.7940);
                break;
            case "낙산공원·이화마을":
                data.setLatitude(37.5794);
                data.setLongitude(127.0062);
                break;
            case "노량진":
                data.setLatitude(37.5130);
                data.setLongitude(126.9404);
                break;
            case "덕수궁길·정동길":
                data.setLatitude(37.5651);
                data.setLongitude(126.9756);
                break;
            case "방배역 먹자골목":
                data.setLatitude(37.4813);
                data.setLongitude(126.9964);
                break;
            case "북촌한옥마을":
                data.setLatitude(37.5826);
                data.setLongitude(126.9831);
                break;
            case "서촌":
                data.setLatitude(37.5792);
                data.setLongitude(126.9718);
                break;
            case "성수카페거리":
                data.setLatitude(37.5446);
                data.setLongitude(127.0555);
                break;
            case "수유리 먹자골목":
                data.setLatitude(37.6341);
                data.setLongitude(127.0212);
                break;
            case "쌍문동 맛집거리":
                data.setLatitude(37.6484);
                data.setLongitude(127.0347);
                break;
            case "압구정로데오거리":
                data.setLatitude(37.5274);
                data.setLongitude(127.0401);
                break;
            case "여의도":
                data.setLatitude(37.5219);
                data.setLongitude(126.9245);
                break;
            case "연남동":
                data.setLatitude(37.5658);
                data.setLongitude(126.9259);
                break;
            case "영등포 타임스퀘어":
                data.setLatitude(37.5172);
                data.setLongitude(126.9030);
                break;
            case "외대앞":
                data.setLatitude(37.5967);
                data.setLongitude(127.0634);
                break;
            case "용리단길":
                data.setLatitude(37.5285);
                data.setLongitude(126.9646);
                break;
            case "이태원 앤틱가구거리":
                data.setLatitude(37.5342);
                data.setLongitude(126.9950);
                break;
            case "인사동·익선동":
                data.setLatitude(37.5748);
                data.setLongitude(126.9854);
                break;
            case "창동 신경제 중심지":
                data.setLatitude(37.6534);
                data.setLongitude(127.0514);
                break;
            case "청담동 명품거리":
                data.setLatitude(37.5242);
                data.setLongitude(127.0492);
                break;
            case "청량리 제기동 일대 전통시장":
                data.setLatitude(37.5801);
                data.setLongitude(127.0384);
                break;
            case "해방촌·경리단길":
                data.setLatitude(37.5417);
                data.setLongitude(126.9876);
                break;
            case "DDP(동대문디자인플라자)":
                data.setLatitude(37.5673);
                data.setLongitude(127.0095);
                break;
            case "DMC(디지털미디어시티)":
                data.setLatitude(37.5771);
                data.setLongitude(126.8986);
                break;
            case "강서한강공원":
                data.setLatitude(37.5585);
                data.setLongitude(126.8377);
                break;
            case "고척돔":
                data.setLatitude(37.4967);
                data.setLongitude(126.8674);
                break;
            case "광나루한강공원":
                data.setLatitude(37.5484);
                data.setLongitude(127.1168);
                break;
            case "광화문광장":
                data.setLatitude(37.5717);
                data.setLongitude(126.9765);
                break;
            case "국립중앙박물관·용산가족공원":
                data.setLatitude(37.5245);
                data.setLongitude(126.9808);
                break;
            case "난지한강공원":
                data.setLatitude(37.5674);
                data.setLongitude(126.8828);
                break;
            case "남산공원":
                data.setLatitude(37.5512);
                data.setLongitude(126.9882);
                break;
            case "노들섬":
                data.setLatitude(37.5132);
                data.setLongitude(126.9534);
                break;
            case "뚝섬한강공원":
                data.setLatitude(37.5313);
                data.setLongitude(127.0668);
                break;
            case "망원한강공원":
                data.setLatitude(37.5520);
                data.setLongitude(126.8960);
                break;
            case "반포한강공원":
                data.setLatitude(37.5126);
                data.setLongitude(126.9957);
                break;
            case "북서울꿈의숲":
                data.setLatitude(37.6204);
                data.setLongitude(127.0408);
                break;
            case "불광천":
                data.setLatitude(37.6100);
                data.setLongitude(126.9200);
                break;
            case "서리풀공원·몽마르뜨공원":
                data.setLatitude(37.4958);
                data.setLongitude(127.0025);
                break;
            case "서울광장":
                data.setLatitude(37.5665);
                data.setLongitude(126.9780);
                break;
            case "서울대공원":
                data.setLatitude(37.4360);
                data.setLongitude(127.0079);
                break;
            case "서울숲공원":
                data.setLatitude(37.5443);
                data.setLongitude(127.0377);
                break;
            case "아차산":
                data.setLatitude(37.5548);
                data.setLongitude(127.1035);
                break;
            case "양화한강공원":
                data.setLatitude(37.5476);
                data.setLongitude(126.9134);
                break;
            case "어린이대공원":
                data.setLatitude(37.5487);
                data.setLongitude(127.0733);
                break;
            case "여의도한강공원":
                data.setLatitude(37.5283);
                data.setLongitude(126.9340);
                break;
            case "월드컵공원":
                data.setLatitude(37.5692);
                data.setLongitude(126.8982);
                break;
            case "응봉산":
                data.setLatitude(37.5600);
                data.setLongitude(127.0227);
                break;
            case "이촌한강공원":
                data.setLatitude(37.5220);
                data.setLongitude(126.9617);
                break;
            case "잠실종합운동장":
                data.setLatitude(37.5112);
                data.setLongitude(127.0717);
                break;
            case "잠실한강공원":
                data.setLatitude(37.5174);
                data.setLongitude(127.0813);
                break;
            case "잠원한강공원":
                data.setLatitude(37.5270);
                data.setLongitude(127.0112);
                break;
            case "청계산":
                data.setLatitude(37.4483);
                data.setLongitude(127.0586);
                break;
            case "청와대":
                data.setLatitude(37.5865);
                data.setLongitude(126.9748);
                break;
            case "북창동 먹자골목":
                data.setLatitude(37.5590);
                data.setLongitude(126.9780);
                break;
            case "남대문시장":
                data.setLatitude(37.5593);
                data.setLongitude(126.9770);
                break;
            default:
                data.setLatitude(0);
                data.setLongitude(0);
                break;
        }
    }



}
