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
COPY target/paypal-provider-service.jar .

# Document that the application listens on port 8083
EXPOSE 8083

# Start the Spring Boot application when the container starts
ENTRYPOINT ["java", "-jar", "paypal-provider-service.jar"]
