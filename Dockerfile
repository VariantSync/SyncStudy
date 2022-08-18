FROM openjdk:18-alpine

# Prepare the environment
RUN apk add maven

# Build the jar files
WORKDIR /home/user
COPY src ./src
COPY local-maven-repo local-maven-repo
COPY pom.xml .
RUN mvn package || exit

FROM openjdk:18-alpine

RUN apk add bash patch git

# Create a user
RUN adduser --disabled-password  --home /home/user --gecos '' user
WORKDIR /home/user

# Copy the docker resources
COPY docker/* ./

# Copy all relevant files from the previous stage
COPY --from=0 /home/user/target ./target

# Adjust permissions
RUN chown user:user /home/user -R
RUN chmod +x run-simulation.sh
RUN chmod +x entrypoint.sh

RUN ls -l
RUN java -version

ENTRYPOINT ["./entrypoint.sh", "./run-simulation.sh"]
USER user