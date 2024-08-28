## REST ADAPTER

###Why ? What ?

Ericsson Cloud Manager(ECM) exposses REST (Representational State Transfer) Api for managing the cloud infrastructure. Like adding new Virtual Machine etc. As part of the POC FMX rule is configured to Add a new EPG Node on some condition, which is achived by invoking the REST Adpater. 
The application a Java Application, so it requires a Java Rutime (JRE) to run . This REST adapter is a small piece of Java Code which invokes the ECM  REST  Api to add a new Virtual Machine(Image) of a EPG node. The java code is wrapped around with a shell script, and appconfig.properties (configuration file) .

###Code structure

Code is structured as a typical Maven project, with a pom.xml to define the project, its dependencies and lifecycle. The assenmbly.xml is used to define the packaing.You will need Java Development Env (JDK > 1.6) to build the application.The bin folder contains run-adapter.sh, shell script file to invoke the Java Application.

###Configuration Options

As mentioned previously, the appconfig.properties is used to externalize the configuration parameters from code.This advocates the write once deploy anywhere principle, you donot have to change the code and recompile to run the code in different environments. Instead these changing parameters are configured in a properties file which is used during runtime. 

####REST Server Endpoint Configuration 

Given below are end-point and authenitication properties of REST server.

rest.server.endpoint=http://localhost:9000/ecm_service/
rest.server.auth.username=ecmadmin
rest.server.auth.password=ecmadmin

Given below is C REST operation[URI] name property
rest.server.createvnic.uri=orders

The REST request is a string property as show below, You will have to update the vdcId,vnId etc

createvm-vnic.template={"order":{"orderItems" ...............


###Building and Using

Use the below Mavne command to build the deployable applicaiton.

mvn assembly:assembly 

it generates the ecmrest-client-1.0-bin.zip or tar.gz which is the deployable application, just unzip in the required directory and grant permissions.