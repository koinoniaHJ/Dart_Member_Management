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

	}
	
	// ----------SELECT ALL--------------------------------------------------

	@PostMapping("/api/members/save")
	public String saveMember(@ModelAttribute Member member, // HTML의 name 속성의 이름과 Member 클래스의 필드명이 일치하면, Spring이 자동으로
															// 상자(객체)를 만들어서 값을 채워 넣음.
			@RequestParam("profileFile") MultipartFile file
	/*
	 * 이미지나 파일 같은 바이너리 데이터는 텍스트와 다르게 전송되므로 MultipartFile이라는 특수한 객체로 가로채야 한다. (HTML 폼
	 * 태그에 enctype="multipart/form-data"가 붙어있는 이유)
	 */
	) {
		/*
		 * 새로운 회원을 등록(CREATE)하는 창구이므로, 혹시라도 들어왔을지 모르는 기존 ID를 null로 강제 세팅. 이렇게 하면 JPA는
		 * 무조건 새로운 번호(Identity)를 생성하여 새 회원으로 등록.
		 */
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

	// ----------INSERT--------------------------------------------------
	
	@PostMapping("/api/members/update")
	public String updateMember(@ModelAttribute Member member, @RequestParam("profileFile") MultipartFile file) {
		/*
		 * repository 에게 findById로 memberId를 전달 -> JPA가 결과물을 Optional 객체로 감싸서 return ->
		 * 반환된 Optional 객체에 .orElseThrow() 메서드를 실행하여 데이터가 있으면 existingMember에 넣어주고,
		 * 없으면(null) 에러를 발생
		 * 
		 * 여기서 member 변수에는 에는 화면에서 꺼내 온 value 가 들어있다. existingMember에는 실제로 DB에서
		 * findById를 하여 꺼내 온 "원본 객체"가 들어있는 것
		 */
		Member existingMember = memberRepository.findById(member.getMemberId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + member.getMemberId()));

		/*
		 * 기존 회원 엔티티에 화면에서 새로 입력한 값들을 덮어씌운다. existingMember.setName(member.getName());
		 * existingMember.setNickName(member.getNickName());
		 * existingMember.setAge(member.getAge());
		 * existingMember.setRating(member.getRating());
		 */

		// 객체지향적 비즈니스 메서드 호출
		// 무분별한 Setter 나열을 지우고 엔티티에게 스스로 정보를 수정하라고 명령.
		existingMember.updateMemberInfo(member.getName(), member.getNickName(), member.getAge(), member.getRating());

		// 사용자가 이미지를 새로 첨부했다면
		if (file != null && !file.isEmpty()) {
			try {
				// 이미지가 저장될 서버의 실제 주소를 동적으로 저장.
				String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";

				// 사용자가 올린 원래 파일명.
				String originalFileName = file.getOriginalFilename();

				// 중복으로 기존 파일이 덮어씌워지는 것을 막기 위해, 랜덤 문장(UUID)을 원래 파일명 앞에 붙인다.
				String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName;

				// 컴퓨터 경로와 랜덤 파일명을 조합해서 하드디스크에 저장될 객체 생성.(RAM에 임시 객체로)
				File targetFile = new File(uploadDir + savedFileName);

				// 브라우저가 보낸 실제 이미지 데이터(file)를 방금 만든 targetFile 속으로 전송
				// 컴퓨터 서버의 하드디스크에 진짜 파일이 생성
				file.transferTo(targetFile);

				// DB 원본 객체(existingMember)의 images 필드 변수에 덮어씌우기.
				existingMember.setImages("/uploads/" + savedFileName);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/* 만약 수정할 때 이미지를 새로 첨부하지 않았다면 (if 문이 실행되지 않으므로)
		existingMember가 원래 가지고 있던 기존 이미지 경로가 지워지지 않고 그대로 유지.
		 */
		
		// 수정한 내용을 저장소에 반영. 
		memberRepository.save(existingMember);

		// 완료 후 메인 페이지로 리다이렉트
		return "redirect:/";
	}
	
	// ----------UPDATE--------------------------------------------------

}