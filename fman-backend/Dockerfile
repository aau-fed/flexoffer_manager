# Use custom Oracal Java 8 as parent image because official java:8 is missing some classes
FROM bn21/java8
MAINTAINER "bn21@cs.aau.dk"
LABEL vendor="FED"

# ENV is available inside the container
ENV profile default
ENV port 8085

# Set the working directory to /app
WORKDIR /app

# Copy the required files into the container at /app
ADD target/fman-backend-1.0-SNAPSHOT.jar /app/fman-backend.jar

# Create a mount point on the native host.
VOLUME /tmp

# Port available to the world outside this container
EXPOSE ${port}

# Start the application when the container launches
ENTRYPOINT ["java", "-Duser.timezone=CET", "-Dspring.profiles.active=${profile}", "-jar", "fman-backend.jar"]
