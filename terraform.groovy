pipeline {
    agent { node 'jenkins-slave' }
    environment { PATH = "/home/jenkins/terraform:$PATH" }
    stages {
        stage('Clone Terraform Code') {
            steps {
                sh 'terraform --version'
                sh 'rm -rf reusable && git init && git clone https://github.com/lich1710/reusable.git'
            }
        }

        stage ('Init') {
            steps {
                sh 'terraform init -input=false ./reusable/stage/services/front-end'
            }
        }

        stage ('Plan') {
            steps {
                sh 'terraform plan -out=./reusable/stage/services/front-end/tfplan -input=false ./reusable/stage/services/front-end'
            }
        }


        stage ('Apply') {
            input {
                message "Should we continue?"
                ok "Yes"
            }
            steps {
                sh 'terraform apply -input=false ./reusable/stage/services/front-end/tfplan'
            }
        }

    }
}
