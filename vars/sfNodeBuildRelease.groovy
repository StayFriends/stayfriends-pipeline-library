#!/usr/bin/groovy

import groovy.json.JsonSlurper

def getNodeProjectVersion() {
  def file = readFile('package.json')
  def project = new JsonSlurper().parseText(file)
  return project.version
}

def call(body) {

    container(name: 'ng2-builder') {
        
        sfNodeNpmBuild()
    }

    try {
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
    } catch (e) {
        error('could not install release')
    }

    return imageVersion
}
