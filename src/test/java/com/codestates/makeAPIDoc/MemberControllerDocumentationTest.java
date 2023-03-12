package com.codestates.makeAPIDoc;

import com.codestates.member.controller.MemberController;
import com.codestates.member.dto.MemberDto;
import com.codestates.member.entity.Member;
import com.codestates.member.mapper.MemberMapper;
import com.codestates.member.service.MemberService;
import com.codestates.stamp.Stamp;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;

import static com.codestates.util.ApiDocumentUtils.getRequestPreProcessor;
import static com.codestates.util.ApiDocumentUtils.getResponsePreProcessor;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
public class MemberControllerDocumentationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private MemberMapper mapper;

    @Autowired
    private Gson gson;

    @Test
    public void postMemberTest() throws Exception {
        // given
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com", "홍길동", "010-1234-5678");
        String content = gson.toJson(post);

        MemberDto.Response responseDto = makeMemberResponse();

        // willReturn()이 최소한 null은 아니어야 한다.
        given(mapper.memberPostToMember(Mockito.any(MemberDto.Post.class))).willReturn(new Member());

        Member mockResultMember = new Member();
        mockResultMember.setMemberId(1L);
        given(memberService.createMember(Mockito.any(Member.class))).willReturn(mockResultMember);

        given(mapper.memberToMemberResponse(Mockito.any(Member.class))).willReturn(responseDto);

        // when
        ResultActions actions =
                mockMvc.perform(
                        post("/v11/members")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        // then
        actions
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", is(startsWith("/v11/members/"))))
                .andDo(document("post-member",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestFields(
                                List.of(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                                        fieldWithPath("phone").type(JsonFieldType.STRING).description("휴대폰 번호")
                                )
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location header. 등록된 리소스의 URI")
                        )
                ));
    }

    @Test
    public void patchMemberTest() throws Exception {
        // given
        long memberId = 1L;
        MemberDto.Patch patch = new MemberDto.Patch(memberId, "홍길동", "010-1111-1111", Member.MemberStatus.MEMBER_ACTIVE);
        String content = gson.toJson(patch);

        MemberDto.Response responseDto =
                new MemberDto.Response(1L,
                        "hgd@gmail.com",
                        "홍길동",
                        "010-1111-1111",
                        Member.MemberStatus.MEMBER_ACTIVE,
                        new Stamp());

        // willReturn()이 최소한 null은 아니어야 한다.
        given(mapper.memberPatchToMember(Mockito.any(MemberDto.Patch.class))).willReturn(new Member());

        given(memberService.updateMember(Mockito.any(Member.class))).willReturn(new Member());

        given(mapper.memberToMemberResponse(Mockito.any(Member.class))).willReturn(responseDto);

        // when
        ResultActions actions =
                mockMvc.perform(
                        patch("/v11/members/{member-id}", memberId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberId").value(patch.getMemberId()))
                .andExpect(jsonPath("$.data.name").value(patch.getName()))
                .andExpect(jsonPath("$.data.phone").value(patch.getPhone()))
                .andExpect(jsonPath("$.data.memberStatus").value(patch.getMemberStatus().getStatus()))
                .andDo(document("patch-member",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(
                                parameterWithName("member-id").description("회원 식별자")
                        ),
                        requestFields(
                                List.of(
                                        fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("회원 식별자").ignored(),
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("이름").optional(),
                                        fieldWithPath("phone").type(JsonFieldType.STRING).description("휴대폰 번호").optional(),
                                        fieldWithPath("memberStatus").type(JsonFieldType.STRING).description("회원 상태: MEMBER_ACTIVE / MEMBER_SLEEP / MEMBER_QUIT").optional()
                                )
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("결과 데이터"),
                                        fieldWithPath("data.memberId").type(JsonFieldType.NUMBER).description("회원 식별자"),
                                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("data.name").type(JsonFieldType.STRING).description("이름"),
                                        fieldWithPath("data.phone").type(JsonFieldType.STRING).description("휴대폰 번호"),
                                        fieldWithPath("data.memberStatus").type(JsonFieldType.STRING).description("회원 상태: 활동중 / 휴면 상태 / 탈퇴 상태"),
                                        fieldWithPath("data.stamp").type(JsonFieldType.NUMBER).description("스탬프 갯수")
                                )
                        )
                ));
    }

    @Test
    void getMemberTest() throws Exception {
        MemberDto.Response response = makeMemberResponse();

        given(memberService.findMember(Mockito.anyLong()))
                .willReturn(new Member());
        given(mapper.memberToMemberResponse(Mockito.any(Member.class)))
                .willReturn(response);


        mockMvc.perform(
                get("/v11/members/{member-id}", response.getMemberId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.data.name").value(response.getName()),
                jsonPath("$.data.phone").value(response.getPhone())
        ).andDo(document("get-member",
                getRequestPreProcessor(),
                getResponsePreProcessor(),
                pathParameters(
                        parameterWithName("member-id").description("회원 식별자")
                ),
                responseFields(
                        List.of(
//                                fieldWithPath("uri").type(JsonFieldType.STRING).description("요청한 리소스의 URI 정보"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("Member 정보"),
                                fieldWithPath("data.memberId").type(JsonFieldType.NUMBER).description("회원 식별자"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("이름"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data.phone").type(JsonFieldType.STRING).description("휴대폰 번호"),
                                fieldWithPath("data.memberStatus").type(JsonFieldType.STRING).description("회원 상태: 활동중 / 휴면 상태 / 탈퇴 상태"),
                                fieldWithPath("data.stamp").type(JsonFieldType.NUMBER).description("스탬프 갯수")
                        )
                )
        ));

    }

    @Test
    void getMembersTest() throws Exception {
        Member member1 = makeMember();
        Member member2 = makeMember2();

        MemberDto.Response response = makeMemberResponse();
        MemberDto.Response response2 = makeMemberResponse2();

        int page = 1;
        int size = 10;

//        MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<>();
//        multiValueMap.add("page", page+"");
//        multiValueMap.add("size", size+"");

        Page<Member> pageList = new PageImpl<>(List.of(member1,member2), PageRequest.of(page-1,size, Sort.by("memberId").descending()),2);
        List<MemberDto.Response> memberList = new ArrayList<>();
        memberList.add(response);
        memberList.add(response2);

        given(memberService.findMembers(Mockito.anyInt(),Mockito.anyInt()))
                .willReturn(pageList);
        given(mapper.membersToMemberResponses(Mockito.anyList()))
                .willReturn(memberList);

        mockMvc.perform(
                get("/v11/members?page="+page+"&size="+size)
//                get("/v11/members")
//                        .params(multiValueMap) // 이렇게 해도 됨
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.data[0].email").value(member1.getEmail()), // 페이지네이션 정렬에 따른 순서 주의
                jsonPath("$.data[1].email").value(member2.getEmail())
        ).andDo(document("get-members",
                getRequestPreProcessor(),
                getResponsePreProcessor(),
                requestParameters(
                        List.of(
                                parameterWithName("page").description("페이지 수"),
                                parameterWithName("size").description("페이지 당 Member 갯수")
                        )
                ), responseFields(
                        List.of(
//                                fieldWithPath("uri").type(JsonFieldType.STRING).description("요청한 리소스의 URI 정보"),
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("결과 데이터"),
                                fieldWithPath("data[].memberId").type(JsonFieldType.NUMBER).description("회원 식별자"),
                                fieldWithPath("data[].email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data[].name").type(JsonFieldType.STRING).description("이름"),
                                fieldWithPath("data[].phone").type(JsonFieldType.STRING).description("휴대폰 번호"),
                                fieldWithPath("data[].memberStatus").type(JsonFieldType.STRING).description("회원 상태: 활동중 / 휴면 상태 / 탈퇴 상태"),
                                fieldWithPath("data[].stamp").type(JsonFieldType.NUMBER).description("스탬프 갯수"),
                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT).description("페이지 정보"),
                                fieldWithPath("pageInfo.page").type(JsonFieldType.NUMBER).description("페이지 수"),
                                fieldWithPath("pageInfo.size").type(JsonFieldType.NUMBER).description("한 페이지의 갯수"),
                                fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("총 원소"),
                                fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수")
                        )
                )));
    }

    @Test
    void deleteMemberTest() throws Exception { // 결론적으로 spring연결 확인하는 정도 밖에 의미 없음

        doNothing().when(memberService).deleteMember(Mockito.anyLong()); // 그나마 하나 있는 기능 아무것도 안하게 만들기

        mockMvc.perform(
                        delete("/v11/members/{member-id}", 1L)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isNoContent())
                .andDo(document(
                        "delete-member",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(
                                parameterWithName("member-id").description("회원 식별자")
                        )
                ));
    }

    public MemberDto.Response makeMemberResponse () {
        MemberDto.Response response = new MemberDto.Response(1L,
                "hgd@gmail.com",
                "홍길동",
                "010-1111-1111",
                Member.MemberStatus.MEMBER_ACTIVE,
                new Stamp());

        return response;
    }

    public MemberDto.Response makeMemberResponse2 () {
        MemberDto.Response response = new MemberDto.Response(2L,
                "hgd2@gmail.com",
                "둘길동",
                "010-2222-2222",
                Member.MemberStatus.MEMBER_ACTIVE,
                new Stamp());

        return response;
    }

    public Member makeMember(){
        Member member = new Member("hgd@gmail.com", "홍길동", "010-1234-5678");
        member.setStamp(new Stamp());
        member.setMemberStatus(Member.MemberStatus.MEMBER_ACTIVE);
        member.setMemberId(1L);

        return member;
    }

    public Member makeMember2(){
        Member member = new Member("hgd2@gmail.com", "둘길동", "010-2222-2222");
        member.setStamp(new Stamp());
        member.setMemberStatus(Member.MemberStatus.MEMBER_ACTIVE);
        member.setMemberId(2L);

        return member;
    }
}

