[블로그 포스팅 주소](https://velog.io/@wish17/%EC%BD%94%EB%93%9C%EC%8A%A4%ED%85%8C%EC%9D%B4%EC%B8%A0-%EB%B0%B1%EC%97%94%EB%93%9C-%EB%B6%80%ED%8A%B8%EC%BA%A0%ED%94%84-57%EC%9D%BC%EC%B0%A8-Spring-MVC-API-%EB%AC%B8%EC%84%9C%ED%99%94)

# [Spring MVC] API 문서화

# API 문서화

> 클라이언트가 REST API 백엔드 애플리케이션에 요청을 전송하기 위해서 알아야 되는 요청 정보(요청 URL(또는 URI), request body, query parameter 등)를 문서로 잘 정리하는 것

> API 문서 or API 스펙(사양, Specification)
- API 사용을 위한 어떤 정보가 담겨 있는 문서

##  Swagger

```java
@ApiOperation(value = "회원 정보 API", tags = {"Member Controller"})
@RestController
@RequestMapping("/v11/swagger/members")
@Validated
@Slf4j
public class MemberControllerSwaggerExample {
    private final MemberService memberService;
    private final MemberMapper mapper;

    public MemberControllerSwaggerExample(MemberService memberService, MemberMapper mapper) {
        this.memberService = memberService;
        this.mapper = mapper;
    }

    @ApiOperation(value = "회원 정보 등록", notes = "회원 정보를 등록합니다.")

    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "회원 등록 완료"),
            @ApiResponse(code = 404, message = "Member not found")
    })
    @PostMapping
    public ResponseEntity postMember(@Valid @RequestBody MemberDto.Post memberDto) {
        Member member = mapper.memberPostToMember(memberDto);
        member.setStamp(new Stamp()); // homework solution 추가

        Member createdMember = memberService.createMember(member);

        return new ResponseEntity<>(
                new SingleResponseDto<>(mapper.memberToMemberResponse(createdMember)),
                HttpStatus.CREATED);
    }

    @ApiOperation(value = "회원 정보 조회", notes = "회원 식별자(memberId)에 해당하는 회원을 조회합니다.")
    @GetMapping("/{member-id}")
    public ResponseEntity getMember(
            @ApiParam(name = "member-id", value = "회원 식별자", example = "1")  // (5)
            @PathVariable("member-id") @Positive long memberId) {
        Member member = memberService.findMember(memberId);
        return new ResponseEntity<>(
                new SingleResponseDto<>(mapper.memberToMemberResponse(member))
                                    , HttpStatus.OK);
    }

}
```

### Swagger의 API 문서화 방식

- 애터네이션 기반의 API 문서화 방식
- 애플리케이션 코드에 문서화를 위한 애너테이션들이 포함된다.
- 가독성 및 유지 보수성이 떨어진다.
- API 문서와 API 코드 간의 정보 불일치 문제가 발생할 수 있다.
- API 툴로써의 기능을 활용할 수 있다.


###  Swagger 장단점

단점
- 애너테이션이 많이 추가되어야 함
    - 가시성 bad
    - 코드량 많음
-  API 스펙 정보를 문자열로 입력하는 경우가 많다.
    - 실수가능성 높음

장점
- Postman처럼 API 요청 툴로써의 기능을 사용할 수 있다

***

## Spring Rest Docs

>Spring Rest Docs
- REST API 문서를 자동으로 생성해 주는 Spring 하위 프로젝트

[연습 코드 GitHub주소](https://github.com/wish9/Practice-api-documentation/commit/9235e5cd95e5acbc1b0710b94086a07d81d25924)

### Spring Rest Docs의 API 문서화 방식
- 테스트 코드 기반의 API 문서화 방식
- 애플리케이션 코드에 문서화를 위한 정보들이 포함되지 않는다.
- 테스트 케이스의 실행이 “passed”여야 API 문서가 생성된다.
- 테스트 케이스를 반드시 작성해야된다.
- API 툴로써의 기능은 제공하지 않는다.


### Spring Rest Docs 장단점

장점
- 테스트 케이스로 API 문서를 만든다.
    - 추가되는 코드량이 적다.
    -  테스트가 통과 되어야지만 API 문서가 정상적으로 만들어 진다
    (실수 방지 가능)
    - API 스펙 정보와 API 문서 내의 정보가 일치한다고 보장 가능

단점
- 테스트 케이스를 무조건 성공(passed)하게 **만들어** 줘야한다.


### Spring Rest Docs의 API 문서 생성 흐름

[![](https://velog.velcdn.com/images/wish17/post/78d512d9-84e5-49b5-a26b-21b2338b59c6/image.png)](https://wjcodding.tistory.com/69)

>스니핏(snippet)
- 일부 조각을 의미
- 테스트 케이스 하나 당 하나의 스니핏이 생성
(여러개의 스니핏을 모아서 하나의 API 문서를 생성할 수 있다.)


### Spring Rest Docs 설정

- Spring Rest Docs를 사용해서 API 문서를 생성하기 위해서는 .adoc 문서 스니핏을 생성해주는 Asciidoctor가 필요하다.

#### build.gradle 설정

```java
plugins {
	id 'org.springframework.boot' version '2.7.1'
	id 'io.spring.dependency-management' version '1.0.11.RELEASE'
	id "org.asciidoctor.jvm.convert" version "3.3.2"    // .adoc 파일 확장자를 가지는 AsciiDoc 문서를 생성해주는 Asciidoctor를 사용하기 위한 플러그인을 추가
	id 'java'
}

group = 'com.codestates'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

repositories {
	mavenCentral()
}

ext { // API 문서 스니핏이 생성될 경로를 지정
	set('snippetsDir', file("build/generated-snippets"))
}

configurations { // AsciiDoctor에서 사용되는 의존 그룹을 지정
	asciidoctorExtensions
}

dependencies {
	testImplementation 'org.springframework.restdocs:spring-restdocs-mockmvc' // spring-restdocs-core와 spring-restdocs-mockmvc 의존 라이브러리 추가

	asciidoctorExtensions 'org.springframework.restdocs:spring-restdocs-asciidoctor' // asciidoctorExtensions 그룹에  spring-restdocs-asciidoctor 의존 라이브러리를 추가

	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-validation'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	compileOnly 'org.projectlombok:lombok'
	runtimeOnly 'com.h2database:h2'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	implementation 'org.mapstruct:mapstruct:1.5.1.Final'
	annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.1.Final'
	implementation 'org.springframework.boot:spring-boot-starter-mail'

	implementation 'com.google.code.gson:gson'
}

tasks.named('test') {// :test task 실행 시
	outputs.dir snippetsDir
	useJUnitPlatform() // 스니핏 디렉토리 경로 설정
}

tasks.named('asciidoctor') { // :asciidoctor task 실행 시
	configurations "asciidoctorExtensions" // Asciidoctor 기능을 사용하기 위해 :asciidoctor task에 asciidoctorExtensions 을 설정
	inputs.dir snippetsDir
	dependsOn test
}

// :build 실행 전에 실행되는 task
task copyDocument(type: Copy) {
	dependsOn asciidoctor            // [:asciidoctor]가 실행된 후에 task가 실행 되도록 의존성을 설정
	from file("${asciidoctor.outputDir}")   //  "build/docs/asciidoc/" 경로에 생성되는 index.html을 copy
	into file("src/main/resources/static/docs")   // 괄호안 경로에 복사한 index.html을 추가
}

build {
	dependsOn copyDocument  // :build task가 실행되기 전에 :copyDocument task가 먼저 수행 되도록 의존성 설정
}

// 애플리케이션 실행 파일이 생성하는 :bootJar task 설정
bootJar {
	dependsOn copyDocument    // :bootJar task 실행 전에 :copyDocument task가 실행 되도록 의존성을 설정
	from ("${asciidoctor.outputDir}") {
		into 'static/docs'     // Asciidoctor 실행으로 생성되는 index.html 파일을 jar 파일 안에 추가
	} // jar 파일에 index.html을 추가해 줌으로써 웹 브라우저에서 접속(http://localhost:8080/docs/index.html) 후, API 문서를 확인할 수 있게 된다.
}
```

- HTML 파일로 변환된 API 문서는 외부에 제공할 수도 있고, 웹브라우저에 접속해서 API 문서를 확인할 수도 있다.

####  API 문서 스니핏을 사용하기 위한 템플릿(또는 source 파일) 생성

1. ``src/docs/asciidoc/`` 경로에 해당하는 디렉토리를 생성
(Gradle 프로젝트의 경우, 템플릿 문서가 위치하는 디폴트 경로가 ``src/docs/asciidoc``다)
2. 위 경로에 비어있는 템플릿 문서(index.adoc)를 생성

![](https://velog.velcdn.com/images/wish17/post/e2e3bb39-1857-4d06-bba3-dc96c3a7918a/image.png)

***

## Controller 테스트 케이스에 Spring RestDocs 적용하기

``@WebMvcTest(컨트롤러클래스명.class)``
- Controller를 테스트 하기 위한 전용 애너테이션

``@MockBean(JpaMetamodelMappingContext.class)``
- JPA에서 사용하는 Bean 들을 Mock 객체로 주입해주는 설정

``@AutoConfigureRestDocs``
- Spring Rest Docs 자동 구성해주는 애너테이션

``@MockBean``
- Mock 객체를 주입
- Controller 클래스가 의존하는 객체의 의존성을 제거하기 위해 사용

```java
ResultActions actions =  request 전송
             
        actions
                .andExpect(status().isCreated()) // response에 대한 기대 값 검증
                .andExpect(header().string("Location", is(startsWith("/v11/members/"))))
                .andDo(document("post-member",    //  API 문서 스펙 정보 추가
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
```

([request 전송 방법](https://velog.io/@wish17/%EC%BD%94%EB%93%9C%EC%8A%A4%ED%85%8C%EC%9D%B4%EC%B8%A0-%EB%B0%B1%EC%97%94%EB%93%9C-%EB%B6%80%ED%8A%B8%EC%BA%A0%ED%94%84-56%EC%9D%BC%EC%B0%A8-Spring-MVC-%ED%85%8C%EC%8A%A4%ED%8C%85Testing2#%EC%8B%A4%EC%8A%B5-1-mockito-%EC%82%AC%EC%9A%A9%ED%95%9C-%EC%8A%AC%EB%9D%BC%EC%9D%B4%EC%8A%A4slice-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EC%BC%80%EC%9D%B4%EC%8A%A4-%EC%9E%91%EC%84%B1)은 이전에 한 실습 참고)

위와 같이 response 검증 후 ``andDo(document())`` 메서드를 통해 API 문서를 생성한다.

### @SpringBootTest vs @WebMvcTest

#### @SpringBootTest + @AutoConfigureMockMvc

- ``@SpringBootTest`` 애너테이션은 ``@AutoConfigureMockMvc`` 과 함께 사용되어 Controller를 테스트 할 수 있다.
    - 전체 Bean을 ApplicationContext에 등록하여 사용
    - 테스트 환경 구성 편리, 실행 속도는 느림
    - 데이터베이스까지 요청 프로세스가 이어지는 통합 테스트에 주로 사용
    
#### @WebMvcTest

- ``@WebMvcTest`` 애너테이션은 Controller 테스트에 필요한 Bean만 ApplicationContext에 등록한다.
    - 실행 속도가 빠름
    - Controller에서 의존하고 있는 객체가 있다면 해당 객체에 대해서 Mock 객체를 사용하여 의존성을 제거 필요
    - Controller를 위한 슬라이스 테스트에 주로 사용




***

## 스니핏을 이용한 API 문서화


### 템플릿 문서 내용 추가


```http
= 커피 주문 애플리케이션    // API 문서의 제목
:sectnums:
:toc: left
:toclevels: 4
:toc-title: Table of Contents
:source-highlighter: prettify

We Won Jong <wjwee9@gmail.com>   // API 문서를 생성한 사람의 정보

v1.0.0, 2022.04.08    // API 문서의 생성 날짜

// 테스트 케이스 실행을 통해 생성한 API 문서 스니핏을 사용하는 부분
***
== MemberController
=== 회원 등록
.curl-request
include::{snippets}/post-member/curl-request.adoc[]

.http-request
include::{snippets}/post-member/http-request.adoc[]

.request-fields
include::{snippets}/post-member/request-fields.adoc[]

.http-response
include::{snippets}/post-member/http-response.adoc[]

.response-headers
include::{snippets}/post-member/response-headers.adoc[]

=== 회원 정보 수정
.curl-request
include::{snippets}/patch-member/curl-request.adoc[]

.http-request
include::{snippets}/patch-member/http-request.adoc[]

.path-parameters
include::{snippets}/patch-member/path-parameters.adoc[]

.request-fields
include::{snippets}/patch-member/request-fields.adoc[]

.http-response
include::{snippets}/patch-member/http-response.adoc[]

.response-fields
include::{snippets}/patch-member/response-fields.adoc[]
```

#### 템플릿 문서에서 스니핏을 사용하는 방법

``include::{snippets}/스니핏 문서가 위치한 디렉토리/스니핏 문서파일명.adoc[]``

- ``.curl-request`` 에서 ``.``은 하나의 스니핏 섹션 제목을 표현하기 위해 사용
- ``include``는 Asciidoctor에서 사용하는 매크로(macro) 중 하나
    - 스니핏을 템플릿 문서에 포함할 때 사용
    - ``::`` 은 매크로를 사용하기 위한 표기법
- ``{snippets}``는 해당 스니핏이 생성되는 디폴트 경로를 의미
    - build.gradle 파일에 설정한 snippetsDir 변수를 참조하는데 사용

### 템플릿 문서를 HTML 파일로 변환

1. resources에 static + docs 경로 설정 해주고 index.html 파일 생성

![](https://velog.velcdn.com/images/wish17/post/91b7555c-3a7c-4fb6-acdb-810e920a00dd/image.png)

2. Gradle task 명령 실행

![](https://velog.velcdn.com/images/wish17/post/031a1f3a-6e3b-4433-a056-c11680138ce9/image.png)

우측 상단의 [Gradle] 탭을 클릭한 후, ``:bootJar`` 또는 ``:build`` task 명령을 더블 클릭

3. 위 과정을 통해 성공적으로 index.html 파일이 생성되면 IntelliJ에서 애플리케이션을 실행하고
웹 브라우저에 ``http://localhost:8080/docs/index.html``를 입력

![](https://velog.velcdn.com/images/wish17/post/55606955-1e23-4ab6-9ef8-ac5035c93324/image.png)

이 과정에서 [Gradle오류](https://velog.io/@wish17/%EC%98%A4%EB%A5%98-%EC%A0%95%EB%A6%AC#gradle%EC%98%A4%EB%A5%98)가 발생해 고생했다.

드디어 Spring Rest Docs를 이용해서 API 문서를 생성할 준비 완료!!

위 과정 간단 정리

- Controller 테스트를 위한 테스트 케이스 실행으로 생성된 API 문서 스니핏은 템플릿 문서에 포함해서 사용할 수 있다.

- 애플리케이션 빌드를 통해 템플릿 문서를 HTML 파일로 변환할 수 있다.

- 변환된 HTML 파일을 ‘src/main/resources/static/docs/’ 디렉토리에 위치 시키면 웹 브라우저로 API 문서를 확인할 수 있다.



***

## Spring Rest Docs에서의 Asciidoc

> Asciidoc
- Spring Rest Docs를 통해 생성되는 텍스트 기반 문서 포맷


### 목차 구성

```http
= 커피 주문 애플리케이션     // (1)
:sectnums:                  // (2)
:toc: left                  // (3)
:toclevels: 4               // (4)
:toc-title: Table of Contents   // (5)
:source-highlighter: prettify   // (6)

We Won Jong <wjwee9@gmail.com> // API 문서를 생성한 사람의 정보

v1.0.0, 2023.03.09 // API 문서의 생성 날짜
```

(1) 문서의 제목을 작성하기 위해서는 ``=``를 추가하면 된다. ``====``와 같이 ``=``의 개수가 늘어날 수록 글자는 작아진다.

(2) 목차에서 각 섹션에 넘버링을 해주기 위해서는 ``:sectnums:`` 를 추가하면 된다.

(3) ``:toc:`` 는 목차를 문서의 어느 위치에 구성할 것인지를 설정한다. 위 예시에서는 문서의 왼쪽정렬로 목차가 표시되도록 left를 지정했다.

(4) ``:toclevels:`` 은 목차에 표시할 제목의 level을 지정한다. 위 예시에서는 4로 지정했기 때문에 ==== 까지의 제목만 목차에 표시된다.

(5) ``:toc-title:`` 은 목차의 제목을 지정할 수 있다.

(6) ``:source-highlighter:`` 문서에 표시되는 소스 코드 하일라이터를 지정한다. (위 예시에서는 prettify로 지정)

![](https://velog.velcdn.com/images/wish17/post/fecc8cbf-9144-4065-857c-fcbb5375d604/image.png)

``들여쓰기`` = 박스 문단

`` ***`` = 단락을 구분 지울 수 있는 수평선 추가

![](https://velog.velcdn.com/images/wish17/post/0954851d-9498-427a-b52e-e8a4b0d9d03f/image.png)

경고 문구 추가 = ``CAUTION:``, ``NOTE:`` , ``TIP:`` , ``IMPORTANT:`` , ``WARNING:`` 등

![](https://velog.velcdn.com/images/wish17/post/6ea16ef8-ed7c-4a67-abf4-becc84565acd/image.png)

URL Scheme = http, https, ftp, irc, mailto, wjwee9@gmail.com
(위와 같은 URL Scheme는 Asciidoc에서 자동으로 인식하여 링크가 설정 된다.)

 이미지 추가 = ``image::``
- ``image::https://velog.velcdn.com/images/wish17/post/fa5f2b25-161f-491f-8f4b-d589a3d42861/image.png[spring]``

![](https://velog.velcdn.com/images/wish17/post/fa5f2b25-161f-491f-8f4b-d589a3d42861/image.png)

> Asciidoctor
- Asciidoctor는 AsciiDoc 포맷의 문서를 파싱해서 HTML 5, 매뉴얼 페이지, PDF 및 EPUB 3 등의 문서를 생성하는 툴
- Spring Rest Docs에서는 Asciidoc 포맷의 문서를 HTML 파일로 변환하기 위해 내부적으로 Asciidoctor를 사용한다.

### 문서 스니핏을 템플릿 문서에 포함 시키기

위와 같은 방법들로 템플릿(index.adoc) 문서를 다 작성한 뒤 디폴트 디렉토리 주소 ``src/main/resources/static/docs``에 비어있는 텍스트 파일 ``index.html``을 생성해 두고  위에서 언급한 Gradle task 명령(build) 실행을 하면 자동 생성 된다.

***

## 실습

[실습내용 풀코드 Gihub 주소](https://github.com/wish9/Practice-api-documentation/commit/6b5362e5ba6c422ada4b4efc5743787dc3ec3a85)

이전 실습까지는 API문서를 만들기 위한 ``andDo(document())``메서드를 사용하지 않았기 때문에 ``MockMvcRequestBuilders`` 클래스의 ``get()``, ``post()``, ``patch()``를 사용해도 상관 없었다.

> **키포인트!!!**
***
하지만 ``andDo(document())``메서드를 사용하기 위해서는 ``RestDocumentationRequestBuilders`` 클래스의 ``get()``, ``post()``, ``patch()``를 사용해야 한다.
***

```java
 // getRequestPreProcessor()
 // getResponsePreProcessor()
 .andDo(document(
                        "delete-member",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(
                                parameterWithName("member-id").description("회원 식별자")
                        )
                ));
                
// preprocessRequest(prettyPrint())
// preprocessResponse(prettyPrint())
.andDo(document(
                        "delete-member",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("member-id").description("회원 식별자")
                        )
                ));

```

위와 같이 `` preprocess`` 메서드를 이용해 문서화 정렬방법을 바꿀 수 있다.

#### preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()):

- 요청(request)과 응답(response)의 body를 읽기 쉬운 형식으로 pretty print를 적용하는 역할을 한다.
- API 문서화 과정에서 요청과 응답의 내용을 보기 좋은 형식으로 표시하기 위해 사용된다.
- 요청과 응답의 본문 내용을 정리하고 가독성을 높이기 위해 사용

#### getRequestPreProcessor(), getResponsePreProcessor():

- 특정 헤더나 쿼리 파라미터 등을 미리 설정할 수 있다.
- API 문서화 과정에서 요청과 응답을 보다 정확하게 모사하고 문서화하기 위해 사용된다.

결국 둘 다 api 문서화를 자동화하기 위해 사용하는 메서드이지만 내용을 정렬하는 형식을 바꾼다는 차이점이 있다.
