# Stage 1: Build
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Sao chép file pom.xml của module acv trước để cache dependencies
COPY acv/pom.xml ./acv/
# Để maven tải các dependency trước khi build code thực tế
RUN mvn -f acv/pom.xml dependency:go-offline -B

# Sao chép mã nguồn của module acv
COPY acv/src ./acv/src

# Thực hiện build ứng dụng và bỏ qua chạy test để tiết kiệm thời gian build
RUN mvn -f acv/pom.xml clean package -DskipTests

# Stage 2: Run
FROM openjdk:17-jdk-slim
WORKDIR /app

# Sao chép file jar đã build thành công từ stage 1
COPY --from=build /app/acv/target/*.jar app.jar

# Render tự động cấp port qua biến môi trường PORT, cấu hình Spring Boot chạy trên port này
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
