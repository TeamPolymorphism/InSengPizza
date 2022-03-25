package com.teamPM.neart.controller;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.teamPM.neart.security.UserCustomDetailsService;
import com.teamPM.neart.service.MemberService;
import com.teamPM.neart.vo.MemberVO;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Controller
public class MemberDetailsController {

	@Autowired
	private MemberService memberService;

	@Autowired
	UserCustomDetailsService UserCustomDetailsService;

	// 내 페이지 보여주기 by seolin
	@GetMapping("/mypage")
	public String mypage(MemberVO memberVO, Model model) {

		log.info("mypage() ..");
		log.info("MemberVO " + memberVO);

		String id = memberVO.getId();

		return "/user/mypage";

	}

	// 회원정보수정 가져오기 by seolin
	@GetMapping("/update")
	public String update(MemberVO memberVO, Model model) {

		log.info("update() ...");

		String id = memberVO.getId();

		model.addAttribute("update_view", memberService.read(id));

		return "/user/update";

	}

	// 회원정보수정 post랑 수정 후 세션값 받기 by seolin
	@PostMapping("/update")
	public String modify(HttpSession session, HttpServletRequest request, MemberVO memberVO, Model model) {

		log.info("modify() ...");
		System.out.println("modify 타나여//////");

		// 수정하는 코드
		memberService.modify(memberVO);
		log.info("==============memverVO: " + memberVO);

		// 세션 등록
		UserDetails userDetails = (UserDetails) UserCustomDetailsService.loadUserByUsername(memberVO.getId());
		// MemberCustonDetails를 리턴해 memberVO의 username을 가지고 온다.

		System.out.println("세션등록------------");
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,
				(userDetails).getPassword(), (userDetails).getAuthorities());
		// UsernamePasswordAuthenticationToken에서 principal, password, authority를 토큰으로 세
		// 개 가지고 오고, authentication에 담는다.

		System.out.println("토큰 받기------------");
		SecurityContext securityContext = SecurityContextHolder.getContext();
		securityContext.setAuthentication(authentication);
		// 세션 안에 수정한 내용을 넣는 코드

		System.out.println("수정내용 충력--------------");
		session = request.getSession(true);
		session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

		memberService.modify(memberVO);

		return "redirect:/";

	}

	@GetMapping("/deletecheck")
	public String remove(MemberVO memberVO, Model model) {

		log.info("deletecheck() ...");

		return "/user/deletecheck";
	}

	@GetMapping("/delete")
	public String checkmore(MemberVO memberVO, Model model) {

		log.info("userdelete() ...");

		return "/user/delete";
	}

	// 회원탈퇴하기 by seolin
	@RequestMapping("/userdelete")
	public String delete(HttpServletRequest request, MemberVO memberVO, String id, Model model) {

		log.info("delete()...");
		memberService.delete(memberVO.getId());
		
		request.getSession().invalidate();
	    request.getSession(true);
		
		return "redirect:/logout";
	}

	/*
	 * //회원한 탈퇴 로그인 불가 by seolin
	 * 
	 * @RequestMapping("/") public String withdraw(Principal principal,
	 * HttpServletRequest request, HttpSession session) {
	 * System.out.println("------withdraw"); System.out.println("탈퇴한 회원--------"+
	 * principal);
	 * 
	 * 
	 * int enabled = principal.getEnabled();
	 * 
	 * if(enabled == 0) {
	 * 
	 * session.invalidate();
	 * 
	 * System.out.println("------회원탈퇴하셨습니다");
	 * 
	 * 
	 * return "/home"; } if (session == 1 || !request.isRequestedSessionIdValid()) {
	 * System.out.println("세션이 무효화 상태입니다."); }
	 * 
	 * session.invalidate();
	 * 
	 * return "/home"; }
	 */
}
