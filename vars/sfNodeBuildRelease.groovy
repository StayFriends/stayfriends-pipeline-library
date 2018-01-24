#!/usr/bin/groovy

import groovy.json.JsonSlurper

def getNodeProjectVersion() {
  def file = readFile('package.json')
  def project = new JsonSlurper().parseText(file)
  return project.version
}

def call(body) {

    container(name: 'ng2-builder') {

        stage('Dependencies') {
            sh 'cp /home/jenkins/npm-config/.npmrc .'
            sh 'npm config list'
            sh 'npm --loglevel info install'
        }

        stage('Test') {
            env.NODE_ENV = "test"
            sh 'npm run lint'
            sh 'Xvfb :99 -screen 0 1024x768x16 &'
            sh 'npm test'
          }

        stage('Build') {
            sh 'npm run dist'
        }
    }

    def imageVersion = ""
    
    container('client') {
        stage('Build Release') {
            def versionPrefix = getNodeProjectVersion()
            def canaryVersion = "${versionPrefix}-build.${env.BUILD_NUMBER}"
            dir('dist') {
              imageVersion = performCanaryRelease {
                version = canaryVersion
              }
            }
        }
    }

    sfHelmBuildRelease {
        version = imageVersion
    }

    return imageVersion
}
