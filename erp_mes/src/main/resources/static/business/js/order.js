document.addEventListener("DOMContentLoaded", () => {
	// TUI Grid 인스턴스 변수로 선언
	let orderGrid;
	let orderDetailGrid;
	let productListGrid;

	const orderAddModalElement = document.getElementById('orderAddModal');
	const orderAddModal = new bootstrap.Modal(orderAddModalElement);
	const form = document.getElementById("orderAddForm");

	// 새로운 DOM 요소 변수
	const selectedItemsContainer = document.getElementById('selectedItemsContainer');
	const totalPriceElement = document.getElementById('totalPrice');
	const emptyMessage = document.getElementById('emptyMessage');

	// 오늘 날짜를 'YYYY-MM-DD' 형식으로 가져오는 코드
	const today = new Date();
	const year = today.getFullYear();
	const month = String(today.getMonth() + 1).padStart(2, '0');
	const day = String(today.getDate()).padStart(2, '0');
	const todayString = `${year}-${month}-${day}`;

	// 선택한 품목 정보를 저장할 배열
	let selectedProducts = [];

	// 편집 모드 관련
	let isEditMode = false;
	let editOrderId = null;
	let editItems = []; // 서버에서 불러온 편집 대상 품목들

	const addBtn = document.getElementById("addBtn");
	if (!isBUSTeam && !isAUTLevel) {
		if (addBtn) addBtn.style.display = "none";
	}

	// 동적으로 수정 버튼 생성 (등록 버튼 옆에)
	if (isBUSTeam || isAUTLevel) {
		let editBtn = document.getElementById("editBtn");
		if (!editBtn) {
			editBtn = document.createElement("button");
			editBtn.id = "editBtn";
			editBtn.type = "button";
			editBtn.className = "btn btn-secondary ms-2";
			editBtn.textContent = "수정";
			editBtn.style.display = "none"; // 기본 숨김
			// addBtn이 있는 곳의 부모에 추가 (존재하지 않으면 body에 append)
			if (addBtn && addBtn.parentNode) {
				addBtn.parentNode.insertBefore(editBtn, addBtn.nextSibling);
			} else {
				document.body.appendChild(editBtn);
			}
		}

		// 수정 버튼 클릭 이벤트는 여기서 한 번만 등록
		editBtn.addEventListener('click', () => {
			console.log("수정 버튼 클릭됨");

			// 현재 포커스된 셀 정보 가져오기
			const focused = orderGrid.getFocusedCell();
			console.log("focused:", focused);

			if (!focused) {
				alert("수정할 행을 선택해주세요.");
				return;
			}

			// 포커스된 rowKey 기반으로 행 데이터 가져오기
			const rowData = orderGrid.getRow(focused.rowKey);
			console.log("선택된 rowData:", rowData);

			// 수정 모달 열기
			openEditModal(rowData.orderId, rowData);
		});
	}

	// TUI Grid 인스턴스들을 초기화하고 데이터를 불러오는 함수
	const initializePage = () => {
		// 수주 목록 그리드 초기화
		orderGrid = new tui.Grid({
			el: document.getElementById('orderGrid'),
			scrollX: true,
			scrollY: true,
			bodyHeight: 200,
			rowHeight: 'auto',
			minBodyHeight: 200,
			emptyMessage: '조회결과가 없습니다.',
			columns: [
				{ header: '수주번호', name: 'orderId', align: 'center', sortable: true,},
				{ header: '거래처명', name: 'clientName', align: 'center' },
				{ header: '등록자 사원번호', name: 'empId', align: 'center' },
				{ header: '등록자', name: 'empName', align: 'center' },
				{
					header: '수주일', name: 'orderDate', align: 'center',
					sortable: true,
					// formatter 함수 추가
					formatter: function(value) {
						// value.value는 "2025-09-17T02:37:19"와 같은 형태
						if (value.value) {
							return value.value.split('T')[0]; // T 문자를 기준으로 날짜만 추출
						}
						return value.value;
					}
				},
				{
					header: '납기예정일', name: 'deliveryDate', align: 'center',
					sortable: true,
					editor: {
						type: 'datePicker',
						options: {
							format: 'yyyy-MM-dd',
							minDate: new Date()
						}
					},
					formatter: function(value) {
						if (value.value) {
							// "yyyy-MM-ddTHH:mm:ss.sss" 형식에서 날짜 부분만 추출
							return value.value.split('T')[0];
						}
						return '';
					}
				},
				{ header: '수주수량', name: 'totalOrderQty', align: 'center', sortable: true,},
				{
					header: '수주금액', name: 'totalOrderPrice', align: 'center',
					sortable: true,
					formatter: function(value) {
						if (value.value) {
							return value.value.toLocaleString();
						}
						return value.value;
					}
				},
				{
					header: '수주상태', name: 'orderStatus', align: 'center',
					formatter: function(value) {
						let color = '';
						let statusText = '';
						switch (value.value) {
							case 'RECEIVED':
								color = 'blue';
								statusText = '등록';
								break;
							case 'CANCELED':
								color = 'red';
								statusText = '취소';
								break;
							case 'INPRODUCTION':
								color = 'green';
								statusText = ' 생산중';
								break;
							case 'INSHIPMENT':
								color = 'brown';
								statusText = '출하진행중';
								break;
							case 'COMPLETION':
								color = 'black';
								statusText = '출하완료';
								break;
						}
						return `<span style="color: ${color}; font-weight: bold;">${statusText}</span>`;
					}
				}
			],
			data: []
		});

		// 수주 상세 목록을 위한 그리드 인스턴스
		orderDetailGrid = new tui.Grid({
			el: document.getElementById('orderDetailGrid'),
			scrollX: true,
			scrollY: true,
			bodyHeight: 200,
			minBodyHeight: 200,
			emptyMessage: '수주 목록의 행을 클릭하여 상세 정보를 확인하세요.',
			columns: [
				{ header: 'No.', name: 'id', align: 'center', width: 70 },
				{ header: '수주번호', name: 'orderId', align: 'center' },
				{ header: '품목번호', name: 'productId', align: 'center' },
				{ header: '품목명', name: 'productName', align: 'center' },
				{ header: '수량', name: 'orderQty', align: 'center', sortable: true },
				{ header: '단위', name: 'unit', align: 'center' },
				{
					header: '단가', name: 'orderPrice', align: 'center',
					sortable: true,
					formatter: function(value) {
						if (value.value) {
							return value.value.toLocaleString();
						}
						return value.value;
					}
				},
				{
					header: '총금액', name: 'totalPrice', align: 'center',
					sortable: true,
					formatter: function(value) {
						if (value.value) {
							return value.value.toLocaleString();
						}
						return value.value;
					}
				},
				{
					header: '수주상태', name: 'orderDetailStatus', align: 'center',
					formatter: function(value) {
						let color = '';
						let statusText = '';
						switch (value.value) {
							case 'RECEIVED':
								color = 'blue';
								statusText = '등록';
								break;
							case 'CANCELED':
								color = 'red';
								statusText = '취소';
								break;
							case 'INPRODUCTION':
								color = 'green';
								statusText = ' 생산중';
								break;
							case 'INSHIPMENT':
								color = 'brown';
								statusText = '출하진행중';
								break;
							case 'COMPLETION':
								color = 'black';
								statusText = '출하완료';
								break;
						}
						return `<span style="color: ${color}; font-weight: bold;">${statusText}</span>`;
					}
				}

			],
			data: []
		});

		// 페이지 로드 시 전체 수주 목록 불러오기
		loadOrders();

		
		orderGrid.on('click', async (ev) => {
			const rowData = orderGrid.getRow(ev.rowKey);

			// rowData가 존재하지 않으면 아무 작업도 하지 않고 함수를 종료
			if (!rowData) {
				// 행이 선택되지 않은 경우 수정 버튼을 숨김
				editBtn.style.display = "none";
				editBtn.removeAttribute('data-order-id');
				return;
			}

			// 수주상태 컬럼 클릭 시 취소 로직
			if (isBUSTeam || isAUTLevel) {
				if (ev.columnName === 'orderStatus') {
					if (rowData.orderStatus === 'CANCELED') {
						const reason = rowData.reason || "취소 사유가 등록되지 않았습니다.";
						alert(`[취소된 수주]\n수주번호: ${rowData.orderId}\n\n취소 사유: ${reason}`);
						return;
					}
					if (rowData.orderStatus === 'RECEIVED') {
						editBtn.style.display = "none";

						const cancelReason = prompt("수주를 취소하시겠습니까? 취소 사유를 입력해주세요.");

						// 사용자가 '취소'를 누르거나, 아무것도 입력하지 않고 '확인'을 누른 경우 처리
						if (cancelReason === null) {
							// 사용자가 prompt 창에서 '취소' 버튼을 누름
							return;
						}

						if (cancelReason.trim() === "") {
							alert("취소 사유를 반드시 입력해야 합니다.");
							return;
						}

							const orderId = rowData.orderId;
							try {
								const csrfToken = document.querySelector('meta[name="_csrf"]').content;
								const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

								const res = await fetch(`/business/api/orders/${orderId}/cancel`, {
									method: "PUT",
									headers: {
										"Content-Type": "application/json",
										[csrfHeader]: csrfToken
									},
									body: JSON.stringify({ reason: cancelReason })
								});

								if (!res.ok) {
									throw new Error(await res.text());
								}

								orderGrid.setValue(ev.rowKey, 'orderStatus', 'CANCELED');
								orderGrid.setValue(ev.rowKey, 'reason', cancelReason);
								alert("수주가 취소되었습니다.");

								loadOrderDetails(orderId);
							} catch (err) {
								console.error("수주 취소 실패:", err);
								alert("수주 취소 실패: " + err.message);
							}
					} else {
						// '등록' 상태가 아닌 경우 (생산중, 출하진행중 )
						alert("생산/출하 진행중이거나 완료된 수주는 취소할 수 없습니다.");
						return;
					}
					return;
				}
			}

			// 그 외 클릭: 상세 로드 및 수정 버튼 표시
			loadOrderDetails(rowData.orderId);

			// 수정 버튼의 표시 여부와 data 속성을 제어합니다.
			if (rowData.orderStatus === 'RECEIVED') {
				editBtn.style.display = "inline-block";
				editBtn.dataset.orderId = rowData.orderId;
			} else {
				editBtn.style.display = "none";
				editBtn.removeAttribute('data-order-id');
			}
		});

	};

	// 페이지 초기화 함수 호출
	initializePage();

	// 서버에서 목록/데이터 로드하는 함수들
	//--------------------------------------------------------
	let allOrders = [];

	function loadOrders() {
		fetch("/business/api/orders")
			.then(response => response.json())
			.then(data => {
				allOrders = data; // 전체 목록 저장
				orderGrid.resetData(allOrders); // 그리드 초기 데이터 세팅
			})
			.catch(error => console.error("수주 목록 불러오기 오류:", error));
	}

	// 검색 버튼 클릭 시 실행
	function filterOrders() {
		const status = document.getElementById("orderStatus").value;
		const keyword = document.getElementById("combinedSearch").value.trim();
		const startDate = document.getElementById("inputDateSearch").value;
		const endDate = document.getElementById("inputDateEndSearch").value;

		let filteredData = allOrders;

		// 진행상태 필터
		if (status !== "ALL") {
			filteredData = filteredData.filter(order => order.orderStatus === status);
		}

		// 거래처명/수주번호 필터
		if (keyword) {
			filteredData = filteredData.filter(order =>
				(order.clientName && order.clientName.includes(keyword)) ||
				(order.orderId && order.orderId.includes(keyword))
			);
		}

		// 납기예정일 필터
		if (startDate || endDate) {
			filteredData = filteredData.filter(order => {
				const deliveryDate = order.deliveryDate;
				if (!deliveryDate)
					return false;

				// 날짜 데이터가 유효한지 확인하고 범위 필터링
				if (startDate && endDate) {
					return deliveryDate >= startDate && deliveryDate <= endDate;
				} else if (startDate) {
					return deliveryDate >= startDate;
				} else if (endDate) {
					return deliveryDate <= endDate;
				}
				return false; // 날짜 데이터가 없으면 필터링
			});
		}

		orderGrid.resetData(filteredData);
	}

	// 검색 이벤트 바인딩
	document.getElementById("searchBtn").addEventListener("click", filterOrders);

	// 엔터키 검색
	document.getElementById("combinedSearch").addEventListener("keydown", function(e) {
		if (e.key === "Enter") {
			filterOrders();
		}
	});

	// 수주 상세 목록을 불러오는 함수
	function loadOrderDetails(orderId) {
		fetch(`/business/api/orders/${orderId}/details`)
			.then(response => {
				if (!response.ok) {
					throw new Error('네트워크 응답이 올바르지 않습니다.');
				}
				return response.json();
			})
			.then(data => {
				orderDetailGrid.resetData(data);
			})
			.catch(error => console.error("수주 상세 목록 불러오기 오류:", error));
	}

	// 수주 등록 모달창 - 거래처 리스트 불러오기 (매출사 && 거래중)
	function loadClientsForModal(isEditMode) {
		return fetch("/business/api/clients")
			.then(response => response.json())
			.then(data => {
				const selectElement = document.getElementById("clientId");
				selectElement.innerHTML = '<option value="">선택</option>';

				let filteredClients = data;

				// 수정 모드가 아닐 때만 필터링 조건을 적용
				if (!isEditMode) {
					filteredClients = data.filter(client =>
						client.clientType === '매출사' && client.clientStatus === '거래중'
					);
				}

				filteredClients.forEach(client => {
					const option = document.createElement("option");
					option.value = client.clientId;
					option.textContent = client.clientName;
					selectElement.appendChild(option);
				});
			})
			.catch(error => console.error("거래처 목록 불러오기 오류:", error));
	}

	// 수주 등록 모달창 - 품목 리스트 불러오기
	function loadProductsForModal() {
		return fetch("/business/api/products")
			.then(response => response.json())
			.then(data => {
				if (productListGrid) {
					productListGrid.resetData(data);
				}
			})
			.catch(error => console.error("품목 목록 불러오기 오류:", error));
	}

	//--------------------------------------------------------------------------------------

	// 모달/품목 선택 UI 관련
	// ----------------------------------------------------------------------------------
	if (addBtn) {
		addBtn.addEventListener("click", async () => {
			isEditMode = false;
			editOrderId = null;
			editItems = [];

			// 모달 제목/버튼 텍스트 초기화
			document.getElementById('orderModalTitle').textContent = '수주등록';
			document.getElementById('orderSubmitBtn').textContent = '등록';

			// 클라이언트 선택 활성화
			const clientSelect = document.getElementById("clientId");
			if (clientSelect) clientSelect.disabled = false;

			// 거래처 리스트를 먼저 로드하고 모달 띄움
			await loadClientsForModal(false);
			orderAddModal.show();
		});
	}

	// 모달이 완전히 표시된 후에 품목 리스트 그리드 초기화 및 데이터 로드
	orderAddModalElement.addEventListener('shown.bs.modal', async () => {
		const deliveryDateInput = document.getElementById("deliveryDate");
		if (deliveryDateInput) {
			deliveryDateInput.min = todayString;
		}
		// productListGrid가 아직 생성되지 않았다면 초기화
		if (!productListGrid) {
			productListGrid = new tui.Grid({
				el: document.getElementById('productListGrid'),
				scrollX: false,
				scrollY: true,
				rowHeaders: ['checkbox'], // 체크박스 열 추가
				bodyHeight: 280,
				columns: [
					{ header: '품목번호', name: 'productId', align: 'center', width: 100 },
					{ header: '품목명', name: 'productName', align: 'left', minwidth: 170 },
					{ header: '단위', name: 'unit', align: 'center', width: 70 },
					{
						header: '단가', name: 'price', align: 'center', minwidth: 90,
						formatter: function(value) {
							if (value.value) {
								return value.value.toLocaleString();
							}
							return value.value;
						}
					}
				],
				columnOptions: {
					resizable: true // 컬럼 너비를 사용자가 조절 가능
				},
				data: []
			});

			// TUI Grid의 resize 메서드를 호출하여 크기를 강제로 재조정
			productListGrid.refreshLayout();

			// 이벤트 리스너를 한 번만 등록
			productListGrid.on('checkAll', () => updateSelectedItems());
			productListGrid.on('uncheckAll', () => updateSelectedItems());
			productListGrid.on('check', () => updateSelectedItems());
			productListGrid.on('uncheck', () => updateSelectedItems());

			// 행 클릭 이벤트 → 체크박스 토글
			productListGrid.on('click', (ev) => {
				if (ev.rowKey != null && ev.columnName !== '_disabled') { // 'disabled'컬럼이 아닌 경우에만 토글
					const checkedRowKeys = productListGrid.getCheckedRowKeys();
					if (checkedRowKeys.includes(ev.rowKey)) {
						productListGrid.uncheck(ev.rowKey);
					} else {
						productListGrid.check(ev.rowKey);
					}
					updateSelectedItems();
				}
			});
		}

		// 품목 데이터를 먼저 로드
		await loadProductsForModal();

		if (isEditMode && editOrderId) {
			try {
				// productListGrid에서 해당 품목 체크
				const gridData = productListGrid.getData();
				productListGrid.uncheckAll();

				const toCheckRowKeys = [];
				editItems.forEach(item => {
					const idx = gridData.findIndex(r => r.productId === item.productId);
					if (idx >= 0) {
						toCheckRowKeys.push(idx);
					}
				});

				toCheckRowKeys.forEach(rk => productListGrid.check(rk));

				// selectedProducts 업데이트 후 렌더
				selectedProducts = editItems.map(item => {
					const gridItem = gridData.find(g => g.productId === item.productId);
					return {
						...gridItem,
						qty: item.orderQty
					};
				});
				renderSelectedItems();

			} catch (err) {
				console.error('수주 상세 조회 실패', err);
			}
		} else {
			// 신규 등록: 초기 렌더
			selectedProducts = [];
			renderSelectedItems();
		}
	});

	// 모달이 완전히 닫힌 후 그리드 파괴 및 상태 리셋
	orderAddModalElement.addEventListener('hidden.bs.modal', () => {
		if (productListGrid) {
			productListGrid.destroy();
			productListGrid = null;
			selectedProducts = []; // 선택된 품목 배열 초기화
			renderSelectedItems(); // 화면 초기화
		}
		// 편집 모드 리셋 (모달 닫히면)
		isEditMode = false;
		editOrderId = null;
		editItems = [];
		// 모달 제목/버튼 텍스트 원복
		const titleEl = document.getElementById('orderModalTitle');
		if (titleEl) titleEl.textContent = '수주등록';
		const submitBtn = document.getElementById('orderSubmitBtn');
		if (submitBtn) submitBtn.textContent = '등록';
		// client select 활성화
		const clientSelect = document.getElementById("clientId");
		if (clientSelect) clientSelect.disabled = false;
		// 폼 초기화
		form.reset();
	});

	// 선택된 품목 목록을 업데이트하는 함수
	const updateSelectedItems = () => {
		if (!productListGrid) return;

		const checkedRows = productListGrid.getCheckedRows();

		const newSelectedProducts = checkedRows.map(r => {
			const existingItem = selectedProducts.find(sp => sp.productId === r.productId);
			let quantity = 1; // 기본값

			if (isEditMode && editItems.length > 0) {
				// 편집 모드에서는 editItems에서 수량을 찾음
				const editItem = editItems.find(it => it.productId === r.productId);
				if (editItem) {
					quantity = editItem.orderQty; // orders_detail의 orderQty 컬럼값 사용
				}
			} else if (existingItem) {
				// 신규 등록에서는 기존에 선택된 수량 정보가 있으면 유지
				quantity = existingItem.qty;
			}

			return {
				...r,
				qty: quantity
			};
		});

		selectedProducts = newSelectedProducts;
		renderSelectedItems();
	};

	const renderSelectedItems = () => {
		selectedItemsContainer.innerHTML = '';
		let total = 0;

		if (selectedProducts.length === 0) {
			// 빈 메시지 엘리먼트가 존재하고, 선택된 품목이 없을 때만 표시
			if (emptyMessage) {
				selectedItemsContainer.appendChild(emptyMessage);
			}
		} else {
			// 빈 메시지를 다시 숨김
			if (emptyMessage && emptyMessage.parentNode) {
				emptyMessage.parentNode.removeChild(emptyMessage);
			}

			selectedProducts.forEach((item, index) => {
				const price = parseInt(item.price) || 0;
				const initialQty = item.qty ? item.qty : 1;

				const itemDiv = document.createElement('div');
				itemDiv.classList.add('d-flex', 'align-items-center', 'mb-2');
				itemDiv.dataset.productId = item.productId;

				const itemHtml = `
	                <div class="d-flex align-items-center w-100">
	                    <span class="me-2">${item.productName}</span>
	                    <input type="number" 
	                        class="form-control form-control-sm me-2 item-qty" 
	                        style="width: 60px;" 
	                        value="${initialQty}" min="1" 
	                        data-product-price="${price}" 
	                        data-unit="${item.unit}"
							data-product-id="${item.productId}"
	                        onchange="updateItemPrice(this)">
	                    <span class="me-2">×</span> 
	                    <span class="me-2">${price.toLocaleString()} 원</span>
						<span class="me-2">=</span> 
	                    <span class="item-total-price me-auto text-end">₩${(price * initialQty).toLocaleString()}</span>
	                    <button type="button" class="btn-close ms-2 remove-item-btn" aria-label="Close"></button>
	                </div>
	            `;

				itemDiv.innerHTML = itemHtml;
				selectedItemsContainer.appendChild(itemDiv);

				// remove 버튼 이벤트
				itemDiv.querySelector('.remove-item-btn').addEventListener('click', (event) => {
					event.preventDefault();
					event.stopPropagation();
					removeItem(item.productId);
				});

				total += price * initialQty;
			});
		}

		totalPriceElement.textContent = `₩${total.toLocaleString()}`;
	};

	// 개별 품목의 수량을 변경했을 때 총 가격을 업데이트
	window.updateItemPrice = (inputElement) => {
		const quantity = parseInt(inputElement.value) || 0;
		const price = parseInt(inputElement.dataset.productPrice) || 0;
		const newTotal = quantity * price;

		const productId = inputElement.dataset.productId;
		const itemDiv = selectedItemsContainer.querySelector(`[data-product-id='${productId}']`);

		// selectedProducts 배열의 수량 업데이트
		const itemToUpdate = selectedProducts.find(item => item.productId === productId);
		if (itemToUpdate) {
			itemToUpdate.qty = quantity;
		}

		if (itemDiv) {
			const el = itemDiv.querySelector('.item-total-price');
			if (el) el.textContent = `₩${newTotal.toLocaleString()}`;
		}

		// 전체 총 금액 업데이트
		updateTotalPrice();
	};

	// 총 금액을 다시 계산하는 함수
	const updateTotalPrice = () => {
		let total = 0;
		selectedProducts.forEach(item => {
			total += (item.qty || 0) * (item.price || 0);
		});
		totalPriceElement.textContent = `₩${total.toLocaleString()}`;
	};

	// 수주 품목 목록에서 항목을 제거
	window.removeItem = (productId) => {
		// TUI Grid에서 해당 체크박스 해제
		if (productListGrid) {
			const gridData = productListGrid.getData();
			const rowKey = gridData.findIndex(item => item.productId === productId);
			if (rowKey !== -1) {
				// TUI Grid의 이벤트 리스너가 다시 호출되지 않도록 `false`를 두 번째 인자로 전달합니다.
				productListGrid.uncheck(rowKey, false);
			}
		}

		// selectedProducts 배열에서도 제거
		selectedProducts = selectedProducts.filter(item => item.productId !== productId);

		// 화면 다시 렌더링
		renderSelectedItems();
	};

	// ----------------------------------------------------------------------------------------------

	// 폼 제출 이벤트 (수주 등록 및 수정)
	form.addEventListener("submit", async (event) => {
		event.preventDefault();

		// 거래처 선택 값
		const clientSelect = document.getElementById("clientId");
		const clientId = clientSelect.value;
		const clientName = clientSelect.options[clientSelect.selectedIndex] ? clientSelect.options[clientSelect.selectedIndex].text : '';

		if (!clientId) {
			alert("거래처를 선택해주세요.");
			return;
		}

		// deliveryDate: input 값이 보통 "YYYY-MM-DD" 이므로 그대로 사용 (서버의 java.sql.Date에 맞춰)
		let deliveryDate = document.getElementById("deliveryDate").value || "";
		// 안전하게 "YYYY-MM-DD" 형태로 맞춤
		if (deliveryDate.includes("T")) deliveryDate = deliveryDate.split("T")[0];

		if (!deliveryDate) {
			alert("납기예정일을 선택해주세요.");
			return;
		}

		// 품목 수집은 selectedProducts 배열을 사용
		const items = selectedProducts.map(item => {
			const orderQty = parseInt(item.qty) || 0;
			const orderPrice = parseInt(item.price) || 0;
			return {
				productId: item.productId,
				productName: item.productName,
				unit: item.unit,
				orderQty: orderQty,
				orderPrice: orderPrice,
				totalPrice: orderQty * orderPrice
			};
		});

		if (items.length === 0) {
			alert("하나 이상의 품목을 선택해주세요.");
			return;
		}

		const totalOrderQty = items.reduce((sum, item) => sum + item.orderQty, 0);
		const totalOrderPrice = items.reduce((sum, item) => sum + item.totalPrice, 0);

		const payload = {
			clientId: clientId,
			clientName: clientName,
			deliveryDate: deliveryDate,
			totalOrderQty: totalOrderQty,     // DTO 필드명
			totalOrderPrice: totalOrderPrice, // DTO 필드명
			items: items
		};

		const csrfToken = document.querySelector('meta[name="_csrf"]').content;
		const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

		try {
			let res;
			if (isEditMode && editOrderId) {
				res = await fetch(`/business/api/orders/${editOrderId}`, {
					method: "PUT",
					headers: {
						"Content-Type": "application/json",
						[csrfHeader]: csrfToken
					},
					body: JSON.stringify(payload)
				});
			} else {
				// 신규 등록
				res = await fetch("/business/api/orders/submit", {
					method: "POST",
					headers: {
						"Content-Type": "application/json",
						[csrfHeader]: csrfToken
					},
					body: JSON.stringify(payload)
				});
			}

			if (res.ok) {
				const j = await res.json();
				if (isEditMode) {
					alert("수주 수정 완료: " + (j.orderId || editOrderId));
				} else {
					alert("수주 등록 완료: " + j.orderId);
				}
				orderAddModal.hide();
				loadOrders();

				// 신규 등록 시에도 방금 등록된 주문의 상세 목록을 보여줄 수 있도록 orderId를 사용합니다.
				const updatedOrderId = isEditMode ? editOrderId : j.orderId;
				loadOrderDetails(updatedOrderId);

			} else {
				const txt = await res.text();
				console.error("서버 응답 에러:", res.status, txt);
				alert((isEditMode ? "수정 실패: " : "등록 실패: ") + txt);
			}
		} catch (err) {
			console.error(err);
			alert("서버 통신 오류");
		}
	});
	// -------------------------------------------------------------------------------------

	// 편집 모달 열기 (기존 수주 불러와서 채우기)
	async function openEditModal(orderId, rowData) {

		try {
			// 서버에서 해당 수주 정보(헤더 + 아이템)를 불러옴
			const res = await fetch(`/business/api/orders/${orderId}`);
			if (!res.ok) {
				throw new Error("수주 정보를 불러오지 못했습니다.");
			}
			const order = await res.json();

			// 편집 플래그 설정
			isEditMode = true;
			editOrderId = orderId;

			// modal 제목/버튼 텍스트 변경
			document.getElementById('orderModalTitle').textContent = '수주 수정';
			document.getElementById('orderSubmitBtn').textContent = '수정';

			// 클라이언트 로드 후 선택값 세팅 (거래처는 변경 불가)
			await loadClientsForModal(true);
			const clientSelect = document.getElementById("clientId");
			if (clientSelect) {
				clientSelect.value = order.clientId || '';
				clientSelect.disabled = true; // 거래처 변경 불가
			}

			// 납기일
			document.getElementById("deliveryDate").value = order.deliveryDate ? order.deliveryDate.split("T")[0] : "";

			// 품목 목록 저장 → shown.bs.modal 에서 반영됨
			editItems = order.items || [];

			// 모달 show (product 그리드는 shown 이벤트에서 editItems를 보고 체크/qty 반영)
			orderAddModal.show();

		} catch (err) {
			console.error("편집 모달 오픈 실패:", err);
			alert("편집 모달을 열 수 없습니다: " + err.message);
		}
	}
});