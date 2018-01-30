#!/usr/bin/groovy

def call(body) {

    container(name: 'ng2-builder') {

        try {
            stage('Dependencies') {
                sh 'cp /home/jenkins/npm-config/.npmrc .'
                sh 'npm config list'
                sh 'npm --loglevel info install'
            }
        } catch (e) {
            error('could not resolve dependencies')
        }

        try {
            stage('Test') {
                try {
                    env.NODE_ENV = "test"
                    sh 'npm run lint'
                } catch (e) {
                    error('could not lint artefact')
                }
                    sh 'Xvfb :99 -screen 0 1024x768x16 &'
                    sh 'npm run test'
            }
        } catch (e) {
            error('could not test artefact')
        }

        try {
            stage('Build') {
                sh 'npm run build'
            }
        } catch (e) {
            error('could not build artefact')
        }

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
