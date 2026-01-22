<h1 align="center"> ERP · MES 통합 업무 시스템 </h1>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-007396?logo=openjdk" />
  <img src="https://img.shields.io/badge/SpringBoot-3.2-success?logo=springboot" />
  <img src="https://img.shields.io/badge/Oracle-XE-orange?logo=oracle" />
  <img src="https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker" />
  <img src="https://img.shields.io/badge/AWS-EC2-FF9900?logo=amazonaws" />
  <img src="https://img.shields.io/badge/CI/CD-GitHub Actions-blue?logo=githubactions" />
  <img src="https://img.shields.io/badge/Reverse Proxy-Nginx-009639?logo=nginx" />
</p>

<p align="center">
  <b>ERP/MES 도메인을 기반으로 설계부터 운영까지 경험한 백엔드 개발 프로젝트</b><br/>
  팀 프로젝트 이후 전체 시스템을 단독으로 고도화·배포·운영하며 핵심 모듈 설계와 실서비스 인프라 구축을 담당했습니다.
</p>

<hr/>

<h2>프로젝트 개요</h2>
<ul>
  <li>제조 현장의 <b>문서·전자결재·공정·생산·재고</b>를 통합 관리하는 Spring Boot 기반 ERP · MES 웹 시스템</li>
  <li><b>팀 프로젝트(2025.08 ~ 2025.09)</b> 이후 전체 시스템을 <b>단독 고도화·배포·운영</b></li>
  <li>AWS 기반 <b>실서비스 운영 중</b></li>
</ul>

<h2>Links</h2>
<ul>
  <li><b>서비스:</b> <a href="https://rladntjd85.site" target="_blank" rel="noopener noreferrer">https://rladntjd85.site</a></li>
  <li><b>발표자료(PPT):</b> <a href="https://docs.google.com/presentation/d/1qDlwXMYiBPprzpUOIGZ-u-aldkS5UgSG/edit" target="_blank" rel="noopener noreferrer">Google Slides</a></li>
  <li><b>요구사항·테이블 설계:</b> <a href="https://docs.google.com/spreadsheets/d/1Yc7EdMWPktm3QDcTg-RSB7pWTYimPJ7k/edit" target="_blank" rel="noopener noreferrer">Google Sheets</a></li>
</ul>
<hr/>

<h2>핵심 기여 요약</h2>
<ul>
  <li>전자결재 시스템 <b>전체 설계 및 핵심 기능 개발</b></li>
  <li>MES <b>LOT(생산이력)</b> 핵심 로직 구현</li>
  <li>전자결재·문서·MES 영역 <b>DB 구조 설계</b> 및 성능 개선</li>
  <li><b>Docker·AWS 기반 실서비스 인프라</b> 단독 구축 및 운영</li>
</ul>

<hr/>

<h2>주요 담당 영역</h2>

<h3>전자결재 시스템</h3>
<ul>
  <li>전자결재 <b>전체 플로우 및 DB 구조 100% 설계</b>
    <ul>
      <li>작성 → 결재중 → 승인/반려 흐름 설계</li>
      <li>결재선·권한·이력 테이블 설계</li>
    </ul>
  </li>
  <li>결재 <b>상신(제출)</b> 기능 직접 구현</li>
  <li>승인/반려 로직 구조 설계 참여</li>
</ul>

<h3>MES LOT(생산이력)</h3>
<ul>
  <li>공정별 LOT 자동 생성 로직 설계 및 구현</li>
  <li>생산지시 → 공정 → 생산결과 전체 흐름 개발</li>
  <li>LOT 기반 이력 추적(Tracking) 기능 구현</li>
  <li>Oracle XE 조인 구조 개선으로 조회 성능 향상</li>
</ul>

<h3>공통문서 관리</h3>
<ul>
  <li>공통 문서 CRUD 기능 개발</li>
  <li>Docker 환경 파일 경로/볼륨 매핑 이슈 해결</li>
</ul>

<hr/>

<h2>프로젝트 종료 후 개인 고도화</h2>
<ul>
  <li>MyBatis + JPA 혼용 구조 안정화</li>
  <li>불필요 SQL 제거 및 성능 개선</li>
  <li>서비스 모듈 구조 정리 → 유지보수성 향상</li>
  <li>Docker 리소스/메모리 이슈 해결</li>
</ul>

<hr/>


<h2>인프라 · DevOps (단독 구축)</h2>
<ul>
  <li>Docker 기반 Spring Boot / Oracle XE / Nginx 컨테이너 구성</li>
  <li>Nginx Reverse Proxy → Spring Boot 연동</li>
  <li>AWS EC2(Ubuntu) 실서비스 운영</li>
  <li>Cloudflare DNS Proxy + HTTPS 적용</li>
  <li>GitHub Actions + Self-hosted Runner 기반 CI/CD 구성
    <ul>
      <li>main push → 자동 빌드/배포</li>
      <li>배포 시간 약 <b>70% 단축</b></li>
    </ul>
  </li>
</ul>

<hr/>

<h2>기술 스택</h2>
<ul>
  <li><b>Backend:</b> Java 21, Spring Boot 3, Spring Security, JPA, MyBatis</li>
  <li><b>Database:</b> Oracle XE (Docker)</li>
  <li><b>Infra/DevOps:</b> Docker, Docker Compose, AWS EC2, Nginx, Cloudflare, GitHub Actions</li>
  <li><b>Frontend:</b> Thymeleaf, HTML/CSS/JS, Bootstrap, jQuery</li>
</ul>

<hr/>

<h2>인프라 구조</h2>
<pre>
Client
  ↓ (TLS 1.3)
Cloudflare (DNS Proxy / WAF)
  ↓
Nginx (Reverse Proxy)
  ↓
Spring Boot (Docker)
  ↓
Oracle XE (Docker)
</pre>

<hr/>

<h2>한 줄 정리</h2>
<p>
  <b>
    ERP/MES 도메인을 기반으로 설계·개발·배포·운영까지 수행한 백엔드 개발 프로젝트로,
    LOT 관리를 통해 생산·공정·결과 등 시스템 전반의 흐름을 이해하고
    전자결재를 포함한 핵심 모듈의 아키텍처와 DB 구조를 설계·구현했습니다.
  </b>
</p>
