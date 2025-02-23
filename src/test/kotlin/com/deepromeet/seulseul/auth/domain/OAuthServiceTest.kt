package com.deepromeet.seulseul.auth.domain

import com.deepromeet.seulseul.auth.api.request.SignUpRequest
import com.deepromeet.seulseul.auth.api.request.Terms
import com.deepromeet.seulseul.auth.domain.response.ExistsUserResponse
import com.deepromeet.seulseul.auth.domain.response.LoginResponse
import com.deepromeet.seulseul.auth.domain.response.SignUpResponse
import com.deepromeet.seulseul.auth.exception.AuthException
import com.deepromeet.seulseul.auth.infrastructure.client.KakaoApiClient
import com.deepromeet.seulseul.auth.infrastructure.client.Provider
import com.deepromeet.seulseul.auth.infrastructure.response.KakaoAccount
import com.deepromeet.seulseul.auth.infrastructure.response.KakaoUserInfoResponse
import com.deepromeet.seulseul.auth.infrastructure.response.Profile
import com.deepromeet.seulseul.user.domain.User
import com.deepromeet.seulseul.user.domain.UserReader
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = ["kakao.api.url=http://dummy",
        "jwt.access.secret=thisisfortestdGVzdEFjY2Vzc1NlY3JldEtleVZhbHVlMTIzNDU2Nzg=",
        "jwt.refresh.secret=thisisfortestddGVzdFJmZXNoU2VjcmV0S2V5VmFsdWUxMjM0NTY3OA"
    ]
)
class OAuthServiceIntegrationTest {

    @Autowired
    lateinit var oAuthService: OAuthService

    @Autowired
    lateinit var userReader: UserReader

    // 외부 API 호출은 모킹 처리
    @MockitoBean
    lateinit var kakaoApiClient: KakaoApiClient

    @Test
    fun `존재하지 않은 유저를 확인한다`() {
        // given
        val authHeader = "Bearer token"
        val kakaoId = 12345L
        val profile = Profile("test", "test@test.com", "testUrl")
        val kakaoUserInfo = KakaoUserInfoResponse(kakaoId, KakaoAccount(profile))
        `when`(kakaoApiClient.getUserInfo(authHeader)).thenReturn(kakaoUserInfo)

        // when
        val response: ExistsUserResponse = oAuthService.checkUserExists(authHeader, Provider.KAKAO)

        // then
        assertThat(response.exists).isFalse()
    }

    @Test
    fun `존재하는 유저를 확인한다`() {
        // given
        val authHeader = "Bearer token"
        val kakaoId = 12345L
        val profile = Profile("test", "test@test.com", "testUrl")
        val kakaoUserInfo = KakaoUserInfoResponse(kakaoId, KakaoAccount(profile))
        `when`(kakaoApiClient.getUserInfo(authHeader)).thenReturn(kakaoUserInfo)

        val existingUser = User(kakaoId=kakaoId,
            nickname = kakaoUserInfo.nickname,
            thumbnailImageUrl=kakaoUserInfo.thumbnailImageUrl,
            profileImageUrl=kakaoUserInfo.profileImageUrl
        )
        userReader.save(existingUser)

        // when
        val response: ExistsUserResponse = oAuthService.checkUserExists(authHeader, Provider.KAKAO)

        // then
        assertThat(response.exists).isTrue()
    }

    @Test
    fun `회원가입을 성공적으로 수행한다`() {
        // given
        val authHeader = "Bearer token"
        val kakaoId = 67890L
        val profile = Profile("newUser", "new@test.com", "newUrl")
        val kakaoUserInfo = KakaoUserInfoResponse(kakaoId, KakaoAccount(profile))
        `when`(kakaoApiClient.getUserInfo(authHeader)).thenReturn(kakaoUserInfo)
        val signUpRequest = SignUpRequest("dummyValue", "37.123", "126.123", Terms(true))

        // when
        val response: SignUpResponse = oAuthService.signUp(authHeader, signUpRequest)

        // then
        assertThat(response.id).isNotNull()
        assertThat(response.accessToken).isNotBlank()
        assertThat(response.refreshToken).isNotBlank()
    }

    @Test
    fun `회원가입 시 이미 존재하는 유저인 경우 예외를 발생시킨다`() {
        // given
        val authHeader = "Bearer token"
        val kakaoId = 11111L
        val profile = Profile("existingUser", "exist@test.com", "existUrl")
        val kakaoUserInfo = KakaoUserInfoResponse(kakaoId, KakaoAccount(profile))
        `when`(kakaoApiClient.getUserInfo(authHeader)).thenReturn(kakaoUserInfo)

        // 미리 DB에 해당 유저 저장
        val existingUser = User(kakaoId=kakaoId,
            nickname = kakaoUserInfo.nickname,
            thumbnailImageUrl=kakaoUserInfo.thumbnailImageUrl,
            profileImageUrl=kakaoUserInfo.profileImageUrl
        )
        userReader.save(existingUser)
        val signUpRequest = SignUpRequest("dummyValue", "37.123", "126.123", Terms(true))

        // when & then
        assertThatThrownBy { oAuthService.signUp(authHeader, signUpRequest) }
            .isInstanceOf(AuthException.AlreadyExistsUser::class.java)
    }

    @Test
    fun `로그인을 성공적으로 수행한다`() {
        // given
        val authHeader = "Bearer token"
        val kakaoId = 22222L
        val profile = Profile("loginUser", "login@test.com", "loginUrl")
        val kakaoUserInfo = KakaoUserInfoResponse(kakaoId, KakaoAccount(profile))
        `when`(kakaoApiClient.getUserInfo(authHeader)).thenReturn(kakaoUserInfo)

        // 미리 DB에 로그인할 유저 저장
        val user = User(kakaoId=kakaoId,
            nickname = kakaoUserInfo.nickname,
            thumbnailImageUrl=kakaoUserInfo.thumbnailImageUrl,
            profileImageUrl=kakaoUserInfo.profileImageUrl
        )
        val savedUser = userReader.save(user)

        // when
        val response: LoginResponse = oAuthService.login(authHeader, Provider.KAKAO.ordinal)

        // then
        assertThat(response.id).isEqualTo(savedUser.id)
        assertThat(response.accessToken).isNotBlank()
        assertThat(response.refreshToken).isNotBlank()
    }
}
