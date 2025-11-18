// 생산계획 Grid
const planGrid = new tui.Grid({
  el: document.getElementById('plan-grid'),
  columns: [
    { header: '생산계획 번호', name: 'planId', sortable: true, align: 'center' },
    { header: '제품코드', name: 'productId', sortable: true, align: 'center' },
    { header: '제품명', name: 'productName', sortable: true, align: 'center' },
    { header: '생산 수량', name: 'planQuantity', sortable: true, align: 'center' },
    { header: '시작일', name: 'startDate', sortable: true, align: 'center' },
    { header: '종료일', name: 'endDate', sortable: true, align: 'center' },
    { header: '납기일', name: 'dueDate', sortable: true, align: 'center' },
  ],
  bodyHeight: 500
});

async function loadProductPlan() {
	const res = await fetch('productPlanList');
	const productPlanList = await res.json();
	
	planGrid.resetData(productPlanList);
}

loadProductPlan();

// ==================================
// 생산계획 등록 모달 버튼
//	document.addEventListener('DOMContentLoaded', () => {
//	    const btn = document.getElementById('planModalBtn');
//	    const modalEl = document.getElementById('planRegisterModal');
//	    const modal = new bootstrap.Modal(modalEl);
//	
//	    btn.addEventListener('click', () => {
//	        modal.show();
//	    });
//	});
	
	
