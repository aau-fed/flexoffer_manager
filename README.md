# FlexOffer Manager #

*FlexOffer Manager (FMAN)* - is a software system for aggregators, balance responsible parties (BRPs), micro-grid responsibles (MGRs) for managing (potentially large) collections of flexible electrical loads in the [FlexOffer form](https://www.goflex-community.eu/). FMAN integrates advanced FlexOffer aggregation and disaggregation functionalities, optimization, as well as Graphical User Interface (GUI), which allows its users effectively and efficiently analyzing, trading, and shaping available flexibility in near real-time. 

### What is this repository for? ###

In this repository, you will be able to find the source code of the FMAN back-end and front-end sub-systems, installation and deployment instructions, 
and a user manual.

### How do I get set up? ###

#### Manually setting-up and running FMAN ####

##### Setting-up the back-end #####

1. Configure `JDK` and `Apache Maven` environments. FOA requires `JDK 8` which can be found [here](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html). Instructions for installing `Apache Maven` are available [here](https://maven.apache.org/install.html).

2. Install, configure, and run `MySQL` database management system. Instructions are available [here](https://dev.mysql.com/doc/mysql-installation-excerpt/8.0/en/installing.html).

3. Update configuration file `/fman-backend/src/main/resources/fman.properties`. At the minimum, you will need to update the following:
 ```
 spring.datasource.url=jdbc:mysql://localhost:3306/fman?useSSL=false&serverTimezone=CET
 spring.datasource.username=fed
 spring.datasource.password=password
 ```
The default db is `fman` which should be created if it does not exist. Also, the default password is `password`, which should obviously be changed.

2. Go to the root folder and run:
   ```mvn clean compile package```

3. Run the FMAN back-end using the command:
   ```java -jar fman-backend/target/fman-backend-1.0-SNAPSHOT.jar```


##### Setting-up the front-end #####

1. Configure `Node.js` and the `Node Package Manage (npm)` tool. Download instructions can be found [here](https://nodejs.org/en/download/).

2. Make sure you have the [Angular CLI](https://github.com/angular/angular-cli#installation) installed.

3. Go to the `fman-frontend` and install all javascript dependencies using the comand:
    ```npm install```

4. Run the front-end application using the command:
   ```ng serve```


#### B. Automated set-up and running using `docker`

It's easy to setup and run the app with docker. You must install `docker` and `docker-compose`. Installation instruction are available [here](https://docs.docker.com/docker-for-windows/install/)

Open `.env` file and update the following with the correct values
```
MYSQL_ROOT_PASSWORD=
MYSQL_USER=
MYSQL_PASSWORD=
```

##### Pull and run pre-built docker images

```bash
docker-compose pull # you will need to install docker-compose if not installed already

# to launch a service, you must be in the same directory containing docker-compose.yml

# run `foa-app`
docker-compose up fman-backend

# see container logs
docker-compose logs -f fman-backend

# run `fman-frontend`
docker-compose up fman-frontend

# run all
docker-compose up
```

##### Build and run docker images locally

```bash
# compile 
mvn clean compile package

# build docker image
docker-compose build
```

After building docker images, you can run them using the above commands

##### Connect/Disconnect to/from a container

```bash
# attach to a running container
docker attach <container-name>

# detach from the running container
CTRL-p, CTRL-q
```

After the container is up and running, the services can be accessed on ports configured in `docker-compose.yml`

##### Redeployment after changing/updating code
When you make changes to your app code, remember to rebuild your image and recreate your docker containers.
To redeploy a service, use the following command the following commands.
The first rebuilds the image for the service and then stop, destroy, and recreate just the web service.
The --no-deps flag prevents `docker-compose` from also recreating any services which this service depends on.

```
docker-compose build --pull <service-name>
docker-compose up --no-deps -d <service-name>
```

##### Remove unnecessary docker images

```bash
docker system prune -f
```

### User Documentation ###

User documentation can be found at [Flex-Offer Manager User Manual](./FlexOffer_Manager_User_Manual.pdf).

### References ###

* [FlexOffer resource page](https://www.daisy.aau.dk/flexoffers/)


### Acknowledgments ###

This project is supported by Flexible Energy Denmark (FED) - a Danish digitization project aimed at turning Danish electricity consumption flexible to enable excess power production from wind turbines and solar cells. The project is funded by Innovation Fund Denmark.
