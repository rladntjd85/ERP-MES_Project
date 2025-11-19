✔ ERP · MES 통합 시스템

중소 제조 환경에서 사용할 수 있는 ERP + MES 통합 업무 시스템입니다.
AWS EC2, Docker, GitHub Actions 기반 CI/CD 자동배포 파이프라인을 직접 구축했습니다.

🔗 데모: http://13.125.58.201:8080

🚀 주요 기능

ERP: 인사관리, 출퇴근/근태관리, 전자결재, 공지사항

MES: 공정관리, LOT관리, 생산관리, 재고/창고관리

공통: 로그인/권한(Spring Security), 대시보드, Oracle 기반 데이터 처리

🛠 기술 스택

Backend: Java 17, Spring Boot 3, Security, MyBatis/JPA

DB: Oracle 11g XE (Docker)

Infra: AWS EC2(t3.small), Docker, GitHub Actions, CloudWatch Billing 경보

📦 아키텍처 요약
Git Push → GitHub Actions
   - Gradle Build
   - Docker Build → myapp.tar
   - SCP로 EC2 전송
EC2
   - docker load
   - 기존 컨테이너 stop/remove
   - 새 버전 run
사용자 → http://13.125.58.201:8080 접속

🔥 CI/CD 핵심 포인트 (포트폴리오 어필용)

GitHub Actions로 자동 빌드/배포

Docker 이미지 자동 생성 & 교체 배포

환경변수(Oracle URL/계정) Secrets로 보호

EC2 내부에서 docker load·run 자동 실행

CloudWatch Billing 알람으로 비용 폭주 방지

보안그룹 최소 오픈(22, 8080)

🧩 문제 해결 경험
1) 배포 환경에서 Thymeleaf 뷰 깨짐

/경로 → 경로 로 수정

Docker 내부 /tmp/app/BOOT-INF/classes/templates 확인하며 해결

2) Oracle ORA-01861 날짜 오류

MyBatis Date 파라미터 타입 수정

Oracle NLS 설정 문제 조정

3) AWS 비용·보안 관리

CloudWatch Billing 경보

22 / 8080만 허용

t3.small + 20GB로 비용 최적화

👨‍💻 개발자 정보

GitHub: https://github.com/rladntjd85/ERP-MES_Project

Email: rladntjd850@gmail.com
