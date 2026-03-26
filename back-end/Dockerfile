# 第一階段：編譯 (使用 maven 搭配 eclipse-temurin)
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 第二階段：執行 (換成 eclipse-temurin 17)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# 複製產出的 jar 檔
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# 啟動指令
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]