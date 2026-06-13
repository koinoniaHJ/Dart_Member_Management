package com.example.demo.controller;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
		 * @RequestParam이 파라미터를 감시 후 숫자를 int page에 넣어준다. value = "page"은 URL의 파라미터 key
		 * 값과 매핑되어, 값이 매개변수 int page로 들어간다. defaultValue = "1" 설정으로 page 변수에는 기본값으로 1이
		 * 담긴다.
		 */

		int pageIndex = page - 1; // 사용자는 1페이지를 요청하지만, JPA는 첫 페이지를 0으로 인식하므로 (page - 1)을 해준다.

		// 0번째 페이지(첫 페이지), 한 페이지당 4개씩, memberId 기준 오름차순(Ascending) 정렬
		Pageable pageable = PageRequest.of(pageIndex, 4, Sort.by("memberId").ascending());
		// JPA는 Pageable을 파라미터로 받으면 Repository 를 가동시켜 오라클 DB에서 해당 페이지 4명의 데이터와 전체 페이지
		// 정보를 받아온다.
		Page<Member> memberPage = memberRepository.findAll(pageable);

		// 페이징 블록 계산 (예: 1~5 버튼 그룹 생성)
		int currentPage = memberPage.getNumber() + 1; // 현재 페이지.
		int totalPages = memberPage.getTotalPages(); // 전체 페이지 개수.

		// 현재 페이지 기준으로 화면에 보여줄 시작 버튼과 끝 버튼 번호 계산
		int startPage = (((currentPage - 1) / 5) * 5) + 1; // 현재 페이지를 기준으로 하단에 보여줄 '시작 버튼 번호'를 계산.
		int endPage = Math.min(startPage + 4, totalPages); // 끝 버튼 번호를 계산.

		/*
		 * model.addAttribute("key", value); 데이터 배달 상자 : JAVA 코드로 계산을 마친 후 HTML 파일에게
		 * 결과물을 넘겨줘야 할 때 사용
		 */
		model.addAttribute("members", memberPage.getContent()); // 실제 회원 4명 데이터
		model.addAttribute("currentPage", currentPage); // 현재 페이지 번호
		model.addAttribute("totalPages", totalPages); // 총 페이지 수
		model.addAttribute("startPage", startPage); // 시작 버튼 번호 (예: 1)
		model.addAttribute("endPage", endPage); // 끝 버튼 번호 (예: 5)
		model.addAttribute("hasPrev", memberPage.hasPrevious()); // 이전 페이지 존재 여부 (true/false)
		model.addAttribute("hasNext", memberPage.hasNext()); // 다음 페이지 존재 여부 (true/false)

		// src/main/resources/templates/list.html 화면을 렌더링
		return "list";

	} //

	@PostMapping("/api/members/save")
	public String saveMember(@ModelAttribute Member member, // HTML의 name 속성의 이름과 Member 클래스의 필드명이 일치하면, Spring이 자동으로 상자(객체)를 만들어서 값을 채워 넣음.
			@RequestParam("profileFile") MultipartFile file
			/* 이미지나 파일 같은 바이너리 데이터는 텍스트와 다르게 전송되므로 MultipartFile이라는 특수한 객체로 가로채야 한다.
			(HTML 폼 태그에 enctype="multipart/form-data"가 붙어있는 이유) */
	) {
		/* 새로운 회원을 등록(CREATE)하는 창구이므로, 혹시라도 들어왔을지 모르는 기존 ID를 null로 강제 세팅.
	    이렇게 하면 JPA는 무조건 새로운 번호(Identity)를 생성하여 새 회원으로 등록. */
	    member.setMemberId(null);
		// 1. 파일이 존재하는지, 비어있지 않은지 체크
		if (file != null && !file.isEmpty()) {
			try {
				// 이미지가 저장될 실제 컴퓨터 서버 내부 경로 지정
				// (주의: 실무 환경에선 외부 경로를 쓰지만, 개발 단계에선 static 하위에 폴더를 만들어 테스트.)
				String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";

				// 해당 폴더가 실제로 컴퓨터에 존재하지 않으면 폴더를 자동으로 생성
				File dir = new File(uploadDir);
				if (!dir.exists()) {
					dir.mkdirs();
				}

				// 파일명 중복을 방지하기 위해 랜덤한 UUID를 파일명 앞에 붙임 (예: a1b2c3d4_myprofile.jpg)
				String originalFileName = file.getOriginalFilename();
				String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName;

				// 컴퓨터 실제 경로에 파일 저장 처리
				File targetFile = new File(uploadDir + savedFileName);
				file.transferTo(targetFile);

				// 웹 브라우저가 Thymeleaf를 통해 접근할 수 있는 URL 경로를 Entity에 저장
				// (예: /uploads/a1b2c3d4_myprofile.jpg)
				member.setImages("/uploads/" + savedFileName);

			} catch (IOException e) {
				e.printStackTrace();
				// 파일 업로드 실패 시 예외 처리 (실무에선 로깅 후 에러 페이지로 유도)
			}
		} else {
			// 사용자가 이미지를 첨부하지 않았다면 null 혹은 기본 값을 세팅
			member.setImages(null);
		}

		// 2. JPA의 .save() 메서드를 호출하여 Oracle DB에 INSERT 실행
		memberRepository.save(member);

		// 3. 등록이 완료되면 첫 페이지로 리다이렉트(새로고침 효과)
		return "redirect:/";
	}

}