[![Build Status](https://travis-ci.com/nmrhtn7898/bread-project.svg?branch=master)](https://travis-ci.com/nmrhtn7898/bread-project)
[![Coverage Status](https://coveralls.io/repos/github/nmrhtn7898/bread-project/badge.svg)](https://coveralls.io/github/nmrhtn7898/bread-project)
# Bread Project
##### 애플리케이션 실행(로컬 환경)
- 애플리케이션 실행에 필요한 Redis, Mysql, Nginx 도커 컨테이너로 실행하기 위해 
docker-compose 명령어 실행
- docker-compose -f docker/default/docker-compose.yml up -d
- 컨테이너 실행 후, 애플리케이션 실행 및 디버깅
- asciidoctor encoding 관련 에러 발생 시 => 환경 변수 설정 GRADLE_OPTS=-Dfile.encoding=UTF-8

