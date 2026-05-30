document.addEventListener('DOMContentLoaded', function () {
    const modalOverlay = document.getElementById('modal-overlay');
    const memberItems = document.querySelectorAll('.member-item');
    const addBtn = document.getElementById('add-member-btn');

    // 1. 회원 리스트 클릭 시 모달 띄우기 (수정 모드)
    memberItems.forEach(function (item) {
        item.addEventListener('click', function () {
            // 클릭한 회원의 고유 ID(PK) 가져오기 
            // (HTML의 data-member-id 속성에 숨겨둔 값을 뽑아온다.)
            const memberId = item.getAttribute('data-member-id');

            // 클릭한 리스트 안에 있는 텍스트 데이터 긁어오기
            const infoValues = item.querySelectorAll('.info-value');
            const name = infoValues[0].innerText;     // 첫 번째 span (이름)
            const nickname = infoValues[1].innerText; // 두 번째 span (닉네임)
            const age = infoValues[2].innerText;      // 세 번째 span (나이)
            const rating = infoValues[3].innerText;   // 네 번째 span (RATING)
            
            // 리스트에 있던 프로필 이미지 주소 긁어오기
            const imgSrc = item.querySelector('.profile-img').src;

            // 긁어온 데이터를 모달창 안의 input 요소들에 '값(value)'으로 세팅하기
            document.getElementById('modal-member-id').value = memberId; // 숨겨진 PK 인풋
            document.getElementById('member-name-input').value = name;
            document.getElementById('member-nickname-input').value = nickname;
            document.getElementById('member-age-input').value = age;
            document.getElementById('member-rating-input').value = rating;

			// 모달창 프로필 이미지도 긁어온 이미지로 바꿔주기
            document.getElementById('modal-profile-preview').src = imgSrc;

            // 모든 준비가 끝났으니 모달창 띄우기
            modalOverlay.classList.add('active'); 
        });
    });

    // 2. 상단 '추가' 버튼 클릭 시 모달 띄우기 (등록 모드)
	if (addBtn) {
	    addBtn.addEventListener('click', function () {
	        document.getElementById('member-form').reset();
	        
	        /* 숨겨진 ID 값을 반드시 공백으로 직접 지워줘야 새 회원 등록(INSERT)이 된다.
			기존 회원을 클릭해서 수정 모달을 열었다가 닫은 후, 
			상단의 '추가' 버튼을 눌러서 새 회원을 저장하려고 하면
			기존 회원의 ID(PK)가 지워지지 않고 서버로 전송되기 때문.
			*/
	        document.getElementById('modal-member-id').value = ''; 
	        
	        document.getElementById('modal-profile-preview').src = '/images/dummy-profile.png';
	        modalOverlay.classList.add('active');
	    });
	}

    // 3. 모달창 바깥의 어두운 배경 클릭 시 모달 닫기
    modalOverlay.addEventListener('click', function (e) {
        // 클릭한 곳이 하얀색 모달 창 안쪽이 아니라 어두운 오버레이 배경일 때만 닫음
        if (e.target === modalOverlay) {
            modalOverlay.classList.remove('active');
        }
    });
	document.getElementById('member-form').addEventListener('click', function(e) {
	    e.stopPropagation(); // 부모(오버레이)로 클릭 신호가 올라가는 것을 차단
	});

    // 4. 프로필 이미지 파일 첨부 시 즉시 미리보기 기능
    const fileInput = document.getElementById('modal-profile-file');
    const profilePreview = document.getElementById('modal-profile-preview');

    if (fileInput && profilePreview) {
        fileInput.addEventListener('change', function (e) {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function (event) {
                    // 선택한 이미지를 동그란 프로필 영역에 집어넣음
                    profilePreview.src = event.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    }
});