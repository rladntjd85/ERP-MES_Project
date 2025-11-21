package com.erp_mes.mes.stock.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp_mes.erp.config.util.SessionUtil;
import com.erp_mes.mes.lot.trace.TrackLot;
import com.erp_mes.mes.stock.dto.WarehouseDTO;
import com.erp_mes.mes.stock.mapper.WareMapper;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class WareService {
    
    private final WareMapper wareMapper;
    
    // ==================== 창고 관리 ====================
    
    // 창고 목록 조회
    @Transactional(readOnly = true)
    public List<WarehouseDTO> getWarehouseList(String warehouseType, String warehouseStatus, String searchKeyword) {
        log.info("창고 목록 조회 - 유형: {}, 상태: {}, 검색어: {}", warehouseType, warehouseStatus, searchKeyword);
        return wareMapper.selectWarehouseList(warehouseType, warehouseStatus, searchKeyword);
    }
    
    // 창고 타입별 목록 조회
    @Transactional(readOnly = true)
    public List<WarehouseDTO> getWarehouseListByType(String warehouseType) {
        return wareMapper.selectWarehouseListByType(warehouseType);
    }
    
    // 신규 창고 등록
    @Transactional
    public void addWarehouse(WarehouseDTO dto) {
        log.info("창고 등록: {}", dto.getWarehouseId());
        
        if(wareMapper.existsWarehouseById(dto.getWarehouseId())) {
            throw new RuntimeException("이미 존재하는 창고ID입니다.");
        }
        
        wareMapper.insertWarehouse(dto);
    }
    
    // 창고 정보 수정
    @Transactional
    public boolean updateWarehouse(WarehouseDTO dto) {
        log.info("창고 수정: {}", dto.getWarehouseId());
        return wareMapper.updateWarehouse(dto) > 0;
    }
    
    // 창고 삭제 (재고 확인)
    @Transactional
    public Map<String, Object> deleteWarehouses(List<String> warehouseIds) {
        log.info("창고 삭제 요청: {} 건", warehouseIds.size());
        
        List<String> canDelete = new ArrayList<>();
        List<String> cannotDelete = new ArrayList<>();
        
        // 재고 보유 여부 확인
        for(String warehouseId : warehouseIds) {
            int inUseCount = wareMapper.checkWarehouseInUse(warehouseId);
            
            if(inUseCount > 0) {
                cannotDelete.add(warehouseId);
            } else {
                canDelete.add(warehouseId);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        
        // 삭제 가능한 것만 처리
        if(!canDelete.isEmpty()) {
            wareMapper.deleteWarehouses(canDelete);
            result.put("deleted", canDelete.size());
        }
        
        if(!cannotDelete.isEmpty()) {
            result.put("failed", cannotDelete);
            result.put("failedCount", cannotDelete.size());
        }
        
        result.put("success", cannotDelete.isEmpty());
        return result;
    }
    
    // ==================== 입고 관리 ====================
    
    // 입고 목록 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getInputList(String inType, String inStatus) {
        return wareMapper.selectInputList(inType, inStatus);
    }
    
    // 배치별 입고 목록 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getInputListByBatch(String batchId) {
        return wareMapper.selectInputListByBatch(batchId);
    }
    
    // 날짜별 그룹화된 입고 목록 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getGroupedInputList(String date, String inType) {
        return wareMapper.selectGroupedInputList(date, inType);
    }

    // 개별 입고 등록
    @Transactional
    public String addInput(Map<String, Object> params) {
        String itemType = (String) params.get("itemType");
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        Integer todayCount = wareMapper.getTodayInputCount(today);
        if(todayCount == null) todayCount = 0;
        
        Integer inCount = Integer.parseInt(params.get("inCount").toString());
        if(inCount > 1000) {
            throw new RuntimeException("한 번에 1000개까지만 입고 가능합니다!");
        }

        String inId = "IN" + today + String.format("%03d", todayCount + 1);
        params.put("inId", inId);
        
        // 발주 연계 처리 - BATCH 생성 시점에 WAITING으로 변경
        String purchaseId = (String) params.get("purchaseId");
        
        if(params.get("inReason") == null || "".equals(params.get("inReason"))) {
            params.put("inReason", params.get("inType"));
        }

        if("product".equals(itemType)) {
            params.put("inStatus", "입고대기");
            params.put("productId", params.get("productId"));
            wareMapper.insertInput(params);
            
//            String empId = (String) params.get("empId");
//            completeInput(inId, empId);
            
            log.info("완제품 입고 처리: {}", inId);

        } else {
            params.put("inStatus", "입고대기");
            String materialId = (String) params.get("productId");
            params.put("materialId", materialId);
            params.remove("productId");

            wareMapper.insertInput(params);
            
            // 발주 연계 시 상태를 WAITING으로 변경
            if(purchaseId != null && !purchaseId.isEmpty()) {
                wareMapper.updatePurchaseDetailStatus(purchaseId, materialId, "WAITING");
                updatePurchaseMainStatus(purchaseId);
            }
            
            log.info("부품/반제품 입고 대기: {}", inId);
        }
        
        return inId;
    }
    // 발주 메인 상태 업데이트
    private void updatePurchaseMainStatus(String purchaseId) {
        // 모든 상세가 REQUEST가 아닌지 확인
        List<Map<String, Object>> details = wareMapper.selectPurchaseDetails(purchaseId);
        boolean hasWaitingOrComplete = details.stream()
            .anyMatch(d -> "WAITING".equals(d.get("status")) || "COMPLETION".equals(d.get("status")));
        
        if(hasWaitingOrComplete) {
            wareMapper.updatePurchaseStatus(purchaseId, "WAITING");
        }
    }
    // 발주 완료 체크
    private void checkAndUpdatePurchaseCompletion(String purchaseId) {
        List<Map<String, Object>> details = wareMapper.selectPurchaseDetails(purchaseId);
        boolean allComplete = details.stream()
            .allMatch(d -> "COMPLETION".equals(d.get("status")));
        
        if(allComplete) {
            wareMapper.updatePurchaseStatus(purchaseId, "COMPLETION");
            log.info("발주 {} 모든 품목 입고 완료", purchaseId);
        }
    }

    
    // 오늘 배치 건수 조회
    public Integer getTodayBatchCount(String today) {
        Integer count = wareMapper.getTodayBatchCount(today);
        return count != null ? count : 0;
    }

    // 입고 검사 완료 처리 (로트 처리)
    @Transactional
    public void completeInput(String inId, String empId) {

        // 1) 입고 정보 조회
        Map<String, Object> input = wareMapper.selectInputById(inId);

        if (input == null) {
            throw new RuntimeException("입고 정보를 찾을 수 없습니다.");
        }

        String currentStatus = (String) input.get("IN_STATUS");
        if ("입고완료".equals(currentStatus)) {
            log.info("이미 입고완료 처리된 건입니다: {}", inId);
            return;
        }

        String materialId  = (String) input.get("MATERIAL_ID");
        String productId   = (String) input.get("PRODUCT_ID");
        String warehouseId = (String) input.get("WAREHOUSE_ID");
        String purchaseId  = (String) input.get("PURCHASE_ID");
        Integer inCount    = ((Number) input.get("IN_COUNT")).intValue();

        log.info("=== 입고완료 처리 시작 ===");
        log.info("materialId: {}, productId: {}, purchaseId: {}", materialId, productId, purchaseId);


        // 2) 발주가 있다면 발주 상태 처리
        if (purchaseId != null && !purchaseId.isEmpty()) {
            try {
                wareMapper.updatePurchaseDetailStatus(purchaseId, materialId, "COMPLETION");

                List<Map<String, Object>> detailList = wareMapper.selectPurchaseDetails(purchaseId);
                boolean allDone = detailList.stream()
                        .allMatch(d -> "COMPLETION".equals(d.get("status")));

                if (allDone) {
                    wareMapper.updatePurchaseStatus(purchaseId, "COMPLETION");
                }

            } catch (Exception e) {
                log.error("발주 연계 처리 중 오류 발생 (입고는 계속 진행)", e);
            }
        }


        // 3) 로케이션 자동 배정 (자재/완제품 공통)
        String firstLocation = null;

        if (warehouseId != null) {

            // 자재 입고
            if (materialId != null) {
                firstLocation = distributeToWarehouseItemsForMaterial(
                        warehouseId, materialId, inCount, empId
                );
            }
            // 완제품 입고
            else if (productId != null) {
                firstLocation = distributeToWarehouseForProductSimple(
                        warehouseId, productId, inCount, empId
                );
            }
        }

        log.info("배정된 로케이션: {}", firstLocation);


        // 4) 입고 상태 / 위치 업데이트
        wareMapper.updateInputStatus(inId, "입고완료");

        if (firstLocation != null) {
            wareMapper.updateInputLocation(inId, firstLocation);
        }

        log.info("=== 입고 완료 처리 종료 ===");
    }
    
    private String distributeToWarehouseForProductSimple(String warehouseId, String productId, Integer count, String empId) {
        // 실제 로직 없으면 기본 위치만 반환해도 됨
        return "DEFAULT";
    }

    
    // 새로 추가: 발주 관련 메서드만
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPendingPurchases() {
        return wareMapper.selectPendingPurchases();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPurchaseDetails(String purId) {
        return wareMapper.selectPurchaseDetails(purId);
    }

    private void checkAndUpdatePurchaseStatus(String purId) {
        List<Map<String, Object>> details = wareMapper.selectPurchaseDetails(purId);
        boolean allWaitingOrComplete = details.stream()
            .allMatch(d -> !"REQUEST".equals(d.get("status")));
        
        if(allWaitingOrComplete) {
            wareMapper.updatePurchaseStatus(purId, "WAITING");
        }
    }

    private void updatePurchaseCompletion(String purId, String materialId) {
        Map<String, Object> detail = wareMapper.selectPurchaseDetailByMaterial(purId, materialId);
        if(detail != null) {
            Integer orderQty = ((Number) detail.get("orderQty")).intValue();
            Integer receivedQty = ((Number) detail.get("receivedQty")).intValue();
            
            if(receivedQty >= orderQty) {
                wareMapper.updatePurchaseDetailStatus(purId, materialId, "COMPLETION");
                
                // 전체 발주 완료 체크
                List<Map<String, Object>> allDetails = wareMapper.selectPurchaseDetails(purId);
                boolean allComplete = allDetails.stream()
                    .allMatch(d -> "COMPLETION".equals(d.get("status")));
                
                if(allComplete) {
                    wareMapper.updatePurchaseStatus(purId, "COMPLETION");
                }
            }
        }
    }
    
    private String distributeToWarehouseItemsForMaterial(String warehouseId, String materialId, Integer totalCount, String empId) {
        String firstLocation = null;
        int remaining = totalCount;
        int maxPerLocation = 1000;
        int maxLocationsPerMaterial = 6;
        
        // 현재 이 material이 사용 중인 위치 수 확인
        int currentLocationCount = wareMapper.getLocationCountForMaterial(warehouseId, materialId);
        
        while(remaining > 0) {
            // 재사용 가능한 위치 확인 (item_amount = 0인 곳)
            List<String> reusableLocations = wareMapper.getReusableLocations(warehouseId, materialId);
            
            if(!reusableLocations.isEmpty()) {
                // 기존 코드 그대로 (재사용 로직)
                for(String locationId : reusableLocations) {
                    if(remaining <= 0) break;
                    
                    int amountToStore = Math.min(remaining, maxPerLocation);
                    
                    if(firstLocation == null) {
                        firstLocation = locationId;
                    }
                    
                    Map<String, Object> params = new HashMap<>();
                    params.put("locationId", locationId);
                    params.put("materialId", materialId);
                    params.put("addAmount", amountToStore);
                    
                    wareMapper.updateWarehouseItemAmountMaterial(params);
                    
                    remaining -= amountToStore;
                    log.info("기존 위치 {} 재사용, {} 개로 업데이트", locationId, amountToStore);
                }
            }
            
            // 재사용 위치가 없거나 부족하면 새 위치에 INSERT
            if(remaining > 0) {
                // 6개 제한 체크
                if(currentLocationCount >= maxLocationsPerMaterial) {
                    throw new RuntimeException(
                        String.format("자재 %s는 최대 %d개 위치까지만 사용 가능합니다", 
                            materialId, maxLocationsPerMaterial)
                    );
                }
                
                List<String> emptyLocations = wareMapper.getEmptyLocations(warehouseId);
                
                // *** 여기가 핵심 수정 부분 ***
                if(emptyLocations.isEmpty()) {
                    // 빈 위치가 없으면 자동 생성
                    String newLocationId = String.format("%s-%s-%02d", 
                        warehouseId, 
                        materialId.length() > 4 ? materialId.substring(materialId.length()-4) : materialId,
                        currentLocationCount + 1
                    );
                    
                    // item_location 테이블에 새 위치 추가
                    Map<String, Object> locParams = new HashMap<>();
                    locParams.put("warehouseId", warehouseId);
                    locParams.put("locationId", newLocationId);
                    locParams.put("locZone", "AUTO");
                    locParams.put("locRack", String.format("%02d", currentLocationCount + 1));
                    locParams.put("locLevel", "01");
                    locParams.put("locCell", "01");
                    locParams.put("zoneYn", "Y");
                    locParams.put("empId", empId);
                    
                    wareMapper.insertItemLocation(locParams);
                    log.info("자재 {}용 새 위치 {} 자동 생성", materialId, newLocationId);
                    
                    // 생성한 위치를 리스트에 추가
                    emptyLocations = Arrays.asList(newLocationId);
                }
                
                // 빈 위치들 중에서 INSERT 시도 (기존 코드 그대로)
                boolean inserted = false;
                for(String locationId : emptyLocations) {
                    int amountToStore = Math.min(remaining, maxPerLocation);
                    
                    if(firstLocation == null) {
                        firstLocation = locationId;
                    }
                    
                    Map<String, Object> params = new HashMap<>();
                    params.put("manageId", warehouseId + "_" + materialId + "_" + locationId);
                    params.put("warehouseId", warehouseId);
                    params.put("materialId", materialId);
                    params.put("itemAmount", amountToStore);
                    params.put("locationId", locationId);
                    params.put("empId", empId);
                    
                    int result = wareMapper.insertWarehouseItemMaterial(params);
                    
                    if(result > 0) {
                        remaining -= amountToStore;
                        currentLocationCount++;  // 사용 위치 수 증가
                        log.info("새 위치 {}에 {} 개 저장", locationId, amountToStore);
                        inserted = true;
                        break;
                    }
                }
                if(!inserted) {
                    throw new RuntimeException("창고에 저장 가능한 위치가 없습니다");
                }
            }
        }
        return firstLocation;
    }
    
    // 완제품 warehouse_item 분산 저장 메서드 추가
    private String distributeToWarehouseItemsForProduct(String warehouseId, String productId, Integer totalCount, String empId) {
        String firstLocation = null;
        int remaining = totalCount;
        int maxPerLocation = 1000;
        
        // 기존 위치 확인
        List<Map<String, Object>> existingItems = wareMapper.getPartiallyFilledLocationsProduct(warehouseId, productId, maxPerLocation);
        
        // 기존 위치 채우기
        for(Map<String, Object> item : existingItems) {
            if(remaining <= 0) break;
            
            String locationId = (String) item.get("locationId");
            Integer currentAmount = ((Number) item.get("itemAmount")).intValue();
            int availableSpace = maxPerLocation - currentAmount;
            int amountToAdd = Math.min(remaining, availableSpace);
            
            if(firstLocation == null) {
                firstLocation = locationId;
            }
            
            Map<String, Object> params = new HashMap<>();
            params.put("locationId", locationId);
            params.put("productId", productId);
            params.put("addAmount", amountToAdd);
            
            wareMapper.updateWarehouseItemAmountProduct(params);
            
            remaining -= amountToAdd;
        }
        
        // 새 위치 할당
        while(remaining > 0) {
            List<String> emptyLocations = wareMapper.getEmptyLocations(warehouseId);
            
            if(emptyLocations.isEmpty()) {
                log.warn("창고 {}에 빈 위치가 없습니다. 남은 수량: {}", warehouseId, remaining);
                break;
            }
            
            String locationId = emptyLocations.get(0);
            int amountToStore = Math.min(remaining, maxPerLocation);
            
            if(firstLocation == null) {
                firstLocation = locationId;
            }
            
            Map<String, Object> params = new HashMap<>();
            params.put("manageId", warehouseId + "_" + productId + "_" + locationId);
            params.put("warehouseId", warehouseId);
            params.put("productId", productId);
            params.put("itemAmount", amountToStore);
            params.put("locationId", locationId);
            params.put("empId", empId);
            
            wareMapper.insertWarehouseItemProduct(params);
            
            remaining -= amountToStore;
        }
        
        return firstLocation;
    }
    
    // 입고 반려 처리
    @Transactional
    public void rejectInput(String inId, String reason, String empId) {
        Map<String, Object> input = wareMapper.selectInputById(inId);
        
        if(input == null) {
            throw new RuntimeException("입고 정보를 찾을 수 없습니다.");
        }
        
        String status = (String) input.get("IN_STATUS");
        
        // 입고대기 상태만 반려 가능
        if(!"입고대기".equals(status)) {
            throw new RuntimeException("입고대기 상태에서만 반려할 수 있습니다.");
        }
        
        // 입고 상태를 입고반려로 변경
        wareMapper.updateInputStatusWithReason(inId, "입고반려", reason);
        
        log.info("입고 반려 처리: {} (사유: {}, 처리자: {})", inId, reason, empId);
    }
    
    // 반려 사유 목록 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRejectReasons() {
        return wareMapper.selectRejectReasons();
    }
    
    // 생산 완료 제품 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCompletedProduction(String date) {
        List<Map<String, Object>> result = wareMapper.selectTodayProductionForInput(date);
        log.info("생산 완료 조회 결과: {}", result.size());
        return result;
    }
    
    // 생산 완료 제품 배치 입고 (입고대기로 먼저)
    @Transactional
    public String addProductionBatch(List<Map<String, Object>> items, String empId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        Integer batchCount = wareMapper.getTodayBatchCount(today);
        String batchId = "PB" + today + String.format("%03d", batchCount + 1);
        
        log.info("생산 배치 입고 시작: {} 건", items.size());
        
        for(Map<String, Object> item : items) {
            try {
                Object resultIdObj = item.get("resultId");
                Number resultId = null;
                if(resultIdObj != null) {
                    resultId = resultIdObj instanceof Number ? 
                        (Number) resultIdObj : Long.parseLong(String.valueOf(resultIdObj));
                }
                
                item.put("empId", empId);
                item.put("batchId", batchId);
                item.put("resultId", resultId);
                item.put("inStatus", "입고대기"); 
                
                String inId = addInput(item);
                log.info("입고대기 등록: inId={}", inId);
                
                if(resultId != null) {
                    wareMapper.updateWorkResultInId(String.valueOf(resultId), inId);
                }
                
                
            } catch(Exception e) {
                log.error("입고 처리 중 에러:", e);
                throw e;
            }
        }
        
        return batchId;
    }
    
    // 완제품 입고 완료 처리 (개별 LOT 부여)
    @Transactional
    @TrackLot(tableName = "input", pkColumnName = "IN_ID")
    public void completeProductInput(String inId, String empId) {
        Map<String, Object> input = wareMapper.selectInputById(inId);
        
        if(input == null) {
            throw new RuntimeException("입고 정보를 찾을 수 없습니다.");
        }
        
        String currentStatus = (String) input.get("IN_STATUS");
        
        if("입고완료".equals(currentStatus)) {
            log.info("이미 입고완료된 건입니다: {}", inId);
            return;
        }
        
        String productId = (String) input.get("PRODUCT_ID");
        String warehouseId = (String) input.get("WAREHOUSE_ID");
        Integer inCount = ((Number) input.get("IN_COUNT")).intValue();
        
        // warehouse_item 재고 증가 처리
        String firstLocation = distributeToWarehouseItemsForProduct(
            warehouseId, productId, inCount, empId
        );
        
        // 입고 상태를 입고완료로 변경
        wareMapper.updateInputStatus(inId, "입고완료");
        wareMapper.updateInputLocation(inId, firstLocation);
        
        // LOT 추적용 세션 설정
        HttpSession session = SessionUtil.getSession();
        session.setAttribute("targetIdValue", inId);
        
        log.info("완제품 {} 입고완료 및 LOT 부여: {}", productId, inId);
    }
    
    // 완제품 배치의 모든 항목 입고완료 처리
    @Transactional
    public void completeProductBatch(String batchId, String empId) {
        List<Map<String, Object>> inputs = wareMapper.selectInputListByBatch(batchId);
        
        for(Map<String, Object> input : inputs) {
            String inId = (String) input.get("inId");
            String inStatus = (String) input.get("inStatus");
            
            if("입고대기".equals(inStatus)) {
                completeProductInput(inId, empId);
            }
        }
        
        log.info("배치 {} 전체 입고완료 처리", batchId);
    }
    
	// ==================== 출고 관리 ====================
	
    // 출고 목록 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOutputList(String outType, String outStatus, String startDate, String endDate) {
        return wareMapper.selectOutputList(outType, outStatus, startDate, endDate);
    }

    // 재고가 포함된 자재 목록 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMaterialsWithStock() {
        return wareMapper.selectMaterialsWithStock();
    }

    // 재고가 포함된 완제품 목록 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductsWithStock() {
        return wareMapper.selectProductsWithStock();
    }
    // Material manage_id별 재고 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMaterialStockByManageId(String materialId) {
        return wareMapper.getMaterialStockGroupByManageId(materialId);
    }
    
    // 배치 출고 등록 (통합 버전)
    @Transactional
    public String addOutputBatch(List<Map<String, Object>> items, String empId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        Integer batchCount = wareMapper.getTodayOutputBatchCount(today);
        if(batchCount == null) batchCount = 0;
        
        String batchId = "MOB" + today + String.format("%03d", batchCount + 1);
        
        // 오늘 전체 출고 건수 조회
        Integer todayTotalCount = wareMapper.getTodayOutputCount(today);
        if(todayTotalCount == null) todayTotalCount = 0;
        
        for(Map<String, Object> item : items) {
            String manageId = (String) item.get("manageId");
            String materialId = (String) item.get("materialId");
            
            // 단순 일련번호 증가
            todayTotalCount++;
            String outId = "OUT" + today + String.format("%04d", todayTotalCount);
            
            log.info("생성된 outId: {}", outId);
            
            item.put("outId", outId);
            item.put("materialId", materialId);
            item.put("productId", null);
            item.put("batchId", batchId);
            item.put("empId", empId);
            item.put("outType", "출고대기");
            
            wareMapper.insertOutput(item);
        }
        
        return batchId;
    }
    
    // out_id 생성 메서드
    private String generateOutId(String manageId, String today) {
        Integer seq = wareMapper.getOutputSeqByManageId(manageId, today);
        if(seq == null) seq = 0;
        
        // manage_id에서 고유 부분 추출
        String[] parts = manageId.split("_");
        String shortCode = "";
        
        if(parts.length >= 3) {
            // 제품코드 뒷 3자리 + 위치코드 뒷 2자리
            String matPart = parts[1].length() > 3 ? 
                parts[1].substring(parts[1].length() - 3) : parts[1];
            String locPart = parts[2].length() > 2 ? 
                parts[2].substring(parts[2].length() - 2) : parts[2];
            shortCode = matPart + locPart;
        } else {
            shortCode = manageId.length() > 5 ? 
                manageId.substring(manageId.length() - 5) : manageId;
        }
        
        return "OUT" + today + shortCode + String.format("%02d", seq + 1);
    }
    
    // 기존 addOutputBatch는 그대로 두고, 생산계획 전용 메서드 추가
    @Transactional
    public String addProductionMaterialOutputBatch(String planId, List<Map<String, Object>> items, String empId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        Integer batchCount = wareMapper.getTodayOutputBatchCount(today);
        if(batchCount == null) batchCount = 0;
        
        String batchId = "PMOB" + today + String.format("%03d", batchCount + 1);
        
        Integer todayTotalCount = wareMapper.getTodayOutputCount(today);
        if(todayTotalCount == null) todayTotalCount = 0;
        
        for(Map<String, Object> item : items) {
            todayTotalCount++;
            String outId = "POUT" + today + String.format("%04d", todayTotalCount);
            
            log.info("생산계획 자재 출고 등록: {}", outId);
            
            item.put("outId", outId);
            item.put("batchId", batchId);
            item.put("empId", empId);
            item.put("outType", "출고대기");
            item.put("planId", planId);  // 생산계획 ID 추가
            
            wareMapper.insertOutput(item);
        }
        
        return batchId;
    }
    
    // 해당 품목의 재고가 있는 창고 정보(warehouseId, manageId) 조회
    private Map<String, Object> findAvailableWarehouseWithManage(String productId) {
        List<Map<String, Object>> warehouses = wareMapper.getWarehousesWithStock(productId);
        if(warehouses.isEmpty()) {
            throw new RuntimeException("재고가 있는 창고를 찾을 수 없습니다.");
        }
        
        String warehouseId = (String) warehouses.get(0).get("warehouseId");
        String manageId = wareMapper.getManageIdByWarehouse(productId, warehouseId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("warehouseId", warehouseId);
        result.put("manageId", manageId);
        
        return result;
    }

    // 출고 완료 처리
    @Transactional
//    @TrackLot(tableName = "output", pkColumnName = "out_id")
    public void completeOutput(String outId, String empId) {
        Map<String, Object> output = wareMapper.selectOutputById(outId);
        
        if(output == null) {
            throw new RuntimeException("출고 정보를 찾을 수 없습니다.");
        }
        
        log.info("출고 정보: {}", output);
        
        String materialId = (String) output.get("MATERIAL_ID");
        String productId = (String) output.get("PRODUCT_ID");
        
        // work_order_id 먼저 가져오기
        Integer workOrderId = output.get("WORK_ORDER_ID") != null ? 
            ((Number) output.get("WORK_ORDER_ID")).intValue() : null;
        
        if(materialId == null && productId == null) {
            throw new RuntimeException("출고할 품목 정보가 없습니다.");
        }
        
        String warehouseId = (String) output.get("WAREHOUSE_ID");
        String locationId = (String) output.get("LOCATION_ID");
        Integer outCount = ((Number) output.get("OUT_COUNT")).intValue();
        
        // materialId가 있는 경우만 처리 (부품 출고)
        if(materialId != null) {
            log.info("Material 출고: materialId={}, warehouseId={}, locationId={}, qty={}", 
                     materialId, warehouseId, locationId, outCount);
            
            wareMapper.reduceMaterialWarehouseStock(materialId, warehouseId, locationId, outCount);
            wareMapper.reduceMaterialQuantity(materialId, outCount);
        } else if(productId != null) {
            // Product 처리
            log.info("Product 출고: productId={}, warehouseId={}, locationId={}, qty={}", 
                     productId, warehouseId, locationId, outCount);
            // product 처리 로직
        }
        
        // 출고 상태 변경
        wareMapper.updateOutputType(outId, "출고완료");
        
        // 세션 설정
        HttpSession session = SessionUtil.getSession();
//        session.setAttribute("targetIdValue", outId);
        
        // work_order_id도 세션에 저장 (자재 출고인 경우에만 의미있음)
        if(workOrderId != null) {
            session.setAttribute("workOrderId", workOrderId);
            log.info("work_order_id {} 세션 저장", workOrderId);
        }
    }

    // Product 재고 차감 (warehouse_item 차감 후 product 차감)
    private void reduceProductStock(String productId, String warehouseId, Integer qty) {
        List<Map<String, Object>> locations = wareMapper.getProductStockLocations(productId, warehouseId);
        
        int remaining = qty;
        for(Map<String, Object> loc : locations) {
            if(remaining <= 0) break;
            
            String locationId = (String) loc.get("locationId");
            int currentQty = ((Number) loc.get("itemAmount")).intValue();
            
            int reduceQty = Math.min(remaining, currentQty);
            wareMapper.reduceWarehouseItemStock(productId, warehouseId, locationId, reduceQty);
            
            remaining -= reduceQty;
        }
        
        wareMapper.reduceProductQuantity(productId, qty);
    }

    // Material 재고 차감 (warehouse_item 차감 후 material 차감)
    private void reduceMaterialStock(String materialId, String warehouseId, Integer qty) {
        List<Map<String, Object>> locations = wareMapper.getMaterialStockLocations(materialId, warehouseId);
        
        int remaining = qty;
        for(Map<String, Object> loc : locations) {
            if(remaining <= 0) break;
            
            String locationId = (String) loc.get("locationId");
            int currentQty = ((Number) loc.get("itemAmount")).intValue();
            
            int reduceQty = Math.min(remaining, currentQty);
            wareMapper.reduceMaterialWarehouseStock(materialId, warehouseId, locationId, reduceQty);
            
            remaining -= reduceQty;
        }
        
        wareMapper.reduceMaterialQuantity(materialId, qty);
    }

    // 출고 취소
    @Transactional
    public void cancelOutput(String outId) {
        wareMapper.deleteOutput(outId);
    }
    
    // 배치별 출고 목록 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOutputListByBatch(String batchId) {
        return wareMapper.selectOutputListByBatch(batchId);
    }

    // 날짜별 배치 목록 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOutputBatches(String date, String outType) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", date);
        params.put("outType", outType);
        params.put("outStatus", "출고대기"); 
        return wareMapper.selectOutputBatches(params);
    }
    
    // Material 재고 차감 (warehouse_item 기반)
    @Transactional
    public boolean reduceMtlStock(String materialId, String warehouseId, 
            String locationId, Integer reduceQty, String reason, String empId) {
        
        // 전체 창고에서 차감
        List<Map<String, Object>> allLocations = wareMapper.getAllMaterialLocations(materialId);
        
        if(allLocations.isEmpty()) {
            throw new RuntimeException("재고가 없습니다.");
        }
        
        int remaining = reduceQty;
        String firstWarehouseId = null;
        String firstManageId = null;  
        
        for(Map<String, Object> loc : allLocations) {
            if(remaining <= 0) break;
            
            String whId = (String) loc.get("warehouseId");
            String locId = (String) loc.get("locationId");
            String manageId = (String) loc.get("manageId"); 
            int currentQty = ((Number) loc.get("itemAmount")).intValue();
            
            if(firstWarehouseId == null) {
                firstWarehouseId = whId;
                firstManageId = manageId; 
            }
            
            int reduceAmt = Math.min(remaining, currentQty);
            
            if(reduceAmt == currentQty) {
                wareMapper.deleteMtlStock(materialId, whId, locId);
            } else {
                wareMapper.updateMtlStock(materialId, whId, locId, currentQty - reduceAmt);
            }
            
            remaining -= reduceAmt;
        }
        
        if(remaining > 0) {
            throw new RuntimeException("재고 부족");
        }
        
        // material 테이블 동기화
        wareMapper.syncMaterialQty(materialId);
        
        // 출고 기록 생성
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        Integer count = wareMapper.getTodayOutputCount(today);
        if(count == null) count = 0;
        
        String outId = "OUT" + today + String.format("%04d", count + 1);
        
        Map<String, Object> params = new HashMap<>();
        params.put("outId", outId);
        params.put("materialId", materialId);
        params.put("warehouseId", firstWarehouseId);
        params.put("locationId", "AUTO");
        params.put("manageId", firstManageId);
        params.put("outCount", reduceQty);
        params.put("outType", "출고완료");
        params.put("empId", empId);
        params.put("batchId", "MI" + today + String.format("%03d", count + 1));
        params.put("outRemark", reason);
        
        wareMapper.insertOutput(params);
        
        return true;
    }
    
    // 출고 내역 삭제
    @Transactional
    public int deleteOutputs(List<String> outIds) {
        int count = 0;
        for(String outId : outIds) {
            wareMapper.deleteOutput(outId);
            count++;
        }
        log.info("출고 내역 삭제: {}건", count);
        return count;
    }
    
    // 0926 수주 대기 목록 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPendingOrders() {
        return wareMapper.selectPendingOrders();
    }

    // 수주 상세 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOrderDetails(String orderId) {
        return wareMapper.selectOrderDetails(orderId);
    }

    // 제품 전체 재고
    @Transactional(readOnly = true)
    public Integer getProductTotalStock(String productId) {
        Integer stock = wareMapper.getProductTotalStock(productId);
        return stock != null ? stock : 0;
    }

    // 완제품 배치 출고 등록
    @Transactional
    public String addProductOutputBatch(List<Map<String, Object>> items, String empId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        Integer batchCount = wareMapper.getTodayProductOutputBatchCount(today);
        if(batchCount == null) batchCount = 0;
        String batchId = "POB" + today + String.format("%03d", batchCount + 1);
        
        // POUT250926으로 시작하는 최대값 조회
        String prefix = "POUT" + today;
        Integer maxCount = wareMapper.getMaxOutputCount(prefix);
        if(maxCount == null) maxCount = 0;
        
        for(Map<String, Object> item : items) {
            maxCount++;  // 1부터 시작했으니 2, 3, 4...
            String outId = prefix + String.format("%04d", maxCount);
            
            item.put("outId", outId);
            item.put("empId", empId);
            item.put("batchId", batchId);
            item.put("outType", "출고대기");
            
            log.info("출고 등록 시도: {}", outId);
            
            try {
                wareMapper.insertOutput(item);
            } catch(Exception e) {
                log.error("출고 등록 실패 - outId: {}", outId, e);
                throw e;
            }
        }
        
        return batchId;
    }
    
    @Transactional
    public String addProductionOutputBatch(String planId, List<Map<String, Object>> items, String empId) {
    	log.info("=== 생산계획 출고 시작 - planId: [{}] ===", planId);
    	String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        Integer batchCount = wareMapper.getTodayOutputBatchCount(today);
        if(batchCount == null) batchCount = 0;
        
        String batchId = "MOB" + today + String.format("%03d", batchCount + 1);
        
        Integer todayTotalCount = wareMapper.getTodayOutputCount(today);
        if(todayTotalCount == null) todayTotalCount = 0;
        
        // work_order_id 조회 추가
        Map<String, Object> workOrder = wareMapper.getUnstartedWorkOrder(planId);
        log.info("조회된 workOrder: {}", workOrder);
        Integer workOrderId = null;
        if(workOrder != null && workOrder.get("workOrderId") != null) {
            workOrderId = ((Number) workOrder.get("workOrderId")).intValue();
            log.info("plan_id {}의 미착수 work_order_id: {}", planId, workOrderId);
        }
        
        for(Map<String, Object> item : items) {
            todayTotalCount++;
            String outId = "OUT" + today + String.format("%04d", todayTotalCount);
            
            log.info("생산계획 자재 출고 등록: {}", outId);
            
            item.put("outId", outId);
            item.put("batchId", batchId);
            item.put("empId", empId);
            item.put("outType", "출고대기");
            item.put("planId", planId);
            
            if(workOrderId != null) {
                item.put("workOrderId", workOrderId); 
            }
            
            wareMapper.insertOutput(item);
        }
        
        return batchId;
    }
    
    @Transactional
    @TrackLot(tableName = "output", pkColumnName = "out_id")
    public void completeProductOutput(String outId, String empId) {
        Map<String, Object> output = wareMapper.selectOutputById(outId);
        
        if(output == null) {
            throw new RuntimeException("출고 정보를 찾을 수 없습니다.");
        }
        
        String productId = (String) output.get("PRODUCT_ID");
        String warehouseId = (String) output.get("WAREHOUSE_ID");
        Integer outCount = ((Number) output.get("OUT_COUNT")).intValue();
        
        // 재고 차감
        reduceProductStock(productId, warehouseId, outCount);
        
        // 상태를 출고완료로 변경
        wareMapper.updateOutputType(outId, "출고완료");
        
        // LOT 추적용 세션
        HttpSession session = SessionUtil.getSession();
        session.setAttribute("targetIdValue", outId);
    }

    // 수주 출하 상태 업데이트
    private void updateOrderShipmentStatus(String orderId, String productId, Integer shippedQty) {
        wareMapper.updateOrderDetailShipped(orderId, productId, shippedQty);
        
        // 모든 상세가 완료되었는지 확인
        List<Map<String, Object>> details = wareMapper.selectOrderDetails(orderId);
        boolean allComplete = details.stream()
            .allMatch(d -> d.get("orderQty").equals(d.get("shippedQty")));
        
        if(allComplete) {
            wareMapper.updateOrderStatus(orderId, "completion");
        }
    }
    
    // 완제품 manage_id별 재고 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductStockByManageId(String productId) {
        return wareMapper.getProductStockGroupByManageId(productId);
    }
    
    // 생산계획 목록 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPendingProductPlans() {
        return wareMapper.selectPendingProductPlans();
    }

    // 생산계획별 BOM 상세 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPlanBOMDetails(String planId) {
        return wareMapper.selectPlanBOMDetails(planId);
    }
    // ==================== 데이터 조회 ====================

    // 부품 목록 조회 (구버전)
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPartsList() {
        return wareMapper.selectPartsList();
    }
    
    // 입고 가능한 Material 목록 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMaterialsForInput() {
        return wareMapper.selectMaterialsForInput();
    }

    // 거래처 목록 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getClientsList() {
        return wareMapper.selectClientsList();
    }
    
    // 완제품 목록 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductsForInput() {
        return wareMapper.selectProductsForInput();
    }
    
    //====================================================================
 // 창고 정보 조회
    @Transactional(readOnly = true)
    public WarehouseDTO getWarehouseInfo(String warehouseId) {
        return wareMapper.selectWarehouseInfoById(warehouseId);
    }

    // 창고 재고 레이아웃 조회
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getWarehouseStockLayout(String warehouseId) {
        return wareMapper.selectWarehouseStockLayout(warehouseId);
    }

    // 창고 요약 정보
    @Transactional(readOnly = true)
    public Map<String, Object> getWarehouseSummary(String warehouseId) {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("emptyLocations", wareMapper.getEmptyLocationCnt(warehouseId));
        
        summary.put("totalLocations", wareMapper.getTotalLocations(warehouseId));
        summary.put("usedLocations", wareMapper.getUsedLocations(warehouseId));
        summary.put("totalStock", wareMapper.getTotalStockInWarehouse(warehouseId));
        
        return summary;
    }

    // 위치별 상세 정보
    @Transactional(readOnly = true)
    public Map<String, Object> getLocationDetail(String locationId) {
        return wareMapper.selectLocationDetail(locationId);
    }
}