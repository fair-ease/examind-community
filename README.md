# EXAMIND Community

Examind Community makes it possible to easily create a complete Spatial Data Infrastructure, from cataloging geographic
resources to operate a platform of sensors that feeds back information in real time.

[https://www.examind.com](https://www.examind.com)

#### Available [OGC web services](http://www.opengeospatial.org/standards)
* **WMS** : 1.1.1 and 1.3.0 (INSPIRE-compliant)
* **WMTS** : 1.0.0
* **CSW** : 2.0.0, 2.0.2 and 3.0.0 (INSPIRE-compliant)
* **SOS** : 1.0.0 and 2.0.0 (need PostGIS database)
* **WFS** : 1.1.0 and  2.0.0 (INSPIRE-compliant)
* **WPS** : 1.0.0
* **WCS** : 1.0.0

#### Available [OGC API](https://ogcapi.ogc.org/)
- **Coverages** ([link](https://ogcapi.ogc.org/coverages/)) : Part 1 - Core
- **Styles** ([link](https://ogcapi.ogc.org/styles/)) : Part 1 - Core
- **Common** ([link](https://ogcapi.ogc.org/common/)) : Part 1 - Core | Part 2 - Collections

#### Supported input data
* Vector :
  * Shapefiles
  * GeoJSON
  * KML
  * GPX
  * GML
  * CSV (with geometry in WKT)
  * MapInfo MIF/MID format
  * PostGIS database
* Raster :
  * Geotiff
  * NetCDF/NetCDF+NCML
  * Grib
  * Images with .tfw and .prj files for projection and transformation informations

## Java version support

 * 1.0.23 is the last stable version supporting java **8**.
 * Any version after that are based upon Java **17**

## Get started

### Build from sources

#### Requirements
* **JDK 17+** from Oracle. Can be downloaded [here](https://www.oracle.com/java/technologies/downloads/#java17) for your platform.
* **Maven 3.x** found [here](https://maven.apache.org/download.cgi)

#### Procedure
```sh
git clone https://github.com/Geomatys/examind-community.git
cd examind-community
mvn install -DskipTests
```
Note 1 : for smaller download without git history: `git clone --depth 1 https://github.com/Geomatys/examind-community.git`

Note 2 : if you want to build with tests, an in-memory HSQL database will be created, however if you want to use a PostgreSQL database, you'll need an empty database. then execute the following command:
```sh
mvn clean install -Dtest.database.url="postgres://<user>:<password>@<host>:<port>/<database name>"
```
example: `postgres://cstl:admin@localhost:5432/cstl-test`

##### S3 storage system

If you want to use S3 storage system client in the project, run this command before all others :
```shell
mvn install -pl :exa-s3-bundle
```

You must also set an environement variable with the AWS region where the buckets you want to use are located.
Currently only one region can be available at the same time. This will be fixed in the future.

**aws.region** : `eu-central-1`

### Deploy using Docker

#### Build Docker image

When building the project, add the `docker` profile to also compile docker image.
This will produce a Tomcat based image of Examind-Community application. The resulting image will be `images.geomatys.com/examind/examind-community:latest`.

*Tip*: you can customize the docker tag of the image by specifying the `docker.tag` property.

*Example*: Build the project and the docker image, setting the image tag to `myVersion`:

```shell
mvn install -Pdocker -Ddocker.tag=myVersion
```

*Tip*: If you've already compiled the project, and just want to rebuild docker image, you can reduce work by re-packaging the `exa-bundle` module with `docker` profile:

```shell
mvn package -Pdocker -pl :exa-bundle
```

#### Run

Go to docker folder
```
cd <base directory>/docker
```
then type the command
```
./run.sh

or

docker-compose up -d
```

the web application will be available at http://localhost:8080/examind
you can authenticate with user = admin and password = admin.

### Deploy on Tomcat

#### Requirements

To run Examind, you'll need :
* **JDK 17**. Can be downloaded [here](https://www.oracle.com/java/technologies/downloads/#java17) for your platform.
* **PostgreSQL 9.x** (found [here](http://www.postgresql.org/download/)) with a database named `constellation` owned by role:password `cstl:admin`
* **Apache Tomcat 7.0.47+** with support of websockets found [here](http://tomcat.apache.org/download-70.cgi)
or
* **Apache Tomcat 8.0.39+** with support of websockets found [here](http://tomcat.apache.org/download-80.cgi)

#### Tomcat configuration
Create a **setenv.sh** executable file in **bin/** folder of Tomcat with :

```
CATALINA_OPTS="$CATALINA_OPTS -Dfile.encoding=UTF8 -Xmx1024m -XX:MaxPermSize=128m -Dgeotk.image.cache.size=128m -XX:-HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./constellation.hprof $CSTL_OPTS -Dspring.profiles.active=standard"
JAVA_HOME=<PATH_TO_JDK>
JRE_HOME=<PATH_TO_JDK>/jre
```
On tomcat 8 add the following property
```
CATALINA_OPTS="$CATALINA_OPTS -Dorg.apache.catalina.core.ApplicationContext.GET_RESOURCE_REQUIRE_SLASH=true"
```

Tomcat startup :
```
<PATH_TO_TOMCAT>/bin/startup.sh
```
Tomcat shutdown :
```
<PATH_TO_TOMCAT>/bin/shutdown.sh
```

### Usage
Browse  [http://localhost:8080/examind](http://localhost:8080/examind) and authenticate with user `admin` and password `admin`.


### Configuration
Examind retrieve his configuration through various inputs using following priority  :
1. System environment variables following standard naming convention
2. Startup options (`-Dproperty=value`) following standard java properties naming convention
3. External configuration file (referenced with `-Dcstl.config=/path/to/config.properties` option)
4. Default embedded configuration

For example, database configuration can be specified from environment variable `DATABASE_URL` or startup/external property `database.url`.

#### Available configuration properties
* **database.url** : application database URL in Hiroku like format. Default value `postgres://cstl:admin@localhost:5432/constellation`
* **epsg.database.url** : EPSG database URL. Default value same as **database.url**
* **test.database.url** : testing database URL. Default value `postgres://test:test@localhost:5432/cstl-test`
* **cstl.config** : Path to application external configuration properties file. Optional, default null.
* **cstl.url** : Examind application URL. Used by Examind to generate resources URLs.
* **cstl.home** : Application home directory, used by Examind to store logs, indexes, ... . By default, Examind will create a `.constellation` directory in current user home folder.
* **cstl.data** : Application data directory, used by Examind to store integrated data and some configurations ... .  By default, Examind will create a `data` directory relative to `cstl.home` property.

SMTP server configuration (used to re-initialize user password) :
* **cstl.mail.smtp.from** : Default value `no-reply@localhost`
* **cstl.mail.smtp.host** : Default value `localhost`
* **cstl.mail.smtp.port** : Default value `25`
* **cstl.mail.smtp.username** : Default value `no-reply@localhost`
* **cstl.mail.smtp.password** : Default value `mypassword`
* **cstl.mail.smtp.ssl** : Default value `false`

#### Database configuration syntax

It is recommended to use [standard jdbc urls](https://docs.oracle.com/javase/tutorial/jdbc/basics/connecting.html#db_connection_url) when specifying database url. A custom syntax is allowed, but not recommended anymore.

## Contribute

### Activate Git hooks
Examind use Git hooks to standardize commits message format.

```shell
rm .git/hooks/commit-msg
ln -s githook/* .git/hooks/
```
