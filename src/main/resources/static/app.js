document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("searchForm");
    const areaInput = document.getElementById("areaName");
    const resultDiv = document.getElementById("result");
    const autoCompleteList = document.querySelector(".autocomplete-list");
    const forecastSection = document.getElementById("forecast-section");
    const mapSection = document.getElementById("map-section");

    // 자동완성 데이터
    const autoCompleteData = [
        "강남 MICE 관광특구", "동대문 관광특구", "명동 관광특구", "이태원 관광특구", "잠실 관광특구", "종로·청계 관광특구", "홍대 관광특구", "경복궁", "광화문·덕수궁", "보신각", "서울 암사동 유적",
        "창덕궁·종묘", "가산디지털단지역", "강남역", "건대입구역", "고덕역", "고속터미널역", "교대역", "구로디지털단지역", "구로역", "군자역", "남구로역", "대림역", "동대문역", "뚝섬역", "미아사거리역",
        "발산역", "북한산우이역", "사당역", "삼각지역", "서울대입구역", "서울식물원·마곡나루역", "서울역", "선릉역", "성신여대입구역", "수유역", "신논현역·논현역", "신도림역", "신림역", "신촌·이대역",
        "양재역", "역삼역", "연신내역", "오목교역·목동운동장", "왕십리역", "용산역", "이태원역", "장지역", "장한평역", "천호역", "총신대입구(이수)역", "충정로역", "합정역", "혜화역", "홍대입구역(2호선)",
        "회기역", "4·19 카페거리", "가락시장", "가로수길", "광장(전통)시장", "김포공항", "낙산공원·이화마을", "노량진", "덕수궁길·정동길", "방배역 먹자골목", "북촌한옥마을", "서촌", "성수카페거리", "수유리 먹자골목",
        "쌍문동 맛집거리", "압구정로데오거리", "여의도", "연남동", "영등포 타임스퀘어", "외대앞", "용리단길", "이태원 앤틱가구거리", "인사동·익선동", "창동 신경제 중심지", "청담동 명품거리", "청량리 제기동 일대 전통시장",
        "해방촌·경리단길", "DDP(동대문디자인플라자)", "DMC(디지털미디어시티)", "강서한강공원", "고척돔", "광나루한강공원", "광화문광장", "국립중앙박물관·용산가족공원", "난지한강공원", "남산공원", "노들섬", "뚝섬한강공원",
        "망원한강공원", "반포한강공원", "북서울꿈의숲", "불광천", "서리풀공원·몽마르뜨공원", "서울광장", "서울대공원", "서울숲공원", "아차산", "양화한강공원", "어린이대공원", "여의도한강공원", "월드컵공원", "응봉산", "이촌한강공원",
        "잠실종합운동장", "잠실한강공원", "잠원한강공원", "청계산", "청와대", "북창동 먹자골목", "남대문시장"
    ];

    // 자동완성 기능: 입력 이벤트 처리
    areaInput.addEventListener("input", () => {
        const inputValue = areaInput.value.toLowerCase();
        const filteredData = autoCompleteData.filter(item =>
            item.toLowerCase().startsWith(inputValue)
        );

        autoCompleteList.innerHTML = ""; // 이전 결과 초기화

        filteredData.forEach(item => {
            const option = document.createElement("div");
            option.classList.add("autocomplete-option");
            option.textContent = item;
            option.addEventListener("click", () => {
                areaInput.value = item; // 선택된 값 설정
                autoCompleteList.style.display = "none"; // 자동완성 닫기
            });
            autoCompleteList.appendChild(option);
        });

        autoCompleteList.style.display = filteredData.length > 0 ? "block" : "none";
    });

    document.addEventListener("click", (event) => {
        if (!event.target.closest(".autocomplete-container")) {
            autoCompleteList.style.display = "none"; // 클릭 외부 이벤트로 자동완성 닫기
        }
    });

    // 폼 제출 이벤트 처리
    form.addEventListener("submit", (event) => {
        event.preventDefault(); // 기본 동작 방지
        const areaName = areaInput.value.trim();

        if (!areaName) {
            alert("Please enter an area name!");
            return;
        }

        // API 요청
        fetch(`/api/congestion/${areaName}`)
            .then(response => {
                if (response==null) {
                    throw new Error("No data found for the specified area.");
                }
                return response.json();
            })
            .then(data => {
                console.log("Parsed API Data:", data);

                // Map 렌더링
                initKakaoMap(data.latitude, data.longitude);

                // 결과 표시
                renderResult(data);
                renderChart(data.forecastTimes, data.forecastCongestions);

                // 섹션 표시
                forecastSection.style.display = "block";
                mapSection.style.display = "block";

            })
            .catch(error => {
                console.error("Error:", error);
                resultDiv.innerHTML = `<p style="color: red;">${error.message}</p>`;
            });
    });

    // 결과 렌더링
    function renderResult(data) {
        resultDiv.innerHTML = `
            <h2>Live Congestion</h2>
            <table>
                <tr>
                    <th>Measurement Time</th>
                    <td>${data.populationTime}</td>
                </tr>
                <tr>
                    <th>Area Name</th>
                    <td>${data.areaName}</td>
                </tr>
                <tr>
                    <th>Congestion Level</th>
                    <td>${data.areaCongestLevel}</td>
                </tr>
                <tr>
                    <th>Congestion Message</th>
                    <td>${data.areaCongestMsg}</td>
                </tr>
                <tr>
                    <th>Traffic Level</th>
                    <td>${data.roadCongestIdx}</td>
                </tr>
                <tr>
                    <th>Traffic Message</th>
                    <td>${data.roadCongestMsg}</td>
                </tr>
            </table>
        `;
    }

    let forecastChart;

    // 차트 렌더링
    function renderChart(forecastTimes, forecastCongestions) {

        // 데이터 확인
        console.log("Times:", forecastTimes);
        console.log("Congestions:", forecastCongestions);

        const canvas = document.getElementById("forecastChart");
        const ctx = canvas.getContext("2d");

        // Canvas context 확인
        console.log("Canvas Context:", ctx);

        // 기존 차트 삭제
        if (forecastChart) {
            console.log("Destroying existing chart");
            forecastChart.destroy();
        }

        // 새로운 차트 생성
        forecastChart = new Chart(ctx, {
            type: "line",
            data: {
                labels: forecastTimes,
                datasets: [{
                    label: "congestion level",
                    data: forecastCongestions,
                    backgroundColor: "rgba(75, 192, 192, 0.2)",
                    borderColor: "rgba(75, 192, 192, 1)",
                    borderWidth: 2
                }]
            },
            options: {
                scales: {
                    y: {
                        type: "category",
                        labels: ["붐빔", "약간 붐빔", "보통", "여유"],
                        ticks: {
                            font: {
                                size: 13
                            }
                        }
                    },
                    x: {
                        ticks: {
                            font: {
                                size: 13
                            }
                        }
                    }
                }
            }
        });
    }

    // 카카오 지도 초기화 함수
    function initKakaoMap(latitude, longitude) {

        const container = document.getElementById("map");
        const options = {
            center: new kakao.maps.LatLng(latitude, longitude),
            level: 3
        };
        const map = new kakao.maps.Map(container, options);

        // 지도에 교통정보를 표시하도록 지도타입 추가
        map.addOverlayMapTypeId(kakao.maps.MapTypeId.TRAFFIC);

        const marker = new kakao.maps.Marker({
            position: new kakao.maps.LatLng(latitude, longitude),
            map: map
        });
    }

});
