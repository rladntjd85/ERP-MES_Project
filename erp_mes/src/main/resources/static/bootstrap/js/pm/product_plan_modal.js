document.addEventListener('DOMContentLoaded', () => {
    let orderGrid; 
    const modalEl = $('#planRegisterModal');
    const btn = $('#planModalBtn');

    btn.on('click', () => {
        modalEl.modal('show');
    });

    modalEl.on('shown.bs.modal', function () {
        if (!orderGrid) {
            orderGrid = new tui.Grid({
                el: document.getElementById('order-grid'),
                columns: [
                    { header: 'id', name: 'id', sortable: true, align: 'center' },
                    { header: '수주번호', name: 'orderId', sortable: true, align: 'center' },
                    { header: '제품명', name: 'productName', sortable: true, align: 'center' },
                    { header: '총수량', name: 'orderQty', sortable: true, align: 'center' },
                    { header: '상태', name: 'orderDetailStatus', sortable: true, align: 'center' },
                    { header: '납기일', name: 'deliveryDate', sortable: true, align: 'center' },
                ],
                bodyHeight: 180
            });

            // ✅ 여기서 이벤트 바인딩
            orderGrid.on('click', (ev) => {
                const rowData = orderGrid.getRow(ev.rowKey);
                if (rowData) {
                    const orderId = rowData.orderId;
                    const id = rowData.id;
                    loadProductsForOrder(orderId, id);
                }
            });
        }

        loadOrders();
    });

    // 수주 리스트 불러오기
    async function loadOrders() {
        try {
            const res = await fetch('/pm/ordersList');
            const ordersList = await res.json();
            if (orderGrid) orderGrid.resetData(ordersList);
        } catch (err) {
            console.error("수주 리스트 로드 실패:", err);
        }
    }

    // 수주 상세 조회
    async function loadProductsForOrder(orderId, id) {
        try {
            const res = await fetch(`/pm/ordersProduct?order_id=${orderId}&id=${id}`);
            const products = await res.json();

            if (products.length > 0) {
                const p = products[0]; // 어차피 orderId + id는 unique

				// 수주번호
                document.getElementById("order_id").value = p.orderId;
				
                // 제품 세팅
                document.getElementById("product_name").value = p.productName;
                document.getElementById("product_id").value = p.productId;

                // 생산수량 세팅
                document.getElementById("plan_quantity").value = p.orderQty;

                // 납기일 세팅
                document.getElementById("due_date").value = p.deliveryDate;
            }
        } catch (err) {
            console.error("제품 조회 실패:", err);
        }
    }

    // 생산계획 등록
    document.getElementById("planRegisterBtn").addEventListener("click", async () => {
        const form = document.getElementById("planForm");
        const formData = new FormData(form);

		/*for (let pair of formData.entries()) {
		    console.log(pair[0]+ ': ' + pair[1]);  // 확인
		}*/
		
		
        const data = {};
        formData.forEach((value, key) => {
            data[key] = value;
        });

        try {
            const res = await fetch("/pm/productPlanRegist", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(data)
            });
            
            console.log(">>>>>>>"+res)

            if (res.ok) {
                alert("생산계획이 등록되었습니다!");
                $('#planRegisterModal').modal('hide');
                location.reload();
            } else {
                alert("등록 실패!");
            }
        } catch (err) {
            console.error(err);
            alert("오류 발생");
        }
    });
});