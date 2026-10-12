# Base image: Amazon Linux 2023
FROM amazonlinux:2023

# Set /app as the working directory inside the container
WORKDIR /app

# Update packages, install Java 17, and clean package cache
RUN dnf update -y \
   && dnf install -y java-17-amazon-corretto \
   && dnf clean all \
   && rm -rf /var/cache/dnf

# Copy the Spring Boot JAR from host into /app
COPY target/payment-processing-service.jar .

# Document that the application listens on port 8082
EXPOSE 8082

# Start the Spring Boot application when the container starts
ENTRYPOINT ["java", "-jar", "payment-processing-service.jar"]
