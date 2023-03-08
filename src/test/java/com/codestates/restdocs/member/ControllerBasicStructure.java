package com.codestates.restdocs.member;

import com.codestates.member.controller.MemberController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

@WebMvcTest(MemberController.class)   // Controller를 테스트 하기 위한 전용 애너테이션
@MockBean(JpaMetamodelMappingContext.class)   // JPA에서 사용하는 Bean 들을 Mock 객체로 주입해주는 설정
@AutoConfigureRestDocs    // Spring Rest Docs 자동 구성해주는 애너테이션
public class ControllerBasicStructure {
    @Autowired
    private MockMvc mockMvc;

 //   @MockBean // Mock 객체를 주입, Controller 클래스가 의존하는 객체의 의존성을 제거하기 위해 사용

    @Test
    public void postMemberTest() throws Exception {
        // given
        // 테스트 데이터

        // Mock 객체를 이용한 Stubbing

        // when
//        ResultActions actions =
//                mockMvc.perform(
//                        // request 전송
//                );
//
//        // then
//        actions
//                .andExpect(// response에 대한 기대 값 검증)
//                .andDo(document( // document() => API 문서를 생성 하기 위해 Spring Rest Docs에서 지원하는 메서드
//                        //  API 문서 스펙 정보 추가
//                ));
    }
}
