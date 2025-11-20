ERP · MES 통합 업무 시스템 개발 (개인 프로젝트)

2025 — AWS 기반 인프라 및 백엔드 개발

📌 프로젝트 개요

제조 환경에서 필요한 ERP + MES 기능을 통합한 Spring Boot 기반 웹 시스템을 개발했습니다.
AWS EC2·Docker 기반 인프라를 구성하고, EC2 Self-hosted GitHub Actions Runner로 보안 강화 CI/CD 파이프라인을 구축했습니다.

🛠 주요 역할 & 기여도

Spring Boot 기반 ERP(인사·전자결재) + MES(공정·LOT·생산·재고) 전체 API 개발

Spring Security 기반 권한/로그인 구현

Oracle XE(도커) 기반 데이터 모델링 및 쿼리 최적화

AWS EC2 + Docker 환경에서 서비스 배포 및 운영

Self-hosted Runner 기반 CI/CD 구축 → 외부 PR 실행 차단으로 보안 강화

CloudWatch Billing 알람 설정으로 비용 관리 자동화

Docker Network 구성으로 Oracle–Spring 간 안정적인 통신 확보

⚙ 사용 기술

Backend: Java 21, Spring Boot 3, Spring Security, JPA, MyBatis

Database: Oracle XE (Docker)

Infra: AWS EC2(t3.small), Docker, GitHub Actions(Self-hosted Runner), CloudWatch

DevOps: Gradle, Docker Image Build, Secret 관리

🔥 성과

GitHub Actions + Self-hosted Runner로 전체 빌드/배포 자동화 성공

EC2 내부 빌드로 보안성 강화 (외부 PR 자동실행 차단)

Docker 기반 구조로 재배포 속도 약 70% 단축

CloudWatch Billing 알림을 통한 비용 폭주 방지

🧩 문제 해결 경험

Docker 환경에서 Thymeleaf 템플릿 경로 깨짐 → 리소스 경로 정규화로 해결

Oracle ORA-01861(date) 오류 → MyBatis Date 타입/Oracle NLS 설정 조정

Docker 컨테이너 간 통신 오류 → custom network 구성으로 해결

CI/CD 보안 이슈 → 외부 PR workflow 승인 정책 적용, runner 격리

🔗 데모 URL

http://13.125.58.201:8080

👤 GitHub

https://github.com/rladntjd85/ERP-MES_Project
