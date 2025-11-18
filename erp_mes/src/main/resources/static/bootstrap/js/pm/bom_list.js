// bom_list.js
document.addEventListener('DOMContentLoaded', () => {
	
    // 전역 변수: 선택된 제품
    window.selectedProduct = null;

    // BOM Grid 초기화
    window.bomGrid = new tui.Grid({
        el: document.getElementById('bom-grid'),
        columns: [
            { header: '원자재 코드', name: 'materialId', sortable: true, align: 'center' },
            { header: '원자재명', name: 'materialName', sortable: true, align: 'center' },
            { header: '필요 수량', name: 'quantity', sortable: true, align: 'center' },
            { header: '단위', name: 'unit', sortable: true, align: 'center' },
            { header: '등록일', name: 'createdAt', sortable: true, align: 'center' },
            { header: '수정일', name: 'updatedAt', sortable: true, align: 'center' }
        ],
        bodyHeight: 'fitToParent'
    });

    // 특정 제품의 BOM 로드 함수
    window.loadBomByProduct = async function(productId) {
        if (!productId) return;

        try {
            const res = await fetch(`/masterData/bomList?product_id=${productId}`);
            if (!res.ok) throw new Error("HTTP 오류 " + res.status);

            const bomList = await res.json();

            if (!Array.isArray(bomList)) {
                console.error("BOM 데이터가 배열이 아닙니다:", bomList);
                return;
            }

            window.bomGrid.resetData(bomList);
        } catch (err) {
            console.error("BOM 로드 실패:", err);
        }
    }

    // 제품 클릭 시 BOM 로드 및 선택 제품 저장
    if (window.prodGrid) {
        window.prodGrid.on('click', ev => {
            const rowData = window.prodGrid.getRow(ev.rowKey);
            if (rowData) {
                window.selectedProduct = rowData;

                if (typeof window.loadBomByProduct === "function") {
                    window.loadBomByProduct(rowData.productId);
                }
            }
        });
    }

    // BOM 모달 열기 및 선택 제품 정보 채우기
    const bomModalBtn = document.getElementById('bomModalBtn');
    const bomModalEl = document.getElementById('bomRegisterModal');
    const bomModal = new bootstrap.Modal(bomModalEl);

    bomModalBtn.addEventListener('click', () => {
        if (!window.selectedProduct) {
            alert("제품을 먼저 선택해주세요.");
            return;
        }

        // 모달 input 값 채우기 (readonly 용)
        document.getElementById('bomProductId').value = window.selectedProduct.productId || "";
        document.getElementById('bomProductName').value = window.selectedProduct.productName || "";

        bomModal.show();
    });

	// 자재 추가 버튼
	document.getElementById("addMaterialBtn").addEventListener("click", () => {
	    const container = document.getElementById("materialContainer");
	    const firstSelect = document.querySelector("select[name='unit']"); // 기존 select 찾기
	    const newSelect = firstSelect.cloneNode(true); // 옵션 포함 전체 복제
	    newSelect.value = ""; // 새로 추가된 셀렉트는 초기값 비우기

	    const newRow = document.createElement("div");
	    newRow.classList.add("row", "mb-2", "materialRow");

	    newRow.innerHTML = `
	        <div class="col">
	          <input type="text" name="materialId" class="form-control" placeholder="원자재 코드">
	        </div>
	        <div class="col">
	          <input type="text" name="quantity" class="form-control" placeholder="수량">
	        </div>
	        <div class="col select-col"></div>
	        <div class="col">
	          <button type="button" class="btn btn-danger btn-sm removeRow">삭제</button>
	        </div>
	    `;

	    // 셀렉트 자리에 삽입
	    newRow.querySelector(".select-col").appendChild(newSelect);
	    container.appendChild(newRow);
	});

	// 삭제 버튼 동작
	document.addEventListener("click", (e) => {
	    if (e.target.classList.contains("removeRow")) {
	        e.target.closest(".materialRow").remove();
	    }
	});
	
	
    // BOM 등록 AJAX 처리

    document.getElementById("bomRegisterBtn").addEventListener("click", async () => {
        const form = document.getElementById("bomForm");
        const formData = new FormData(form);

		const data = {
			bomId: formData.get("bomId"),
		    productId: formData.get("productId"),
		    revisionNo: parseInt(formData.get("revisionNo")),
		    materials: []
		};

		const materialIds = formData.getAll("materialId");
		const quantities = formData.getAll("quantity");
		const units = formData.getAll("unit");

		materialIds.forEach((m, i) => {
		    if (m) {
		        data.materials.push({
		            materialId: m,
		            quantity: parseFloat(quantities[i]),
		            unit: units[i]
		        });
		    }
		});

		
        try {
            const res = await fetch("/masterData/bomRegist", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(data)
            });

            if (res.ok) {
                alert("BOM이 등록되었습니다!");
				// 모달 닫기
				$('#bomRegisterModal').modal('hide');

				// 제품 목록 새로고침
				location.reload();

                // 선택 제품 기준으로 BOM 다시 로드
                if (window.selectedProduct) {
                    window.loadBomByProduct(window.selectedProduct.productId);
                }
            } else {
                alert("등록 실패!");
            }
        } catch (err) {
            console.error(err);
            alert("오류 발생");
        }
    });
});
