package com.kt.kol.api.bmon.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.kol.common.model.TrtErrInfoDTO;
import com.kt.kol.common.util.HeaderConstants;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("BMONSender 테스트")
class BMONSenderTest {

    @Mock
    private ServerHttpRequest mockRequest;

    @Mock
    private HttpHeaders mockHeaders;

    private BMONSender bmonSender;
    private ObjectMapper objectMapper;
    private TrtErrInfoDTO testTrtErrInfo;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        bmonSender = new BMONSender(objectMapper);

        // BMONSender 설정값 주입
        ReflectionTestUtils.setField(bmonSender, "bmonTrtFlag", true);
        ReflectionTestUtils.setField(bmonSender, "onProfile", "test");
        ReflectionTestUtils.setField(bmonSender, "apiSrcName", "test-api");

        // 테스트용 TrtErrInfo 생성
        testTrtErrInfo = new TrtErrInfoDTO("I", "0000", "성공", "정상처리");

        // Mock 설정
        when(mockRequest.getHeaders()).thenReturn(mockHeaders);
        setupMockHeaders();
    }

    private void setupMockHeaders() {
        when(mockHeaders.getFirst(HeaderConstants.HEADER_ORI_URI)).thenReturn("/test/api");
        when(mockHeaders.getFirst(HeaderConstants.HEADER_GLOBAL_NO)).thenReturn("TEST12345");
        when(mockHeaders.getFirst(HeaderConstants.HEADER_CHNL_TYPE)).thenReturn("WEB");
        when(mockHeaders.getFirst(HeaderConstants.HEADER_ORI_IP)).thenReturn("127.0.0.1");
        when(mockHeaders.getFirst(HeaderConstants.HEADER_USER_ID)).thenReturn("testuser");
        when(mockHeaders.getFirst(HeaderConstants.HEADER_ORG_ID)).thenReturn("TEST_ORG");
        when(mockHeaders.getFirst(HeaderConstants.HEADER_LG_DATE_TIME)).thenReturn("20240818120000");
        when(mockHeaders.getFirst(HeaderConstants.HEADER_CMPN_CD)).thenReturn("KT");
    }

    @Test
    @DisplayName("BMON Flag가 false일 때 빈 Mono 반환")
    void testBmonFlagFalse() {
        // Given
        ReflectionTestUtils.setField(bmonSender, "bmonTrtFlag", false);
        TestDTO testDto = new TestDTO("test", 123);

        // When & Then
        StepVerifier.create(bmonSender.sendBmonMot("T", testDto, testTrtErrInfo, mockRequest))
                .verifyComplete();
    }

    @Test
    @DisplayName("정상적인 BMON 전송 - T 플래그")
    void testNormalBmonSendWithTFlag() {
        // Given
        TestDTO testDto = new TestDTO("test", 123);

        // When
        Mono<Void> result = bmonSender.sendBmonMot("T", testDto, testTrtErrInfo, mockRequest);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        System.out.println("✅ T 플래그 테스트 성공");
    }

    @Test
    @DisplayName("정상적인 BMON 전송 - R 플래그")
    void testNormalBmonSendWithRFlag() {
        // Given
        TestDTO testDto = new TestDTO("test", 123);

        // When
        Mono<Void> result = bmonSender.sendBmonMot("R", testDto, testTrtErrInfo, mockRequest);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        System.out.println("✅ R 플래그 테스트 성공");
    }

    @Test
    @DisplayName("null DTO와 함께 BMON 전송")
    void testBmonSendWithNullDto() {
        // When
        Mono<Void> result = bmonSender.sendBmonMot("T", null, testTrtErrInfo, mockRequest);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        System.out.println("✅ Null DTO 테스트 성공");
    }

    @Test
    @DisplayName("복잡한 객체 구조 테스트")
    void testComplexObjectStructure() {
        // Given
        ComplexTestDTO complexDto = createComplexTestDto();

        // When
        Mono<Void> result = bmonSender.sendBmonMot("T", complexDto, testTrtErrInfo, mockRequest);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        System.out.println("✅ 복잡한 객체 구조 테스트 성공");
    }

    @Test
    @DisplayName("Header 누락 상황 테스트")
    void testMissingHeaders() {
        // Given
        when(mockHeaders.getFirst(anyString())).thenReturn(null);
        TestDTO testDto = new TestDTO("test", 123);

        // When
        Mono<Void> result = bmonSender.sendBmonMot("T", testDto, testTrtErrInfo, mockRequest);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        System.out.println("✅ Header 누락 테스트 성공");
    }

    @Test
    @DisplayName("buildBmonHeader 메서드 테스트")
    void testBuildBmonHeader() throws Exception {
        // Given
        java.lang.reflect.Method method = BMONSender.class.getDeclaredMethod(
                "buildBmonHeader", String.class, TrtErrInfoDTO.class, ServerHttpRequest.class);
        method.setAccessible(true);

        // When
        String result = (String) method.invoke(bmonSender, "T", testTrtErrInfo, mockRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("appName="));
        assertTrue(result.contains("trFlag=T"));
        assertTrue(result.contains("responseType=I"));
        assertTrue(result.contains("svcName=/test/api"));
    }

    @Test
    @DisplayName("buildBmonBody 메서드 테스트")
    void testBuildBmonBody() throws Exception {
        // Given
        TestDTO testDto = new TestDTO("test", 123);
        java.lang.reflect.Method method = BMONSender.class.getDeclaredMethod(
                "buildBmonBody", String.class, Object.class, TrtErrInfoDTO.class);
        method.setAccessible(true);

        // When
        String result = (String) method.invoke(bmonSender, "T", testDto, testTrtErrInfo);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("name:test"));
        assertTrue(result.contains("value:123"));
    }

    @Test
    @DisplayName("성능 테스트 - 1000회 호출")
    void testPerformance() {
        // Given
        TestDTO testDto = new TestDTO("performance", 999);
        int iterations = 1000;

        // When
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            StepVerifier.create(bmonSender.sendBmonMot("T", testDto, testTrtErrInfo, mockRequest))
                    .verifyComplete();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then
        System.out.println("=== 성능 테스트 결과 ===");
        System.out.println("총 호출 횟수: " + iterations);
        System.out.println("총 소요 시간: " + duration + "ms");
        System.out.println("평균 처리 시간: " + (duration / (double) iterations) + "ms");
        System.out.println("초당 처리량: " + (iterations * 1000.0 / duration) + " TPS");

        // 성능 임계값 확인 (평균 5ms 이하)
        assertTrue(duration / (double) iterations < 5.0,
                "평균 처리 시간이 5ms를 초과했습니다: " + (duration / (double) iterations) + "ms");
    }

    // 테스트용 DTO 클래스들
    public static class TestDTO {
        private String name;
        private int value;

        public TestDTO() {
        }

        public TestDTO(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    public static class ComplexTestDTO {
        private String id;
        private Map<String, Object> metadata;
        private List<TestDTO> items;

        public ComplexTestDTO() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }

        public List<TestDTO> getItems() {
            return items;
        }

        public void setItems(List<TestDTO> items) {
            this.items = items;
        }
    }

    private ComplexTestDTO createComplexTestDto() {
        ComplexTestDTO complexDto = new ComplexTestDTO();
        complexDto.setId("complex-123");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", "test");
        metadata.put("priority", 1);
        metadata.put("tags", Arrays.asList("tag1", "tag2"));
        complexDto.setMetadata(metadata);

        List<TestDTO> items = Arrays.asList(
                new TestDTO("item1", 100),
                new TestDTO("item2", 200));
        complexDto.setItems(items);

        return complexDto;
    }
}