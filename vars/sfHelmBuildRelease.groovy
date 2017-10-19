#!/usr/bin/groovy

def call(body) {

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

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

                sh "helm init --client-only"

                // verify chart
                sh "helm lint ${chartDir}"

                // package chart
                sh "helm package --version ${config.version} ${chartDir}"
                def chartPackage = "${config.name}-${config.version}.tgz"
                sh "mkdir -p target/helm"
                sh "mv ${chartPackage} target/helm"
                chartPackage = "target/helm/${chartPackage}"

                def s3Endpoint = "http://helmrepo-minio-svc"
                def helmRepoPath = "testing"
                sh "aws --endpoint-url ${s3Endpoint} s3 cp ${chartPackage} s3://${helmRepoPath}"

                // publish helm chart to helm repo
                //sh "helm repo add sf-testing http://helmrepo-minio-svc.f8.test-kublet-cluster.nbg1.stayfriends.de/testing"
                // update repo index

            }
        }
    }

    return config.version
}
