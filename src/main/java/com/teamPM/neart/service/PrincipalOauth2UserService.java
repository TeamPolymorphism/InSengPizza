package com.teamPM.neart.service;

import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.teamPM.neart.config.oauth.GoogleUserInfo;
import com.teamPM.neart.config.oauth.KakaoUserInfo;
import com.teamPM.neart.config.oauth.NaverUserInfo;
import com.teamPM.neart.config.oauth.OAuth2UserInfo;
import com.teamPM.neart.controller.MemberController;

import com.teamPM.neart.mapper.UserMapper;
import com.teamPM.neart.security.UserCustomDetails;
import com.teamPM.neart.vo.AuthVO;
import com.teamPM.neart.vo.MemberVO;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
	
	 
	/*
	 * @Autowired private @Lazy BCryptPasswordEncoder bCryptPasswordEncoder;
	 */
	
	@Setter(onMethod_ = @Autowired)
	private UserMapper mapper;

	@Override // userRequest는 code를 받아서 accessToken을 응답받은 객체
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		// 후처리 함수 소셜로 부터 받은 userRequest 데이터 후처리
		log.info("login oauth2service");

		OAuth2User oauth2User = super.loadUser(userRequest); // 회원 프로필 조회
		// 소셜 로그인 버튼 클릭->소셜 로그인창->로그인완료->code리턴(oauth2-client)->accessToken
		// userRequest 정보
		// userRequest 정보 -> 회원프로필 조회(loaduser 함수)-> 소셜로부터 회원 프로필을 받아준다.

		log.info("userRequest clientRegistration" + userRequest.getClientRegistration());// code를 통해 구성한 정보
		log.info("getAccessToken" + userRequest.getAccessToken());
		log.info("oauth2User" + oauth2User);// 토큰을 통해 응답 받은 회원 정보

		OAuth2UserInfo userInfo = null;

		if (userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
			// getRegistrationId로 어떤 oauth로 로그인했는지 확인 가능
			userInfo = new NaverUserInfo((Map) oauth2User.getAttributes().get("response"));
			
		} else if (userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {

			userInfo = new KakaoUserInfo(oauth2User.getAttributes());
		} else if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
		
		userInfo = new GoogleUserInfo(oauth2User.getAttributes());
		}

		log.info("======1=====" + userInfo.getEmail());
		
		String email = userInfo.getEmail();
		log.info("======121=====" + email);
		
		MemberVO mvo = mapper.isEmail(email); // 이미 가입이 되어있는지 조회
		System.out.println("===111111======" + mapper.isEmail(email));
		
		
		 
		
		if (mvo == null) {// 가입되어 있지 않다면 가입진행
			 mvo = new MemberVO();
			 
			 String password = "userusersu";
			 System.out.println("userVO.getPassword====확인" + mvo.getPassword());
				
			 BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();
				
			 String encode = passEncoder.encode(password); 
			 System.out.println("passEncoder====확인" + passEncoder);
		
			 log.info("======2====="); 
			 mvo.setId(userInfo.getEmail());
			 log.info("======3====="); // 소셜은 로그인창에 아이디와 비밀번호를 입력하여 로그인하지 않기때문에 아이디 컬럼에 고유식별자 삽입 
			 mvo.setPassword(passEncoder.encode(encode));
			 //mvo.setPassword(userInfo.getProvider() + "_" + userInfo.getProviderId()); // 비밀번호를 임의로 provider+providerId 로 생성
			 log.info("======4====="); 
			 mvo.setName(userInfo.getName());
			 log.info("======5=====");
			 mvo.setPhonenum("12356"); 
			 //mvo.setGender(userInfo.getGender()); mvo.setPhone(userInfo.getMobile());
			 //mvo.setBirth(Date.valueOf(userInfo.getBirthyear() + "-" +
			 //userInfo.getBirthday())); // string 을 오라클의 Date로 // 변환, yyyy-mm-dd형식으로 // 포맷팅
			 mvo.setYear(2022);
			 mvo.setMonth(01);
			 mvo.setDay(01);
			 mvo.setAddress("123");
			 mvo.setEmail(userInfo.getEmail());
			 log.info("======1==2===");
			 
			 int result = mapper.memberJoin(mvo); //가입정보 디비에 입력 
			 
			 log.info("======1==3===결과" +  result);
			 //mapper.insertAuth();// 권한 db에 입력
			 mapper.insertAuthorities();
			 			 
			 mvo = mapper.getUser(userInfo.getEmail());
			 
			 log.info("======1==4===");
		}
		else {
			System.out.println("뭐야=====" + mvo);
			mvo = mapper.getUser(mvo.getId());
		}

		System.out.println("mvo=====확인" + mvo);
		System.out.println("oauth2User.getAttributes()===확인" + oauth2User.getAttributes());
		
		return new UserCustomDetails(mvo, oauth2User.getAttributes());

	}

}