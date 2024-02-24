FROM openjdk:17.0.2
EXPOSE 8080:8080
RUN mkdir /app
ENV IS_DOCKER true
COPY --from=builder /build/libs/KubSAU_TestBackend-all.jar /app/ktor-docker-sample.jar
ENTRYPOINT ["java","-jar","/app/ktor-docker-sample.jar"]
