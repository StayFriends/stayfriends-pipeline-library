#!/usr/bin/groovy

def call(body) {

    def nlabel = "buildpod.${env.JOB_NAME}.${env.BUILD_NUMBER}".replace('-', '_').replace('/', '_')

    echo "podTemplate label: " + nlabel

    podTemplate(name: nlabel, label: nlabel, serviceAccount: 'jenkins', containers: [
        [name: 'ng2-builder'],
        volumes: [
                [$class: 'SecretVolume', mountPath: '/jenkins-config/', secretName: 'jenkins-npm-settings']
        ]) {
        node(nlabel) {

            stage 'Dependencies'
            sh 'npm config list'
            sh 'npm --loglevel info install'

            if ( fileExists("src") ) {
                stage 'Test'
                env.NODE_ENV = "test"
                sh 'npm run lint'
                sh 'Xvfb :99 -screen 0 1024x768x16 &'
                sh 'npm run test'
            }

            stage 'Build'
            sh 'npm run build'

            if ( fileExists("dist") ) {
                stage 'NPM Publish'
                sh 'cp /jenkins-config/* dist/'
                sh 'cd dist && npm publish'
            }
        }
    }
}
