#!/usr/bin/groovy

def call(body) {

    try {
        stage('Dependencies') {
            sh 'cp /home/jenkins/npm-config/.npmrc .'
            sh 'npm config list'
            sh 'npm cache clean --force'
            sh 'rm -rf ./node_modules'
            sh 'rm -rf ./package-lock.json'
            sh 'npm --loglevel info install'
        }
    } catch (e) {
        error('could not resolve dependencies')
    }

    stage('Test') {
        try {
            env.NODE_ENV = "test"
            sh 'npm run lint'
        } catch (e) {
            error('could not lint artefact')
        }
        try {
            sh 'Xvfb :99 -screen 0 1024x768x16 &'
            sh 'npm run test'
        } catch (e) {
            error('could not test artefact')
        }
    }

    try {
        stage('Build') {
            sh 'npm run build'
        }
    } catch (e) {
        error('could not build artefact')
    }
}
