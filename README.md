# ERP · MES 통합 업무 시스템 개발 (개인 프로젝트)

### 2025 — AWS 기반 인프라 · DevOps · 백엔드 개발

제조 현장에서 필요한 **ERP(전자결재·공통문서)** 기능과  
**MES(생산·공정·LOT·재고)** 기능을 통합한 Spring Boot 기반 웹 시스템입니다.

AWS EC2 + Docker 기반 인프라를 구성하고,  
Self-hosted GitHub Actions Runner를 통한 안전한 CI/CD 파이프라인까지 구축했습니다.

---

## 📌 주요 기여 (내가 직접 개발한 기능)

### 🔹 **전자결재 시스템**
- 결재선 설정, 다단계 승인 플로우 구현  
- 결재 문서 상태 관리(작성 → 결재중 → 반려/승인)  
- 알림/이력 관리 및 결재 권한 검증 적용  

### 🔹 **공통문서 관리 시스템**
- 문서 분류/조회/권한 기반 접근 제어  
- 대량 파일 업로드/다운로드 로직 구현  
- Docker 컨테이너 환경에서의 리소스 경로 문제 해결  

### 🔹 **LOT(생산이력) 부여 및 관리**
- 공정별 LOT 자동 생성 로직 개발  
- LOT 기반 이력 추적(공정 → 작업지시 → 생산결과)  
- Oracle XE 기반 쿼리 최적화 및 조인 성능 개선  
- 생산/재고 정보 연동 구조 설계

### 🔹 **고도화·리팩토링 작업**
- MyBatis + JPA 혼용 구조 안정화  
- 쿼리 성능 개선(불필요한 조회 제거 / batch 처리 적용)  
- Thymeleaf 템플릿 경로 문제 해결 (Docker 환경 대응)  
- 레거시 서비스 로직 모듈 분리 및 유지보수성 향상  

---

## 🛠 사용 기술

**Backend**  
- Java 21 · Spring Boot 3 · Spring Security  
- JPA · MyBatis  
- Thymeleaf  

**Database**  
- Oracle XE (Dockerized)  

**DevOps / Infra**  
- AWS EC2 (Ubuntu)  
- Docker / Docker Compose  
- GitHub Actions (Self-hosted Runner)  
- Cloudflare DNS & SSL  
- Nginx Reverse Proxy  
- CloudWatch Billing Alert

---

## ⚙️ 인프라 설계

### 🔹 Docker 기반 Micro Infra
- **spring**, **nginx**, **oracle-xe**  
- 단일 네트워크(mynetwork) 구성으로 안정적 통신 확보  
- nginx → spring 8080 포트 Reverse Proxy

### 🔹 GitHub Actions CI/CD
- EC2에 Self-hosted Runner 설치  
- Build → Docker Image → Compose Deploy 자동화  
- 외부 PR 자동 실행 차단 (보안 강화)

### 🔹 SSL 인증서 & HTTPS 적용
- Let’s Encrypt + Certbot  
- Cloudflare DNS Proxy  
- HTTPS 자동 리다이렉트 설정 완료  

---

## 🔥 주요 성과

- 빌드/배포 자동화 구축 → 배포 속도 **70% 단축**
- Oracle XE + MyBatis 병행 구조 안정화  
- LOT 기반 생산이력 추적 기능 완성  
- SSL + 도메인 배포로 실제 서비스 환경 수준 구현  
- 도커 기반으로 Oracle–Spring 간 통신 문제 해결  

---

## 🌐 Demo

### 🔗 **HTTPS 배포 주소 (Nginx + Domain + SSL 완료)**  
👉 https://rladntjd85.site

(서버 비용 이슈로 간헐적 중단될 수 있음)

### 🔗 **백업 Direct 접속 (EC2:8080)**  
👉 http://13.125.58.201:8080

---

## 👤 Contact  
📧 rladntjd850@gmail.com  
