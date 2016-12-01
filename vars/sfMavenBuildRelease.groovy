#!/usr/bin/groovy

def call(body) {

    def versionPrefix = ""
    try {
      versionPrefix = VERSION_PREFIX
    } catch (Throwable e) {
      versionPrefix = "1.0"
    }

    def canaryVersion = "${versionPrefix}.${env.BUILD_NUMBER}"

    container(name: 'maven') {
        stage 'Build Release' 
            mavenCanaryRelease {
                version = canaryVersion
            }
    }
    return canaryVersion
}
