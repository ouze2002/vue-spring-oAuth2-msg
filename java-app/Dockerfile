# 1단계: 빌드 단계
# Maven 빌드를 위해 maven 이미지를 사용합니다.
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Maven 캐시를 활용하기 위해 pom.xml을 먼저 복사합니다.
# 이렇게 하면 pom.xml에 변경이 없을 경우 종속성 다운로드를 건너뛰어 빌드 속도를 높일 수 있습니다.
COPY pom.xml .

# 종속성 다운로드
RUN mvn dependency:go-offline

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드: 이 단계에서 테스트를 건너뛰고 JAR 파일을 패키징합니다.
RUN mvn package -DskipTests

# 2단계: 런타임 단계
# 빌드된 JAR 파일을 실행하기 위해 더 가벼운 openjdk 이미지를 사용합니다.
FROM openjdk:21-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 포트 노출
EXPOSE 8080

# 'builder' 단계에서 생성된 JAR 파일을 런타임 이미지로 복사합니다.
# target 디렉토리에서 'spring-boot-maven-plugin'이 생성한 실행 가능한 JAR 파일을 찾습니다.
# jar 파일 이름은 'your-app-name-version.jar'와 같은 형식일 수 있으므로 와일드카드를 사용합니다.
COPY --from=builder /app/target/*.jar /app/app.jar

# 소스 코드를 컨테이너에 복사합니다.
COPY . /usr/src/java-app

# 애플리케이션 실행 명령어
# 이 ENTRYPOINT는 컨테이너의 환경 변수를 안정적으로 전달받습니다.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
