# ------------------ Stage 1: Build ---------------------
# Base maven image that contain JDK 21
FROM maven:3.9-eclipse-temurin-21 AS builder

# Create app dir and go inside it
WORKDIR /app

# Copy pom.xml file into app dir
COPY pom.xml .

# Download all dependencies in pom file
RUN mvn dependency:go-offline -q

# copy source file in host to a created file in /app dir named also /src
COPY src ./src

# Build the project into a .jar file inside /target and skip running tests despite compiling them
RUN mvn package -DskipTests -q

#------------------ Stage 2: Run ---------------------------
# Another base image which is the actual image that will be exported
# that only har jre just for running the .jar file compiled in the first stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the compiled .jar file from previous step to our new final image
COPY --from=builder /app/target/*.jar app.jar

# Run the .jar file using java jre
ENTRYPOINT ["java", "-jar", "app.jar"]