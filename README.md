<h1 align="center">✨ ERP · MES 통합 업무 시스템 (NOVA) ✨</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-007396?logo=oracle" />
  <img src="https://img.shields.io/badge/SpringBoot-3.2-success?logo=springboot" />
  <img src="https://img.shields.io/badge/Oracle-XE-orange?logo=oracle" />
  <img src="https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker" />
  <img src="https://img.shields.io/badge/AWS-EC2-FF9900?logo=amazonaws" />
  <img src="https://img.shields.io/badge/CI/CD-GitHub Actions-blue?logo=githubactions" />
  <img src="https://img.shields.io/badge/Reverse Proxy-Nginx-009639?logo=nginx" />
</p>

---

## 🚀 프로젝트 개요
제조 현장의 생산·공정·문서·결재·자재 관리를 하나의 시스템에서 처리하기 위해  
Spring Boot 기반 ERP · MES 모듈을 통합 개발한 시스템입니다.

- **팀 프로젝트 기간:** 2025.08.04 ~ 2025.09.30  
- **프로젝트 종료 후:** 인프라·보안·성능개선·CI/CD 포함한 고도화를 단독 수행

---

## 🔗 Links
- **🌐 서비스 URL:** https://rladntjd85.site  
- **📦 GitHub Repo:** https://github.com/rladntjd85/ERP-MES_Project  
- **📄 발표(PPT):** https://docs.google.com/presentation/d/1qDlwXMYiBPprzpUOIGZ-u-aldkS5UgSG/edit  
- **📊 요구사항·테이블 설계:**  
  https://docs.google.com/spreadsheets/d/1Yc7EdMWPktm3QDcTg-RSB7pWTYimPJ7k/edit

---

# 🧑‍💻 **기여 요약 (핵심 기능 100% 단독 개발)**

### ✔ 전자결재 시스템
- 결재선 설정, 다단계 승인 플로우  
- 문서 상태 흐름 관리(작성→결재중→반려/승인)  
- 결재 권한 검증 + 이력/알림 처리

### ✔ 공통문서 관리 시스템
- 문서 분류/조회/검색  
- 권한 기반 접근 제어  
- 대량 파일 업/다운로드  
- Docker 환경 파일 경로 문제 해결

### ✔ LOT 기반 생산이력(MES)
- 공정별 LOT 자동 생성  
- 공정 → 작업지시 → 생산결과 흐름 구현  
- 조인 성능 개선 + Oracle XE 최적화  
- 재고·생산 연동 구조 설계

---

# 🔧 리팩토링 & 고도화 (개인 작업)

### 🔹 백엔드 구조 개선
- MyBatis + JPA 혼합 구조 안정화  
- 쿼리 최적화 + batch 처리  
- 공통 서비스 모듈 분리 → 유지보수성 향상

### 🔹 DevOps / 인프라 구축
- AWS EC2(Ubuntu) 운영환경 구축  
- Docker Compose 기반 컨테이너 설계  
- Nginx Reverse Proxy + HTTPS(TLS1.3)  
- Cloudflare DNS Proxy + WAF 보안  
- GitHub Actions + Self-hosted Runner 기반 CI/CD 구축  

---

# 🛠 기술 스택

### 🔹 Backend  
Java 21 · Spring Boot 3 · Spring Security  
JPA · MyBatis · Thymeleaf

### 🔹 Database  
Oracle XE (Docker)

### 🔹 DevOps / Infra  
Docker · Docker Compose · GitHub Actions  
AWS EC2 · Nginx · Cloudflare DNS/SSL  

---

# 🏗 인프라 구조
```
Client
  ↓
Cloudflare (DNS Proxy / HTTPS / WAF)
  ↓
Nginx (Reverse Proxy)
  ↓
Spring Boot (Docker)
  ↓
Oracle XE (Docker)
```

---

## 📞 Contact  
📧 rladntjd850@gmail.com
