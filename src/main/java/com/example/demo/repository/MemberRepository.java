package com.example.demo.repository;

import com.example.demo.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // 이 인터페이스가 데이터 접근 계층(Data Access Layer)의 창구임을 선언.
public interface MemberRepository extends JpaRepository<Member, Long> {
	// JPA 엔진이 Member 엔티티의 구조를 분석하여, 해당 메서드들과 매핑될 오라클 전용 SQL 쿼리문을 메모리 상에 자동 생성한다.
	// 코드가 비어있어도 JpaRepository 상속 덕분에 MemberRepository는 DB 조작 메서드를 내부적으로 보유한다.
	
	/* [데이터 실행 흐름]
	개발자가 컨트롤러에서 해당 자바 메서드를 호출하면, MemberRepository 인터페이스로 전달되어 
	JPA 엔진이 해당 메서드와 매핑된 SQL문을 실시간으로 조립 및 캡슐화하여 오라클 DB로 전송하고 실행.
	*/
}