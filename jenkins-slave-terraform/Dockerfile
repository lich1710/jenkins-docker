FROM centos:centos7

MAINTAINER w00f

#Install Essentials

RUN yum update -y && \
    yum clean all

# Install packages

RUN yum install -y git && \
        yum install -y wget && \
        yum install -y openssh-server && \
        yum install -y java-1.8.0-openjdk && \
        yum install -y sudo && \
        yum install -y unzip && \
        yum clean all

# gen dummy keys, centos doesn't autogen them like ubuntu does

RUN /usr/bin/ssh-keygen -A

# Set SSH Configuration to allow remote logins without /proc write access
RUN sed -ri 's/^session\s+required\s+pam_loginuid.so$/session optional pam_loginuid.so/' /etc/pam.d/sshd

# Create Jenkins User
RUN useradd jenkins -m -s /bin/bash

# Add public key for Jenkins login
RUN mkdir /home/jenkins/.ssh
RUN mkdir /home/jenkins/.aws
RUN mkdir /home/jenkins/terraform

COPY files/authorized_keys /home/jenkins/.ssh/authorized_keys
COPY files/credentials  /home/jenkins/.aws/credentials
COPY files/terraform_linux_amd64.zip /home/jenkins/terraform.zip
RUN chown -R jenkins /home/jenkins
RUN chgrp -R jenkins /home/jenkins

RUN chmod 600 /home/jenkins/.ssh/authorized_keys
RUN chmod 500 /home/jenkins/.aws/credentials
RUN chmod 700 /home/jenkins/.ssh

# Add the jenkins user to sudoers
RUN echo "jenkins  ALL=(ALL)  ALL" >> etc/sudoers

# Set Name Servers
COPY files/resolv.conf /etc/resolv.conf

# Unzip terraform and setup the environment
RUN unzip /home/jenkins/terraform.zip -d /home/jenkins/terraform
ENV PATH=/home/jenkins/terraform:$PATH
RUN rm /home/jenkins/terraform.zip

# Expose SSH port and run SSHD
EXPOSE 22
CMD ["/usr/sbin/sshd","-D"]
