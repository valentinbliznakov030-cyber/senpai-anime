FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY senpai-common/pom.xml /common/pom.xml
COPY senpai-common/src /common/src
WORKDIR /common
RUN mvn clean install -DskipTests

WORKDIR /app
COPY senpai-anime/senpai-anime/pom.xml .
RUN mvn dependency:go-offline -B

COPY senpai-anime/senpai-anime/src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jre
WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    nodejs \
    npm \
    ffmpeg \
    chromium \
    wget \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

COPY senpai-anime/senpai-anime/m3u8fetcher.js /app/m3u8fetcher.js
COPY senpai-anime/senpai-anime/package.json /app/package.json
COPY senpai-anime/senpai-anime/package-lock.json /app/package-lock.json

RUN npm install


RUN wget -q https://github.com/nilaoda/N_m3u8DL-RE/releases/latest/download/linux-x64.tar.gz -O /tmp/nm3u8dl.tar.gz && \
    tar -xzf /tmp/nm3u8dl.tar.gz -C /tmp && \
    find /tmp -name "N_m3u8DL-RE" -type f -exec mv {} /app/N_m3u8DL-RE \; && \
    chmod +x /app/N_m3u8DL-RE && \
    rm -rf /tmp/nm3u8dl.tar.gz /tmp/N_m3u8DL-RE* || true

RUN mkdir -p /app/videos /app/subtitles

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]

