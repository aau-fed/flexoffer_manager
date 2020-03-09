# FlexOffer Manager #

*FlexOffer Manager (FMAN)* - is a software system for aggregators, balance responsible parties (BRPs), micro-grid responsibles (MGRs) for managing (potentially large) collections of flexible electrical loads in the [FlexOffer form](https://www.goflex-community.eu/). FMAN integrates advanced FlexOffer aggregation and disaggregation functionalities, optimization, as well as Graphical User Interface (GUI), which allows its users effectively and efficiently analyzing, trading, and shaping available flexibility in near real-time. The initial open-source version of the system was developed by researchers at the Daisy group, Aalborg University, Denmark. FMAN was successfully deployed and demonstrated in Cyprus, Germany, and Switzerland during the [H2020 GOFLEX project](https://goflex-project.eu/).

### What is this repository for? ###

In this reposity, you will be able to find the source code of the FMAN back-end and front-end sub-systems, installation and deployment instructions, 
and a user manual.

### How do I get set up? ###

#### Manually setting-up and running FMAN ####

##### Setting-up the back-end #####

1. Configure JDK and Maven environments

2. Install, configure, and run a database management system, e.g., MySQL.

3. Update FMAN configuration file _/fman-backend/src/main/resources/fman.properties_. Specifically, "spring.datasource.url" datasource URL needs to be updated.

4. Go to to the root folder and run: 
	```mvn clean compile package```
	
5. Run the FMAN back-end using the command:
	```java -jar fman-backend/target/fman-backend-1.0-SNAPSHOT.jar```

##### Setting-up the front-end #####
 
1. Configure Node.js and the Node Package Manage (npm) tool

2. Go to the _/fman-frontend/_ and install all javascript dependencies using the comand:
    ```npm install```

3. Run the front-end application using the command:
   ```ng serve```


#### Install using the docker ####
```bash
sudo curl -L "https://github.com/docker/compose/releases/download/1.23.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

* Attach/detach from container
```bash
# attach
docker attach container-name

# detach
CTRL+p CTRL+q
```

* build and push all images (run this command from the directory containing `docker-compose.yml`)
```
# build images
docker-compose -f docker-compose.yml -f docker-compose.build.yml build

# push images to remote container registry
docker-compose -f docker-compose.yml -f docker-compose.build.yml push
```

* remove multiple docker images matching a parttern/wildcard
```
docker rmi $(docker images | grep repo.treescale.com | tr -s ' ' | cut -d ' ' -f 3)
```

* login to private container registry service
```bash
docker login repo.treescale.com
```

* When you make changes to your app code, remember to rebuild your image and recreate your app’s containers. To redeploy a service called `web`, use the following command the following commands. The first rebuilds the image for web and then stop, destroy, and recreate just the web service. The --no-deps flag prevents Compose from also recreating any services which web depends on.
```
docker-compose build --pull foa-app
docker-compose up --no-deps -d foa-app
```

### User Documentation ###

User documentation can be found at _/AAU Flex-Offer Manager User Manual.docx_.

### Refereces ###

* [FlexOffer resource page](https://www.daisy.aau.dk/flexoffers/)
* https://goflex-project.eu/


### Acknowledgments ###

The project Generalized Operational FLEXibility for Integrating Renewables in the Distribution Grid (GOFLEX) has received funding from the European Union's Horizon 2020 research and innovation programme under grant agreement No 731232. 

![logo](https://goflex-project.eu/global/images/eu.png)
