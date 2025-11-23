# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy common module first and install it
COPY senpai-common/pom.xml /common/pom.xml
COPY senpai-common/src /common/src
WORKDIR /common
RUN mvn clean install -DskipTests

# Copy project files
WORKDIR /app
COPY senpai-anime/senpai-anime/pom.xml .
RUN mvn dependency:go-offline -B

COPY senpai-anime/senpai-anime/src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
# Using Debian-based image (not Alpine) because N_m3u8DL-RE requires glibc
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install Node.js, npm, ffmpeg and dependencies (needed for m3u8fetcher.js with Puppeteer and N_m3u8DL-RE)
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    nodejs \
    npm \
    ffmpeg \
    chromium \
    wget \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

# Copy m3u8fetcher.js and package.json for Puppeteer
COPY senpai-anime/senpai-anime/m3u8fetcher.js /app/m3u8fetcher.js
COPY senpai-anime/senpai-anime/package.json /app/package.json
COPY senpai-anime/senpai-anime/package-lock.json /app/package-lock.json

# Install Node.js dependencies (Puppeteer)
RUN npm install

# Download and install N_m3u8DL-RE Linux version
# Using the latest release from GitHub (linux-x64.tar.gz)
# The file is downloaded from: https://github.com/nilaoda/N_m3u8DL-RE/releases/latest
RUN wget -q https://github.com/nilaoda/N_m3u8DL-RE/releases/latest/download/linux-x64.tar.gz -O /tmp/nm3u8dl.tar.gz && \
    tar -xzf /tmp/nm3u8dl.tar.gz -C /tmp && \
    find /tmp -name "N_m3u8DL-RE" -type f -exec mv {} /app/N_m3u8DL-RE \; && \
    chmod +x /app/N_m3u8DL-RE && \
    rm -rf /tmp/nm3u8dl.tar.gz /tmp/N_m3u8DL-RE* || true

# Create directories for videos and subtitles
RUN mkdir -p /app/videos /app/subtitles

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]

