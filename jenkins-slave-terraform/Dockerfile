FROM centos:centos7

MAINTAINER w00f

#Install Essentials

RUN yum update -y && \
    yum clean all

# Install packages

RUN curl --silent --location https://rpm.nodesource.com/setup_8.x | bash -

RUN yum install -y git nc wget openssh-server java-1.8.0-openjdk sudo \
        unzip git nodejs && \
        yum clean all

# gen dummy keys, centos doesn't autogen them like ubuntu does

RUN /usr/bin/ssh-keygen -A

# Set SSH Configuration to allow remote logins without /proc write access
RUN sed -ri 's/^session\s+required\s+pam_loginuid.so$/session optional pam_loginuid.so/' /etc/pam.d/sshd

# Create Jenkins User
RUN useradd jenkins -m -s /bin/bash

# Add the jenkins user to sudoers
RUN echo "jenkins  ALL=(ALL)  ALL" >> etc/sudoers
# Set Name Servers
COPY files/resolv.conf /etc/resolv.conf


# Add public key for Jenkins login
RUN mkdir /home/jenkins/.ssh
RUN mkdir /home/jenkins/terraform
#Make dir for statsd
#RUN mkdir /usr/local/src/statsd

COPY files/authorized_keys /home/jenkins/.ssh/authorized_keys
COPY files/terraform_linux_amd64.zip /home/jenkins/terraform.zip


RUN chown -R jenkins /home/jenkins
RUN chgrp -R jenkins /home/jenkins

#Clone StatsD and Start statsd services
RUN git clone https://github.com/etsy/statsd.git /usr/local/src/statsd/
#Copy statsd config file
COPY files/config.js /usr/local/src/statsd/config.js

RUN chmod 600 /home/jenkins/.ssh/authorized_keys
RUN chmod 700 /home/jenkins/.ssh

# Unzip terraform and setup the environment
RUN unzip /home/jenkins/terraform.zip -d /home/jenkins/terraform
RUN rm /home/jenkins/terraform.zip

USER jenkins
ENV PATH=/home/jenkins/terraform:$PATH

USER root
# Expose SSH port and run SSHD
EXPOSE 22
CMD ["sh","-c","node /usr/local/src/statsd/stats.js /usr/local/src/statsd/config.js -d"]
