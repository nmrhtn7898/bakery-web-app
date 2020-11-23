[![Build Status](http://211.104.121.100:8080/buildStatus/icon?job=bread-web-api)](http://211.104.121.100:8080/job/bread-web-api/)
[![Coverage Status](https://coveralls.io/repos/github/nmrhtn7898/bread-project/badge.svg)](https://coveralls.io/github/nmrhtn7898/bread-project)
# Bread Project
### 1. 프로젝트 구성
- 해당 프로젝트는 Gradle 기반 멀티 모듈 프로젝트입니다.
- 프로젝트는 현재 3개의 모듈로 구성되어 있습니다.

|이름|내용|설명|
|---|---|---|
|bread-common-module|공통 모듈|모든 모듈에서 공통으로 사용하는 코드, 유틸, 의존성 제공|
|bread-api-module|api 모듈|토큰 기반의 REST API 서비스 제공|
|bread-auth-module|인증 모듈|토큰 발급, 재발급 및 유효성 검사하고 sso 서비스 제공|

### 2. 애플리케이션 실행(로컬 개발 환경)
- 로컬 환경은 도커 컨테이너에서 데이터베이스(Mysql) 및 캐시(Redis) 사용 또는 설치하여 사용
- Test 환경은 내장형 데이터베이스(H2) 및 캐시(Redis) 사용
- 원활한 디버깅을 위해 애플리케이션은 컨테이너 환경이 아닌 IDE로 실행
(dev, prod 환경에서는 컨테이너 기반)
```
docker-compose -f docker/docker-compose.yml up -d // 애플리케이션 실행에 필요한 컨테이너 실행
```

