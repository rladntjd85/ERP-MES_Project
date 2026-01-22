<h1 align="center">ERP · MES 통합 업무 시스템 (NOVA)</h1> <p align="center"> <img src="https://img.shields.io/badge/Java-21-007396?logo=openjdk" /> <img src="https://img.shields.io/badge/SpringBoot-3.2-success?logo=springboot" /> <img src="https://img.shields.io/badge/Oracle-XE-orange?logo=oracle" /> <img src="https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker" /> <img src="https://img.shields.io/badge/AWS-EC2-FF9900?logo=amazonaws" /> <img src="https://img.shields.io/badge/CI/CD-GitHub Actions-blue?logo=githubactions" /> <img src="https://img.shields.io/badge/Reverse Proxy-Nginx-009639?logo=nginx" /> </p>
프로젝트 개요

제조 현장의 문서·결재·공정·생산·재고 관리를 통합 제공하기 위해
Spring Boot 기반으로 개발된 ERP · MES 통합 웹 시스템입니다.

프로젝트는 팀 프로젝트(2025.08.04 ~ 2025.09.30)였으며,
이후 전체 기능 고도화·배포·보안·DevOps 영역을 단독으로 개선하여
AWS 기반 실서비스 환경으로 운영 중입니다.

Links

서비스 URL: https://rladntjd85.site

GitHub Repo: https://github.com/rladntjd85/ERP-MES_Project

발표(PPT): https://docs.google.com/presentation/d/1qDlwXMYiBPprzpUOIGZ-u-aldkS5UgSG/edit

요구사항·테이블 설계:
https://docs.google.com/spreadsheets/d/1Yc7EdMWPktm3QDcTg-RSB7pWTYimPJ7k/edit

기여 요약

본 프로젝트의 핵심 모듈 중

전자결재

공통문서 관리

LOT(생산이력)

DB 전체 설계(전자결재·문서·MES 테이블)

초기 UI(부트스트랩 → 타임리프 템플릿) 구조 설계

는 직접 개발 및 설계를 담당했습니다.

① 팀 프로젝트 당시 개인 기여
전자결재 시스템 (개발 30% / 설계·DB 100%)

결재 상신(제출) 기능 개발

전자결재 전체 플로우 설계
(작성 → 결재중 → 반려/승인 흐름)

결재 라우팅 구조(결재선, 실행 순서) 설계

결재·이력 테이블 등 DB 전체 구조 설계

결재 권한 관리 모델 설계

승인/반려 처리 로직 설계 참여

알림/이력 로직 구조 설계 기여

공통문서 관리 시스템 (100% 개인 개발)

문서 CRUD 기능 개발

Docker 파일 경로 문제 해결(컨테이너 경로 매핑)

LOT(생산이력) 시스템 (100% 개인 개발)

공정별 LOT 자동 생성 로직 개발

생산지시 → 공정 → 생산결과 흐름 구현

LOT 기반 이력 조회(Tracking) 기능 개발

Oracle XE 조인 성능 개선

초기 화면 설계 (UI 템플릿 → Thymeleaf 적용)

Bootstrap 템플릿 기반 초기 UI 구조 설계

레이아웃 구조(Header/Sidebar/Content) 구성

Thymeleaf SSR 화면 템플릿 변환

공통 레이아웃/템플릿 단위 분리

② 프로젝트 종료 후 개인 고도화 및 DevOps 구축
백엔드 개선

MyBatis + JPA 혼용 구조 안정화

복잡 SQL 정리

불필요 조회 제거 및 성능 개선

Docker 환경 리소스·권한·경로 문제 해결

서비스 모듈 구조 개선

인프라 및 DevOps (100% 개인 구축)
Docker 기반 인프라

Spring Boot / Oracle XE / Nginx 컨테이너 구성

단일 브릿지 네트워크(mynetwork) 구성

Nginx Reverse Proxy

Oracle–Spring 연결 문제 해결

AWS 운영환경

AWS EC2(Ubuntu) 운영

Docker Compose 자동 배포

Cloudflare DNS Proxy + SSL

Let’s Encrypt + Certbot HTTPS

CloudWatch 비용 알림

CI/CD 자동화

GitHub Actions + Self-hosted Runner 구성

main push 시 자동 빌드·배포

Docker 이미지 재배포 자동화

Secrets 기반 민감 정보 관리

외부 PR workflow 차단

주요 성과

배포 자동화 구축으로 배포 시간 약 70% 감소

Docker 기반 Oracle–Spring 연동 안정화

LOT 기반 생산이력 전체 흐름 구현

공통문서·결재 구조 설계로 ERP 확장 기반 확보

HTTPS 기반 실서비스 운영환경 구축

주요 기능 요약
ERP

전자결재(설계·DB 100% / 상신 기능 직접 개발)

공통문서 관리

사내문서 Approval Flow 구조

MES

LOT 기반 생산이력 관리

공정별 이력·결과 관리

기술 스택
Backend

Java 21 · Spring Boot 3 · Spring Security
JPA · MyBatis · Thymeleaf

Database

Oracle XE (Docker)

Infra / DevOps

Docker · Docker Compose
AWS EC2
Cloudflare DNS/HTTPS
Nginx Reverse Proxy
GitHub Actions (Self-hosted Runner)

Front-end

HTML · CSS · JS
jQuery · Bootstrap · Toast UI

Client
  ↓ (TLS 1.3)
Cloudflare (DNS Proxy / WAF)
  ↓
Nginx (Reverse Proxy)
  ↓
Spring Boot (Docker)
  ↓
Oracle XE (Docker)

Contact

Email: rladntjd850@gmail.com
