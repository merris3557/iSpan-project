# 第一階段：使用 Maven 進行編譯
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY . .
# 執行編譯並跳過測試，產生 jar 檔
RUN mvn clean package -DskipTests

# 第二階段：使用輕量級 JRE 執行
FROM openjdk:17-jdk-slim
WORKDIR /app
# 從第一階段複製產出的 jar 檔
COPY --from=build /app/target/*.jar app.jar

# 暴露埠號（Render 預設會偵測 8080）
EXPOSE 8080

# 啟動指令，強制指定 prod 設定
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]