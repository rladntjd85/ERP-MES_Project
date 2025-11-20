/**
 *   등록 혹은 수정 페이지 js 
 */
	document.addEventListener('DOMContentLoaded', function() {
		const token = $("meta[name='_csrf']").attr("content");
		const header = $("meta[name='_csrf_header']").attr("content");
	
		$(document).ajaxSend(function(e, xhr, options) {
			if (token && header) {
				xhr.setRequestHeader(header, token);
			}
		});
	});



// 전화번호 유효성 검사 
	document.getElementById('phone').addEventListener('input', function(e) {
  		let input = e.target.value.replace(/\D/g, ''); // 숫자 이외의 문자는 모두 제거
  
		// 최대 11자리까지 허용 (예: 01012345678)
		if (input.length > 11) {
			input = input.substring(0, 11);
		}
  		
		let formatted = '';
		// 입력된 숫자 길이에 따라 하이픈(-)을 삽입합니다.
		if (input.length < 4) { // 예: 010
			formatted = input;
		} else if (input.length < 7) {	// 예: 010-123
			formatted = input.substring(0, 3) + '-' + input.substring(3);
		} else if (input.length < 11) {	// 예: 010-123-456
			formatted = input.substring(0, 3) + '-' + input.substring(3, 6) + '-' + input.substring(6);
		} else {	// 예: 010-1234-5678 (11자리일 경우)
			formatted = input.substring(0, 3) + '-' + input.substring(3, 7) + '-' + input.substring(7);
		}

		// 포맷팅된 결과를 다시 input에 반영
		e.target.value = formatted;
	});
	
	/**
	// 주민등록 번호
	document.getElementById('emp_bd').addEventListener('input', function(e) {
  		let input = e.target.value.replace(/\D/g, ''); // 숫자 이외의 문자는 모두 제거
  
		// 최대 11자리까지 허용 (예: 01012345678)
		if (input.length > 13) {
			input = input.substring(0, 13);
		}
  		
		let formatted = '';
		// 입력된 숫자 길이에 따라 하이픈(-)을 삽입합니다.
		if (input.length < 6) { // 예: 010
			formatted = input;
		} else  {	// 예: 010-123
			formatted = input.substring(0, 6) + '-' + input.substring(6);
		} 

		// 포맷팅된 결과를 다시 input에 반영
		e.target.value = formatted;
	});
	**/
	document.getElementById('resident').addEventListener('input', function(e) {
	  	let input = e.target.value.replace(/\D/g, ''); // 숫자만 남김

		// 최대 13자리까지만 허용
		if (input.length > 13) {
			input = input.substring(0, 13);
		}

		let formatted = '';
		if (input.length <= 6) {
			// 앞 6자리만 입력 중
			formatted = input;
		} else if (input.length === 7) {
			// 앞 6자리 + '-' + 뒷번호 첫 글자
			formatted = input.substring(0, 6) + '-' + input.charAt(6);
		} else {
			// 앞 6자리 + '-' + 뒷번호 첫 글자 + ****** (항상 6개)
			let front = input.substring(0, 6);
			let firstBack = input.charAt(6);
			formatted = front + '-' + firstBack + '******';
		}

		e.target.value = formatted;
	});
	
	
	// 주소검색
	let btnSearchAddress = document.querySelector('#btnSearchAddress');
	btnSearchAddress.onclick = function() {
		new daum.Postcode({
			oncomplete : function(data) {
				document.querySelector('#emp_pc').value = data.zonecode;

				let addr = data.address;
				if (data.buildingName != "") {
					addr += " (" + data.buildingName + ")";
				}
				document.querySelector('#emp_ad').value = addr;
				document.querySelector('#emp_ba').focus();
			}
		}).open();

	}



	// 사진 등록 함수
    function employeePhoto(event) {
        const file = event.target.files[0]; // 선택된 파일
        const imgElement = document.getElementById("pFIle");

        if (!file) return;

        // 허용 확장자
        const allowedExtensions = ['jpg', 'jpeg', 'png', 'gif'];
        const fileExtension = file.name.split('.').pop().toLowerCase();

        // 1) 확장자 체크
        if (!allowedExtensions.includes(fileExtension)) {
            alert("이미지 파일(jpg, png, gif)만 업로드 가능합니다.");
            event.target.value = ""; // 입력값 초기화
            imgElement.src = "";     // 미리보기 제거
            return;
        }

        // 2) MIME 타입 체크 (추가 보안)
        if (!file.type.startsWith("image/")) {
            alert("유효한 이미지 파일이 아닙니다.");
            event.target.value = "";
            imgElement.src = "";
            return;
        }

        // 3) 용량 체크 (5MB 제한)
        const maxSize = 5 * 1024 * 1024; // 5MB
        if (file.size > maxSize) {
            alert("이미지 파일 크기는 최대 5MB까지 가능합니다.");
            event.target.value = "";
            imgElement.src = "";
            return;
        }

        // 4) 미리보기 처리
        const reader = new FileReader();
        reader.onload = function () {
            imgElement.src = reader.result;
            imgElement.style.width = "100%";
            imgElement.style.height = "100%";
            imgElement.style.objectFit = "cover";
        }

        reader.readAsDataURL(file);
    }

	
// 	  '.notice-title'를 클릭하면 '.notice-list'에 .show 클래스를 토글
	document.querySelector('.notice-title').addEventListener('click', function() {
	document.querySelector('.notice-list').classList.toggle('show');
	});
	
	// 입력안된 것들 검사 
	
	function formBtn(){
		let form = document.getElementById("registForm");
		let name = document.getElementById("name");
		let phone = document.getElementById("phone");
		let resident = document.getElementById("resident");
		let passwd = document.getElementById("passwd");
		let staId = document.getElementById("staId");
		let dep = document.getElementById("dep");
		let pos = document.getElementById("pos");
		let level = document.getElementById("level");
		let addrNum = document.getElementById("emp_pc");
		let addr1 = document.getElementById("emp_ad");
		let addr2 = document.getElementById("emp_ba");
		let joinDate = document.getElementById("joinDate");
		
		if(name.value === ""){
			alert("이름을 입력해주세요.");
			name.focus();
			return;
		}else if(phone.value ===""){
			alert("휴대폰 번호를 입력하세요.");
			phone.focus();
			return;
		}else if(resident.value ===""){
			alert("주민등록 번호를 입력하세요.");
			resident.focus();
			return;
		}else if(passwd.value ===""){
			alert("비밀 번호를 입력하세요.");
			passwd.focus();
			return;
		}else if(staId.value ===""){
			alert("재직 현황를 선택하세요.");
			staId.focus();
			return;
		}else if(dep.value ===""){
			alert("부서를 선택하세요.");
			dep.focus();
			return;
		}else if(pos.value ===""){
			alert("직책을 선택하세요.");
			pos.focus();
			return;
		}else if(level.value ===""){
			alert("보안등급을 선택하세요.");
			level.focus();
			return;
		}else if(addrNum.value ===""){
			alert("우편번호를 입력하세요.");
			addrNum.focus();
			return;
		}else if(addr1.value ===""){
			alert("주소를 입력하세요.");
			addr1.focus();
			return;
		}else if(addr2.value ===""){
			alert("상세 주소를 입력하세요.");
			addr2.focus();
			return;
		}else if(joinDate.value ===""){
			alert("입사일을 입력하세요.");
			joinDate.focus();
			return;
		}
		form.submit();
		
		
	}
	
	