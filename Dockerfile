# Use an official OpenJDK runtime as a parent image
# FROM openjdk:8-jre-slim
# Use base image (Ubuntu 22.04 + OpenSplice)
FROM blekkk/opensplice-runtime:1.0.0-java8

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container at /app
COPY be-tactical-figure.jar /app/

# Copy your native library to a directory in the container
COPY *.lib /app/libs/
EXPOSE 8082
EXPOSE 8083

# Set the java.library.path to include the directory containing the native library
# Set the OSPL_HOME environment variable
ENV OSPL_HOME=/opt/HDE/x86_64.linux
ENV PATH=$OSPL_HOME/bin:$PATH
# Add the LD_LIBRARY_PATH to the container's environment
ENV LD_LIBRARY_PATH=$OSPL_HOME/lib${LD_LIBRARY_PATH:+:}$LD_LIBRARY_PATH
# ENTRYPOINT ["java", "-Djava.library.path=/app/lib/", "-jar", "be-tactical-figure.jar"]
# Run the JAR file when the container starts
CMD ["java", "-jar", "be-tactical-figure.jar"]
# CMD ["echo ","$PATH"]
# CMD ["mmstat"]