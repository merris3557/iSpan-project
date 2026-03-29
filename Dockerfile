# --- 第一階段：前端打包 (Vite) ---
FROM node:20-slim AS frontend-build
WORKDIR /app/frontend
# 💡 注意路徑：從根目錄進入 front-end
COPY front-end/package*.json ./
RUN npm install
COPY front-end/ .
RUN npm run build

# --- 第二階段：後端打包 (Maven) ---
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
# 💡 注意路徑：從根目錄進入 back-end
COPY back-end/pom.xml .
COPY back-end/src ./src
# 將前端產出的 dist 複製到後端的靜態資源目錄
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN mvn clean package -DskipTests

# --- 第三階段：運行 ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx400m", "-jar", "app.jar"]