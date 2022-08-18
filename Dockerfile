# syntax=docker/dockerfile:1
FROM openjdk:18-alpine

# Prepare the environment
RUN apk add maven
RUN apk add git

# Build the jar files
WORKDIR /home/user
COPY src ./src
COPY local-maven-repo local-maven-repo
COPY pom.xml .
RUN mvn package || exit

# Clone BusyBox
RUN git clone git://busybox.net/busybox.git

FROM openjdk:18-alpine

RUN apk add bash patch

# Create a user
RUN adduser --disabled-password  --home /home/user --gecos '' user
WORKDIR /home/user
# Copy dataset
COPY variability-busybox ./variability-busybox
# Copy the docker resources
COPY docker/* ./

# Copy all relevant files from the previous stage
COPY --from=0 /home/user/target ./target
COPY --from=0 /home/user/busybox ./busybox

# Adjust permissions
RUN mkdir "/home/user/simulation-files"
RUN ls -l
RUN chown user:user /home/user -R
RUN chmod +x run-simulation.sh
RUN chmod +x entrypoint.sh

RUN ls -l
RUN java -version

ENTRYPOINT ["./entrypoint.sh", "./run-simulation.sh"]
USER user