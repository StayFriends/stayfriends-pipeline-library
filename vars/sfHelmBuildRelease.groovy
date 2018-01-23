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

    def helmSrcDir = "helm"

    if ( fileExists(helmSrcDir) ) {
        container(name: 'client') {
            stage('Helm build') {
                def chartDir = "${helmSrcDir}/${config.name}"
                def chartFile = "${chartDir}/Chart.yaml"

                sh "helm init --client-only"

                // verify chart
                sh "helm lint ${chartDir}"

                // package chart
                sh "helm package --version ${config.version} ${chartDir}"
                def chartPackage = "${config.name}-${config.version}.tgz"

                def helmDir = "target/helm"
                sh "mkdir -p ${helmDir}"
                sh "mv ${chartPackage} ${helmDir}"
                chartPackage = "${helmDir}/${chartPackage}"

                // publish helm chart to helm repo
                def s3Endpoint = "http://helmrepo-minio-svc:9000"
                def helmRepoPath = "testing"
                def helmRepoUrl = "${s3Endpoint}/${helmRepoPath}"
                // download index
                sh "aws --endpoint-url ${s3Endpoint} s3 cp s3://${helmRepoPath}/index.yaml ${helmDir}"
                // update index
                sh "helm repo index --url ${helmRepoUrl} --merge ${helmDir}/index.yaml ${helmDir}"
                // upload chart
                sh "aws --endpoint-url ${s3Endpoint} s3 cp ${chartPackage} s3://${helmRepoPath}"
                // upload index
                sh "aws --endpoint-url ${s3Endpoint} s3 cp ${helmDir}/index.yaml s3://${helmRepoPath}"

                //sh "helm repo add sf-testing http://helmrepo-minio-svc.f8.test-kublet-cluster.nbg1.stayfriends.de/testing"

            }
        }
    }

    return config.version
}
