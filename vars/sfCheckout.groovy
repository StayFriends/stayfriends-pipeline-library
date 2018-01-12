#!/usr/bin/groovy
def call(body) {

    stage('Checkout') {
        //git GIT_URL
        // git access configured for root user
        sh 'chmod -R 600 /root/.ssh' // required for ssh, as long, as it is not supported as option in podTemplate
        checkout scm
    }
    
}
