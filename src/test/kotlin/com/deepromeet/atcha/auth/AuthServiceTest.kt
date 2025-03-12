package com.deepromeet.atcha.auth

import com.deepromeet.atcha.auth.domain.AuthService
import com.deepromeet.atcha.auth.domain.Provider
import com.deepromeet.atcha.auth.domain.UserProviderAppender
import com.deepromeet.atcha.auth.exception.AuthException
import com.deepromeet.atcha.auth.infrastructure.provider.ProviderType
import com.deepromeet.atcha.auth.infrastructure.provider.kakao.KakaoFeignClient
import com.deepromeet.atcha.auth.infrastructure.response.KakaoAccount
import com.deepromeet.atcha.auth.infrastructure.response.KakaoUserInfoResponse
import com.deepromeet.atcha.auth.infrastructure.response.Profile
import com.deepromeet.atcha.support.fixture.UserFixture
import com.deepromeet.atcha.user.domain.UserAppender
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = [
        "kakao.api.url=http://dummy",
        "jwt.access.secret=thisisfortestdGVzdEFjY2Vzc1NlY3JldEtleVZhbHVlMTIzNDU2Nzg=",
        "jwt.refresh.secret=thisisfortestddGVzdFJmZXNoU2VjcmV0S2V5VmFsdWUxMjM0NTY3OA"
    ]
)
class AuthServiceTest {
    @Autowired
    private lateinit var userProviderAppender: UserProviderAppender

    @Autowired
    lateinit var authService: AuthService

    @Autowired
    lateinit var userAppender: UserAppender

    @MockitoBean
    lateinit var kakaoFeignClient: KakaoFeignClient

    @Test
    fun `존재하지 않은 유저를 확인한다`() {
        // given
        val token = "token"
        val kakaoId = 12345L
        val profile = Profile("test", "test@test.com")
        val kakaoUserInfo = KakaoUserInfoResponse(kakaoId, KakaoAccount(profile))
        `when`(kakaoFeignClient.getUserInfo(anyString())).thenReturn(kakaoUserInfo)

        // when
        val result: Boolean = authService.checkUserExists(token, ProviderType.KAKAO.ordinal)

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `존재하는 유저를 확인한다`() {
        // given
        val token = "token"
        val kakaoId = 12345L
        val profile = Profile("test", "test@test.com")
        val kakaoUserInfo = KakaoUserInfoResponse(kakaoId, KakaoAccount(profile))
        `when`(kakaoFeignClient.getUserInfo(anyString())).thenReturn(kakaoUserInfo)

        val existingUser =
            UserFixture.create(
                providerId = kakaoId,
                nickname = kakaoUserInfo.nickname,
                profileImageUrl = kakaoUserInfo.profileImageUrl
            )

        userAppender.save(existingUser)

        // when
        val result: Boolean = authService.checkUserExists(token, ProviderType.KAKAO.ordinal)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `회원가입을 성공적으로 수행한다`() {
        // given
        val token = "token"
        val kakaoId = 67890L
        val profile = Profile("newUser", "new@test.com")
        val kakaoUserInfo = KakaoUserInfoResponse(kakaoId, KakaoAccount(profile))
        `when`(kakaoFeignClient.getUserInfo(anyString())).thenReturn(kakaoUserInfo)
        val user = UserFixture.create()
        val signUpRequest = UserFixture.userToSignUpRequest(user, ProviderType.KAKAO.ordinal)

        // when
        val result = authService.signUp(token, signUpRequest.toSignUpInfo())

        // then
        assertThat(result.id).isNotNull()
        assertThat(result.accessToken).isNotBlank()
        assertThat(result.refreshToken).isNotBlank()
    }

    @Test
    fun `회원가입 시 이미 존재하는 유저인 경우 예외를 발생시킨다`() {
        // given
        val token = "token"
        val kakaoId = 11111L
        val profile = Profile("existingUser", "exist@test.com")
        val kakaoUserInfo = KakaoUserInfoResponse(kakaoId, KakaoAccount(profile))
        `when`(kakaoFeignClient.getUserInfo(anyString())).thenReturn(kakaoUserInfo)

        // 미리 DB에 해당 유저 저장
        val existingUser =
            UserFixture.create(
                providerId = kakaoId,
                nickname = kakaoUserInfo.nickname,
                profileImageUrl = kakaoUserInfo.profileImageUrl
            )
        userAppender.save(existingUser)
        val signUpRequest = UserFixture.userToSignUpRequest(existingUser, ProviderType.KAKAO.ordinal)

        // when & then
        assertThatThrownBy { authService.signUp(token, signUpRequest.toSignUpInfo()) }
            .isInstanceOf(AuthException.AlreadyExistsUser::class.java)
    }

    @Test
    fun `로그인을 성공적으로 수행한다`() {
        // given
        val token = "token"
        val kakaoId = 22222L
        val profile = Profile("loginUser", "login@test.com")
        val kakaoUserInfo = KakaoUserInfoResponse(kakaoId, KakaoAccount(profile))

        `when`(kakaoFeignClient.getUserInfo(anyString())).thenReturn(kakaoUserInfo)

        // 미리 DB에 로그인할 유저 저장
        val user = UserFixture.create(providerId = kakaoId)
        val savedUser = userAppender.save(user)
        userProviderAppender.save(savedUser, Provider(0, ProviderType.KAKAO, token))

        // when
        val result = authService.login(token, ProviderType.KAKAO.ordinal, "TEST_FCM_TOKEN")

        // then
        assertThat(result.id).isEqualTo(savedUser.id)
        assertThat(result.accessToken).isNotBlank()
        assertThat(result.refreshToken).isNotBlank()
    }
}
