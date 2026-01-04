# 10-trillion-dollars

# 배포 업데이트
온프레미스(Self-Hosted Linux Server) 환경으로 인프라를 마이그레이션했습니다.
[바로가기](https://msa.thunderdev.site/)

### 데모 계정 안내
테스트 및 기능 확인을 위한 계정입니다.
~~~
- 판매자 계정
    - ID: seller@sell.com
    - PW: testtest

- 일반 사용자 계정
    - ID: test@test.com
    - PW: testtest
~~~
## Infrastructure:
- 서버/OS: Ubuntu 22.04.5 LTS (온프레미스/자체 구축 서버)
- 가상화: Docker, Docker Compose (컨테이너 기반 환경 격리)
- 네트워크/보안: Cloudflare Tunnel (포트 포워딩 없는 제로 트러스트 보안 적용)
- 버전 관리: GitHub
- 배포 방식: SSH 원격 접속을 통한 컨테이너 오케스트레이션 및 관리

## Local Dev
- 최초 1회: `cp .env.example .env` 후 값 세팅
- 실행: `docker compose up -d --build` → `http://localhost:8080`
- 프론트 빠른 반영(빌드 없이): `docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build`
  - 이후 `dollar-front/src/main/resources/static` 수정 후 새로고침만 하면 반영됩니다.

## Troubleshooting
- `/api/*` 호출이 `502`면 Nginx가 백엔드 컨테이너(upstream)에 연결을 못 하는 상태입니다. 서버에서 `docker compose ps` / `docker compose logs -f dollar-user`로 먼저 확인하세요.

## Monitoring (Loki Stack)
- 구성: Grafana + Loki + Promtail (ELK 대비 경량)
- 실행(서버/로컬): `docker compose -p monitoring -f docker-compose.monitoring.yml up -d`
  - Grafana는 `127.0.0.1:3000`에만 바인딩되어 외부에서 직접 접근 불가
  - `-p monitoring`을 사용해야 앱 배포(`--remove-orphans`) 시 함께 삭제되지 않습니다.
- 서버가 GUI 없는 경우(추천): 맥에서 SSH 터널
  - `ssh -L 3000:127.0.0.1:3000 ubuntu@<mini-pc-ip>`
  - 브라우저: `http://localhost:3000` (기본 계정 `admin` / 비번 `admin`, 필요 시 `GRAFANA_ADMIN_PASSWORD`로 변경)
- 로그 확인: Grafana → Explore → Loki → 예) `{compose_service="dollar-user"}`



## 유저 트래픽 아키텍처
<img width="1100" height="757" alt="Image" src="https://github.com/user-attachments/assets/b1c6ed17-cc08-43f2-af6d-6cc2b004b492" />

## 운영 아키텍처 다이어그램
<img width="1130" height="388" alt="Image" src="https://github.com/user-attachments/assets/220c7b3c-c21e-4c34-8593-6a7508540bb5" />

### 변경점
- Route 53 → Cloudflare DNS/Proxy(+ cloudflared 터널)
- API Gateway/ALB → Nginx 리버스 프록시(호스트 8080)
- S3 → 로컬 업로드 디렉터리(/uploads)
- ElastiCache → Redis 컨테이너
- RDS → MySQL 컨테이너
- ELK 미사용(표준 출력 + Nginx 로그)
- 이메일(SES) 비활성화 또는 SMTP 대체
- 카카오 결제 테스트 모듈 업데이트



### 배포 업데이트 이유
팀 프로젝트 당시에는 기능 구현과 MSA 구조 학습에 집중하느라, 운영 관점은 충분히 설계하지 못했습니다. 이후 AWS 환경에서 실제로 서비스를 굴려보니 트래픽이 크지 않아도 고정 비용이 빠르게 늘었고, 결국 배포를 중단하게 됐습니다.

취업을 위해 AWS 사용법은 익혔지만, 정작 클라우드의 편리함 뒤에 숨겨진 서버의 트래픽 처리 과정과 비용 발생 구조에 대해서는 깊이 있게 고민하지 못했던 것 같습니다.

물론 클라우드 경험도 정말 중요하지만, 왜 클라우드를 사용하는지를 온전히 이해하려면 직접 맨땅에 배포해보는 경험이 선행되어야 한다는 생각이 들었습니다. 그래서 이번 리팩토링을 통해 클라우드 의존도를 낮추고 온프레미스 환경에서 스스로 통제 가능한 견고한 배포 환경을 다시 구축했습니다.
