# Stage 0, "build-stage", to build and compile the frontend
FROM teracy/angular-cli as build-stage
MAINTAINER "bn12@cs.aau.dk"
LABEL vendor="FED"

# Set the working directory to /app
WORKDIR /app

# copy package.json and package-lock.json to /app/
COPY package*.json /app/

# install dependencies
RUN npm install

# copy rest of the porject (excluding node_modules/ and dist/ as they are in .dockerignore)
COPY . /app/

# Compile app
RUN ng build --output-path=./dist/out

# Stage 1, based on Nginx, to have only the compiled app, ready for production with Nginx
FROM nginx
COPY --from=build-stage /app/dist/out/ /usr/share/nginx/html

# Port available to the world outside this container
EXPOSE 80

# Copy the default nginx.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf
