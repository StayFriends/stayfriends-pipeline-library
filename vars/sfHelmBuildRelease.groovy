#!/usr/bin/groovy

def call(body) {

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def sf = new com.stayfriends.Utils()

    if ( !config.name ) {
        config.name = env.JOB_NAME
    }
    if ( !config.version ) {
        config.version = "1.0.${env.BUILD_NUMBER}"
    }

    def helmDir = "helm"

    if ( fileExists(helmDir) ) {
        container(name: 'client') {
            stage('Helm build') {
                def chartDir = "${helmDir}/${config.name}"
                def chartFile = "${chartDir}/Chart.yaml"

                // verify chart
                sh "helm lint ${chartDir}"

                // package chart
                sh "helm package --version ${config.version} ${chartDir}"
                def chartPackage = "${config.name}-${config.version}.tgz"
                
                // TODO publish helm chart to helm repo
                //sh "curl -v -u admin:admin123 --upload-file ${chartPackage} http://nexus/content/repositories/staging/${config.name}/${config.version}/${chartPackage}"
            }
        }
    }

    return config.version
}
