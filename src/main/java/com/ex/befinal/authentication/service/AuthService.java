package com.ex.befinal.authentication.service;

import com.ex.befinal.authentication.dto.KakaoTokenResponse;
import com.ex.befinal.authentication.dto.KakaoUserDetailsResponse;
import com.ex.befinal.authentication.dto.SignInResponse;
import com.ex.befinal.authentication.dto.UrlJson;
import com.ex.befinal.authentication.dto.UserResponse;
import com.ex.befinal.authentication.provider.JwtTokenProvider;
import com.ex.befinal.authentication.service.client.KakaoClient;
import com.ex.befinal.authentication.service.strategy.SignInStrategy;
import com.ex.befinal.authentication.service.strategy.SignInUrlCreateStrategy;
import com.ex.befinal.constant.UserRole;
import com.ex.befinal.models.User;
import com.ex.befinal.user.repository.UserJpaRepository;
import com.ex.befinal.utils.RandomNicknameGenerator;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {
  private final SignInStrategy signInStrategy;
  private final UserJpaRepository userJpaRepository;
  private final KakaoClient kakaoClient;
  private final InMemoryClientRegistrationRepository inMemoryRepository;
  private final RandomNicknameGenerator nicknameGenerator;
  private final JwtTokenProvider jwtTokenProvider;
  /**
   * AuthVendor 에 따른 각 Vendor 로그인을 수행하는 URL 을 반환합니다.
   */

  public UrlJson getLoginUrl() {
    SignInUrlCreateStrategy urlCreateStrategy = signInStrategy.getStrategy();
    return new UrlJson(urlCreateStrategy.create());
  }

  public SignInResponse signIn(String providerName, String authCode) {
    ClientRegistration provider = inMemoryRepository.findByRegistrationId(providerName);
    KakaoTokenResponse tokenResponse = kakaoClient.getToken(authCode, provider);
    User user = getUserProfile(providerName, tokenResponse, provider);
    if (!user.getEnable()) {
      throw new DisabledException("비활성화된 회원입니다. 관리자에게 문의하세요.");
    }
    String accessToken = jwtTokenProvider.createAccessToken(user.getNickName());
    UserResponse userResponse =
        new UserResponse(
            user.getId(),
            user.getKakaoId(),
            user.getRole(),
            user.getNickName(),
            user.getCreateAt(),
            user.getProvider());
    return new SignInResponse(accessToken, userResponse);
  }

  private User getUserProfile(String providerName, KakaoTokenResponse tokenResponse, ClientRegistration provider) {
    KakaoUserDetailsResponse kakaoId =
        kakaoClient.getUserAttributes(provider, tokenResponse);
    Optional<User> userOptional =userJpaRepository.findByKakaoId(kakaoId.id());
    String nickname = nicknameGenerator.nickname();
    String n = nickNameDuplicateCheck(nickname);
    if (!userOptional.isPresent()) {
      User user = User.builder()
          .createAt(new Date())
          .kakaoId(kakaoId.id())
          .role(UserRole.USER)
          .enable(true)
          .nickName(n)
          .provider(providerName)
          .build();
      userJpaRepository.save(user);
      return user;
    }
    return userOptional.get();
  }

  private String nickNameDuplicateCheck(String nickname) {

    for (int i = 0; i < 1300; i++) {
      if (!userJpaRepository.existsByNickName(nickname)) {
        return nickname;
      }
      nickname = nicknameGenerator.nickname();
    }
    return nickname;
  }
}
