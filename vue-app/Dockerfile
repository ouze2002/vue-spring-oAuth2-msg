# 1. 빌드 스테이지: Vue 앱 빌드
FROM node:20-slim AS build-stage

WORKDIR /usr/src/app

COPY package.json yarn.lock ./

RUN yarn install

COPY . .

# .env 파일을 복사하여 빌드 환경에 포함
# 프로덕션 빌드이므로, .env.production 파일을 복사합니다.
COPY .env.production ./.env

# yarn build 명령어는 package.json에 정의된 빌드 스크립트를 실행합니다.
RUN yarn build

# 2. 프로덕션 스테이지: Nginx 컨테이너 구성
FROM nginx:stable-alpine AS production-stage

# 커스텀 nginx.conf 파일을 컨테이너에 복사합니다.
# 이 파일은 Nginx가 Vue 앱을 올바르게 서빙하고 API 요청을 프록시하도록 설정합니다.
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

# 빌드 스테이지에서 생성된 정적 파일을 Nginx 웹 루트 디렉토리로 복사합니다.
# Vue 앱의 경우 빌드 결과물이 'dist' 폴더에 생성됩니다.
COPY --from=build-stage /usr/src/app/dist /usr/share/nginx/html

# 소스 코드를 컨테이너에 복사합니다.
COPY --from=build-stage /usr/src/app /usr/src/vue-app


# 컨테이너가 80번 포트를 사용함을 외부에 알립니다.
EXPOSE 80

# Nginx 서버를 시작합니다.
CMD ["nginx", "-g", "daemon off;"]