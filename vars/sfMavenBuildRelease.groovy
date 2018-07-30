#!/usr/bin/groovy

def call(body) {

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    if ( !config.version ) {
        config.version = "1.0.${env.BUILD_NUMBER}"
    }

    container(name: 'maven') {

        stage('Maven deploy') {
            sh "git checkout -b ${env.JOB_NAME}-${config.version}"
            sh "mvn org.codehaus.mojo:versions-maven-plugin:2.5:set -U -DnewVersion=${config.version}"
            sh "mvn --batch-mode --update-snapshots --errors clean deploy"        
        }
        
        stage('Docker push') {
            sh "mvn fabric8:push -Ddocker.push.registry=${env.FABRIC8_DOCKER_REGISTRY_SERVICE_HOST}:${env.FABRIC8_DOCKER_REGISTRY_SERVICE_PORT}"
        }

    }

    sfHelmBuildRelease {
        version = config.version
    }

    // // publish html
    // publishHTML (target: [
    //   allowMissing: true,
    //   alwaysLinkToLastBuild: false,
    //   keepAll: false,
    //   reportDir: 'generated-docs',
    //   reportFiles: 'index.html',
    //   reportName: "Docs"
    // ])

    return config.version
}
