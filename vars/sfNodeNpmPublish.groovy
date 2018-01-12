#!/usr/bin/groovy

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
            sh 'npm run test'
        }

        stage('Build') {
            sh 'npm run build'
        }

        stage('NPM Publish') {
            sh 'cp /home/jenkins/npm-config/.npmrc dist/'
            sh 'cd dist && npm publish'
        }
    }
}
