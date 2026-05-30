package com.example.demo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Member;
import com.example.demo.repository.MemberRepository;

@Controller
public class MemberController {
	@Autowired // 1. 오라클 DB와 소통할 레포지토리 창구 가져오기
	private MemberRepository memberRepository;
	
	// 사용자가 웹사이트 주소(http://localhost:8080/)로 들어왔을 때 ---
	@GetMapping("/")
	public String mainUrl(@RequestParam(value = "page", defaultValue = "1") int page, Model model) {
		/* 
		 @RequestParam이 파라미터를 감시 후 숫자를 int page에 넣어준다.
		 value = "page"은 URL의 파라미터 key 값과 매핑되어, 값이 매개변수 int page로 들어간다.
		 defaultValue = "1" 설정으로 page 변수에는 기본값으로 1이 담긴다.
		*/
		
		
	    int pageIndex = page - 1; // 사용자는 1페이지를 요청하지만, JPA는 첫 페이지를 0으로 인식하므로 (page - 1)을 해준다.
	    
		// 0번째 페이지(첫 페이지), 한 페이지당 4개씩, memberId 기준 오름차순(Ascending) 정렬
	    Pageable pageable = PageRequest.of(pageIndex, 4, Sort.by("memberId").ascending());
	    // JPA는 Pageable을 파라미터로 받으면 Repository 를 가동시켜 오라클 DB에서 해당 페이지 4명의 데이터와 전체 페이지 정보를 받아온다.
	    Page<Member> memberPage = memberRepository.findAll(pageable);
	    
	    // 페이징 블록 계산 (예: 1~5 버튼 그룹 생성)
	    int currentPage = memberPage.getNumber() + 1;	// 현재 페이지.	
	    int totalPages = memberPage.getTotalPages();	// 전체 페이지 개수.
	    
	    // 현재 페이지 기준으로 화면에 보여줄 시작 버튼과 끝 버튼 번호 계산
	    int startPage = (((currentPage - 1) / 5) * 5) + 1; // 현재 페이지를 기준으로 하단에 보여줄 '시작 버튼 번호'를 계산.
	    int endPage = Math.min(startPage + 4, totalPages); // 끝 버튼 번호를 계산.
	    
	    /*
	    model.addAttribute("key", value); 
		데이터 배달 상자 : JAVA 코드로 계산을 마친 후 HTML 파일에게 결과물을 넘겨줘야 할 때 사용 */
	    model.addAttribute("members", memberPage.getContent());		// 실제 회원 4명 데이터
	    model.addAttribute("currentPage", currentPage);         	// 현재 페이지 번호
	    model.addAttribute("totalPages", totalPages);           	// 총 페이지 수
	    model.addAttribute("startPage", startPage);             	// 시작 버튼 번호 (예: 1)
	    model.addAttribute("endPage", endPage);                 	// 끝 버튼 번호 (예: 5)
	    model.addAttribute("hasPrev", memberPage.hasPrevious()); 	// 이전 페이지 존재 여부 (true/false)
	    model.addAttribute("hasNext", memberPage.hasNext());     	// 다음 페이지 존재 여부 (true/false)
	    
	    // src/main/resources/templates/list.html 화면을 렌더링
		return "list"; 

	} //
	
	@PostMapping("/api/members/save")
	public String saveMember(
	    @ModelAttribute Member member, // name, nickName, age, rating 을 자바 객체로 한방에 바인딩
	    @RequestParam("images") MultipartFile file // HTML의 name="images"인 파일 덩어리를 가로챔
	) {
	    // 1. file이 비어있지 않다면 서버 로컬 폴더에 저장하고 저장 경로 문자열 생성
	    // 2. member.setImages(저장경로); 꽂아넣기
	    // 3. memberRepository.save(member); DB에 전송
	    return "redirect:/";
	}

}