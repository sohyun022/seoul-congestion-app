# 서울시 실시간 인구 혼잡도 서비스

서울 열린 데이터 광장에서 제공하는 '서울 실시간 도시데이터'를 이용하여 서울시의 특정 지역에 대한 실시간 인구 혼잡도 정보를 제공하는 웹 애플리케이션입니다. 사용자는 특정 지역의 혼잡도를 실시간으로 확인하고, 예측 데이터를 차트로 시각화하며, 지도를 통해 위치 정보를 확인할 수 있습니다.

## 주요 기능

- **지역 검색**: 자동완성 검색 창을 통해 서울의 특정 지역을 검색할 수 있습니다.
- **실시간 혼잡도 조회**: 선택한 지역의 실시간 혼잡도를 확인하고 상세 메시지를 제공합니다.
- **혼잡도 예측 데이터**: 향후 12시간 동안의 예측 혼잡도를 차트 형태로 확인할 수 있습니다.
- **인터랙티브 지도**: 선택한 지역의 위치를 카카오 지도에서 확인할 수 있습니다.

## 데모  비디오
![demo video](https://github.com/user-attachments/assets/22249dea-d9b1-4ca2-a72c-563306e09037)

## 기술 스택

- **프론트엔드**: HTML, CSS, JavaScript
- **백엔드**: Java, Spring Boot
- **API 통합**: Kakao Maps API, 서울시 공공데이터 API
- **빌드 도구**: Gradle
- **차트 라이브러리**: Chart.js
- **버전 관리**: Git, GitHub

## 사전 준비 사항

다음 소프트웨어 및 API 키가 필요합니다:
- **Java 17 이상**
- **Gradle**
- **서울시 실시간 도시데이터 API 키** (서울 열린데이터 광장에서 발급)
- **Kakao Maps API 키** (Kakao Developers에서 발급)

## 설치 방법

1. **저장소 클론**:
   ```bash
   git clone https://github.com/sohyun022/seoul-congestion-app.git
   cd seoul-congestion-app
   ```

2. **application.properties 설정**

`src/main/resources/` 디렉토리에 `application.properties` 파일을 생성하고 다음 내용을 추가하세요:

```properties
  spring.application.name=seoul-congestion
  seoul.api.base-url=http://openapi.seoul.go.kr:8088
  seoul.api.request-type=json
  seoul.api.service-name=citydata
  seoul.api.start-index=1
  seoul.api.end-index=10
  spring.web.resources.static-locations=classpath:/static/
  seoul.api.key=YOUR_SEOUL_API_KEY
  kakao.api.key=YOUR_KAKAO_API_KEY
```

3. **애플리케이션 빌드**

빌드된 `.jar` 파일을 사용하여 애플리케이션을 실행합니다:

```bash
java -jar build/libs/seoul-congestion-app.jar
```

5. **웹 애플리케이션 접속**
브라우저에서 `http://localhost:8080`을 열어 애플리케이션을 실행합니다.

