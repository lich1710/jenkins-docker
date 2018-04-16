# JENKINS ENVIRONMENT WITH GRAPHITE & GRAFANA MONITORING USING DOCKER 


## I. PURPOSE

Purpose of the environment is to have a Jenkins environment with a slave running on Docker. For illustration purpose, we have the slave to be built specifically for the purpose of running Terraform infrastructure script. At the same time, the load of the slave server will be reported back to Graphite and displayed using Grafana. You can further tweak the environment to build Jenkins pipeline for different purpose. 

## II. DESIGN

This example has 5 components, all running on Docker Container and can be launch at the same time using Docker-Compose: 

- Jenkins Master Server - installed with Docker-ce and pre-loaded with some Jenkins plugin to run Docker. The master server can use docker-plugin to auto launch an ephemeral container as build slave when new build is triggered. This requires setup in Jenkins to authenticate to the Docker Host. 
- Nginx Proxy - To act as access Proxy to the Jenkins Master Server Web Services. 
- Jenkins Terraform Slave Server - with pre-loaded terraform to run terraform code, statsd and collectd to report metrics back to Graphite
- Graphite Server - Using Graphite-StatsD docker image which can be found here: https://github.com/graphite-project/docker-graphite-statsd. Loaded with predefined Storages-Schema to recognize CollectD metrics
- Grafana Server - Using official Grafana docker image

## III. INSTRUCTION ON LAUNCHING THE ENVIRONMENT

### 1. PREREQUISITES

The whole environment can be launch simply by running the docker-compose.yml script. Let's look at the requirement to launch this environment successfully:

#### JENKINS MASTER:

```
master:
  # Build is the folder where the docker file is
  build: jenkins-master
  ports:
    - "50000:50000"
  volumes:
    - var_jenkins_home:/var/jenkins_home
    - var_log_jenkins:/var/log/jenkins
    - var_backup:/var/jenkins_backup
  links:
    - slave:jenkins-slave
```

For best practices, we are running the Jenkins Master on persistence storage using Docker volumes. There are 3 volumes here attached to this container, the most important one is the var_jenkins_home volume which stores all the Jenkins configurations, plugins, jobs. 

```
nginx:
  build: jenkins-nginx
  ports:
    - "80:80"
  links:
  #Refer to the compose name inside this file is jenkinsmaster
    - master:jenkins-master
```

Nginx container act as access proxy to the Jenkins Web Interface. Two containers will use port 50000 and 80 of the Docker host. Change to another port if there is conflict.

#### JENKINS SLAVE:

```
slave:
  build: jenkins-slave-terraform
  volumes:
  # AWS credentials folder. Export $HOME before run docker-compose
    - $HOME/.aws:/home/jenkins/.aws
  links:
    - graphite:graphite
```

The slave was built for launching Terraform script, thus need AWS credentials. This either can be done by simply mounting the aws credentials folder from the host or set as environment variable in the slave or define in Jenkins. 

#### GRAPHITE AND GRAFANA SERVER:

```
graphite:
  build: graphite
  container_name: graphite
  ports:
    - "8080:80"
    - "2003-2004:2003-2004"
    - "2023-2024:2023-2024"
    - "8125:8125/udp"
    - "8126:8126"
  volumes:
    - graphite-date:/opt/graphite
```

```
grafana:
  image: "grafana/grafana"
  container_name: grafana
  ports:
    - "3000:3000"
  links:
    - graphite:graphite
  volumes:
    - grafana-storage:/var/lib/grafana
```

Graphite Web Interface can be access via port 8080 and Grafana can be access via ports 3000. If there are ports conflict please change the port number.



### 2. LAUNCHING THE ENVIRONMENT

From top folder, run:

```
docker-compose up -d
```

#### a. You can skip some steps b and c by using my Jenkins home folder and Grafana from below sources:

Download my /var/jenkins_home folder: https://1drv.ms/u/s!AmQg3nnClm2t70KYQdhR-4OK8z8r

Copy the content over the folder /var/jenkins_home in Jenkins Master Server or on host Volume

Download my /var/lib/grafana folder: https://1drv.ms/u/s!AmQg3nnClm2t70NLZqu1nkHvo51d

Copy the content over the folder /var/lib/grafana in Grafana Server or on host Volume. 


#### b. SETUP JENKINS MASTER 

To authenticate Jenkins master with Jenkins slave, we need to add the private key to use to login to slave:

1. From Jenkins Web Interface -> Credentials -> System -> Global credentials 

Add a new key with the content from the files: https://github.com/lich1710/jenkins-docker/blob/master/jenkins-master/privatekey

2. Add Jenkins-Slave node
Go to Manage Jenkins -> Manage Node -> New Node. Configure as below using the key above:

![alt text](sample-images/Screen%20Shot%202018-04-16%20at%209.40.01%20PM.png)

3. Run a simple Terraform Pipeline on the new slave

Create a new project -> Choose Pipeline. Paste the code from the file in: ![terraform.groovy](terraform.groovy)

4. Build the pipeline

Review the Terraform plan at the stage Plan, if you are okay with the Plan, you can deploy the infrastructure by click on Yes on Apply stage. Else click on Abort. 

![alt text](sample-images/Screen%20Shot%202018-04-15%20at%2011.41.48%20PM.png)

#### c. SETUP GRAPHITE AND GRAFANA

GRAPHITE has been preconfigured to received statsd and collectd metric. You can see the metrics either in Graphite Web or Grafana.
Go to Grafana Web, login using admin/admin credentials. Since Grafana container already linked to Graphite as defined as hostname graphite, you can configure the Grafana data source from Graphite as below:

![alt text](sample-images/Screen%20Shot%202018-04-16%20at%209.29.09%20PM.png)

Build some simple dashboard to display the metrics from Graphite:

![alt text](sample-images/Screen%20Shot%202018-04-15%20at%2011.40.33%20PM.png)


## IV. CONCLUSION

With this setup, we have successfully built a simple Jenkins environment with a master and a slave. The slave Dockerfile was specifically targeted to run Terraform build, with further tweak, it can run other kind of code base. The slave CPU and Memory load was constantly monitored by Graphite and Grafana. 

Using my Jenkins master setting and correct Docker host setting, you can even launch an ephemeral container every time a build is triggered, and the container is auto destroyed after the build successful. 

In the futures, I will add function to track successful & failed build from Jenkins and report those metric back to be displayed on Grafana.
