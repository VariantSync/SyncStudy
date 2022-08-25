FROM openjdk:17-alpine

# Prepare the environment
RUN apk add maven

# Build the jar files
WORKDIR /home/user
COPY src ./src
COPY local-maven-repo local-maven-repo
COPY pom.xml .
RUN mvn package || exit

FROM alpine:3.16.2

RUN apk update
RUN apk add --no-cache --upgrade openjdk17 bash patch git python3 py3-matplotlib
# Create a user
RUN adduser --disabled-password  --home /home/user --gecos '' user
WORKDIR /home/user

# Copy the docker resources
COPY docker/* ./
COPY plots ./plots
COPY simulation-files ./simulation-files

# Copy all relevant files from the previous stage
COPY --from=0 /home/user/target ./target

# Adjust permissions
RUN chown user:user /home/user -R
RUN chmod +x run-simulation.sh
RUN chmod +x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh", "./run-simulation.sh"]
USER user