
// 출근 버튼
	$(document).ready(function() {
	    const checkInBtn = $("#checkInBtn");
	
	    checkInBtn.on("click", function() {
// 	    	console.log("출근버튼 확인");
	        if (!confirm("출근하시겠습니까?")) return;
	
	        $.ajax({
	            url: "/attendance/checkIn",
	            type: "POST",
	            contentType: "application/json",
	            data: JSON.stringify({}), // 기존 fetch에서 body 부분
				beforeSend: function(xhr) { // AJAX 실제 요청 전 먼저 요청할 정보(주로 헤더정보)
					xhr.setRequestHeader(csrfHeader, csrfToken)
				},
	            success: function(data) {
	                if (data && data.checkInTime) {
	                    const rawTime = data.checkInTime;
	                    const dateObj = new Date(rawTime);
	
	                    // yyyy-MM-dd HH:mm:ss 형식으로 변환
	                    const formatted = dateObj.getFullYear() + "-" +
	                                      String(dateObj.getMonth() + 1).padStart(2, '0') + "-" +
	                                      String(dateObj.getDate()).padStart(2, '0') + " " +
	                                      String(dateObj.getHours()).padStart(2, '0') + ":" +
	                                      String(dateObj.getMinutes()).padStart(2, '0') + ":" +
	                                      String(dateObj.getSeconds()).padStart(2, '0');
	
	                    alert(`출근 완료: ${formatted}`);
						
//						$("#commute_start_time").val(formatted);
						
	                    location.reload(); // 필요 시 리스트 새로고침
	                }
	            },
	            error: function(xhr) {
	                if (xhr.status === 401) {
	                    alert("로그인이 필요합니다.");
	                    window.location.href = "/login";
	                } else if (xhr.status === 409) {
	                    alert("이미 오늘 출근한 상태입니다.");
	                    window.location.href = "/attendance/commuteList";
	                } else {
	                    alert("출근 처리 중 오류 발생: " + xhr.status);
	                    console.error(xhr);
	                }
	            }
	        });
	    });
	});
	
	// 퇴근 버튼
	$(document).ready(function() {
	    const checkOutBtn = $("#checkOutBtn");
	
	    checkOutBtn.on("click", function() {
// 	    	console.log("퇴근버튼 확인");
	        if (!confirm("퇴근하시겠습니까?")) return false;
	
	        $.ajax({
	            url: "/attendance/checkOut",
	            type: "POST",
	            contentType: "application/json",
	            data: JSON.stringify({}), // 기존 fetch에서 body 부분
				beforeSend: function(xhr) { // AJAX 실제 요청 전 먼저 요청할 정보(주로 헤더정보)
					xhr.setRequestHeader(csrfHeader, csrfToken)
				},
	            success: function(data) {
	                if (data && data.checkOutTime) {
	                    const rawTime = data.checkOutTime;
	                    const dateObj = new Date(rawTime);
	
	                    // yyyy-MM-dd HH:mm:ss 형식으로 변환
	                    const formatted = dateObj.getFullYear() + "-" +
	                                      String(dateObj.getMonth() + 1).padStart(2, '0') + "-" +
	                                      String(dateObj.getDate()).padStart(2, '0') + " " +
	                                      String(dateObj.getHours()).padStart(2, '0') + ":" +
	                                      String(dateObj.getMinutes()).padStart(2, '0') + ":" +
	                                      String(dateObj.getSeconds()).padStart(2, '0');
	
	                    alert(`퇴근 완료: ${formatted}`);
//						$("#commute_end_time").val(formatted);
	                    location.reload(); // 리스트 새로고침
	                }
	            },
	            error: function(xhr) {
	                if (xhr.status === 401) {
	                    alert("로그인이 필요합니다.");
	                    window.location.href = "/login";
	                } else if (xhr.status === 409) {
	                    alert("이미 오늘 퇴근한 상태입니다.");
	                    window.location.href = "/attendance/commuteList";
	                } else {
	                    alert("퇴근 처리 중 오류 발생: " + xhr.status);
	                    console.error(xhr);
	                }
	            }
	        });
	    });
	});