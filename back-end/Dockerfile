# --- 第一階段：編譯前端 ---
FROM node:18 AS frontend-build
WORKDIR /app/frontend
# 複製前端原始碼
COPY front-end/package*.json ./
RUN npm install
COPY front-end/ .
# 執行打包 (這會產生一個 dist 資料夾)
RUN npm run build

# --- 第二階段：編譯後端 Java ---
FROM maven:3.8-openjdk-17 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# 將第一階段打包好的前端檔案，複製到 Java 的靜態資源路徑
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
# 編譯並跳過測試
RUN mvn clean package -DskipTests

# --- 第三階段：執行環境 ---
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]