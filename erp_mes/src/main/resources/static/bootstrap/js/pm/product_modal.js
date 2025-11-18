
	// 제품 등록 ajax
	document.getElementById("productRegisterBtn").addEventListener("click", async () => {
	    const form = document.getElementById("productForm");
	    const formData = new FormData(form);

	    // FormData → JSON 변환
	    const data = {};
	    formData.forEach((value, key) => {
	        data[key] = value;
	    });

	    try {
	        const res = await fetch("/masterData/productRegist", {
	            method: "POST",
	            headers: {
	                "Content-Type": "application/json",
					[csrfHeader]: csrfToken
	            },
	            body: JSON.stringify(data)
	        });

	        if (res.ok) {
	            alert("제품이 등록되었습니다!");
	            // 모달 닫기
				$('#productRegisterModal').modal('hide');

	            // 제품 목록 새로고침
	            location.reload();
	        } else {
	            alert("등록 실패!");
	        }
	    } catch (err) {
	        console.error(err);
	        alert("오류 발생");
	    }
	});
	
	






