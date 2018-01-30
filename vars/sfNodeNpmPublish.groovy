#!/usr/bin/groovy

def call(body) {

    container(name: 'ng2-builder') {

        sfNodeNpmBuild()

        try {
            stage('NPM Publish') {
                sh 'cp /home/jenkins/npm-config/.npmrc-publish dist/.npmrc'
                sh 'cd dist && npm publish'
            }
        } catch (e) {
            error('could not publish to repository')
        }
    }
}
