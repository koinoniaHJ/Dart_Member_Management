package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.util.Date;

@Entity	// 1. 오라클 데이터베이스 테이블과 매핑할 클래스임을 선언
@Table(name = "member") // 오라클 DB에 생성될 테이블명을 'member'로 지정
public class Member {

    @Id // 3. 테이블의 기본키(Primary Key)로 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 오라클 12c 이상 표준인 GENERATED ALWAYS AS IDENTITY 규칙 적용
    @Column(name = "member_id")
    private Long memberId;

    @Column(nullable = false, length = 50) // NOT NULL 및 글자 수 제한(50) 적용
    private String name;

    @Column(name = "nick_name", length = 50) // 자바 변수명(camelCase)과 DB 컬럼명(snake_case) 매핑
    private String nickName;

    @Column(precision = 3) // NUMBER(3) 데이터 타입 적용
    private Integer age;

    @Column(length = 10)
    private String gender;

    @Column(length = 10)
    private String rating;

    @Column(length = 4000)
    private String memo;

    @Column(length = 4000)
    private String images;

    // 최초 등록 시점에만 자바가 날짜를 넣도록 insertable만 true로 유지
    @Column(name = "reg_date", nullable = false, updatable = false)
    private Date regDate; 

    // 추가된 수정일자 컬럼 (등록/수정 시점 모두 자바가 제어)
    @Column(name = "update_date", nullable = false)
    private Date updateDate;

    // --- JPA가 스스로 날짜를 계산해서 꽂아 넣는 자동화 메서드 영역 ---
    
    @PrePersist // 1. 최초 데이터가 DB에 INSERT 되기 직전에 실행되는 자바 메서드
    protected void onCreate() {
        Date now = new Date();
        this.regDate = now;    // 최초 등록일 세팅
        this.updateDate = now; // 최초 등록시에는 수정일도 등록일과 동일하게 세팅
    }

    @PreUpdate // 2. 이미 존재하는 데이터를 수정(UPDATE)하기 직전에 실행되는 자바 메서드
    protected void onUpdate() {
        this.updateDate = new Date(); // 수정이 일어날 때마다 현재 시간으로 갱신
    }

    // --- 기본 생성자 (JPA 필수 사양) ---
    public Member() {}

    // --- Getter / Setter 메서드 (데이터를 넣고 빼기 위한 통로) ---
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public Date getRegDate() { return regDate; }
    public void setRegDate(Date regDate) { this.regDate = regDate; }
}