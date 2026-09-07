FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN javac -cp ".:mysql-connector-j-26.7.0.jar" src/controlefinanceiroweb/*.java
EXPOSE 8080
CMD ["java", "-cp", ".:src:mysql-connector-j-26.7.0.jar", "controlefinanceiroweb.ControleFinanceiroWeb"]
