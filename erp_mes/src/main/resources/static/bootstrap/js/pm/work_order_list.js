// 작업지시 Grid
const workOrderGrid = new tui.Grid({
  el: document.getElementById('work-grid'),
  columns: [
    { header: '작업지시 번호', name: 'workOrderId', sortable: true, align: 'center' },
    { header: '생산계획', name: 'planId', sortable: true, align: 'center' },
    { header: '시작일', name: 'startDate', sortable: true, align: 'center' },
    { header: '종료일', name: 'endDate', sortable: true, align: 'center' },
    { header: '작업상태', name: 'workOrderStatus', sortable: true, align: 'center' },
    { header: '작업 담당자', name: 'empName', sortable: true, align: 'center' },
    {
      header: '발주 요청',   // 버튼 컬럼
      name: 'purchaseRequest',
      align: 'center',
	  formatter: ({ row }) => {
	    const workOrderStatus = row.workOrderStatus;

	    if (workOrderStatus === '재고부족') {
	      return `<button class="btn btn-sm btn-warning purchase-btn">발주요청</button>`;
	    } else {
	      return `<button class="btn btn-sm btn-secondary purchase-btn" disabled>발주요청</button>`;
	    }
	  }
    }
  ],
  bodyHeight: 500
});

async function loadWorkOrder() {
	const res = await fetch('workOrderList');
	const workOrderList = await res.json();
	
	workOrderGrid.resetData(workOrderList);
}

loadWorkOrder();

// ==================================
// 작업지시 등록 모달 버튼
	document.addEventListener('DOMContentLoaded', () => {
	    const btn = document.getElementById('workOrderModalBtn');
	    const modalEl = document.getElementById('workOrderRegisterModal');
	    const modal = new bootstrap.Modal(modalEl);
	
	    btn.addEventListener('click', () => {
	        modal.show();
	    });
	});
	
	
