#!/usr/bin/groovy

def call(body) {

    container(name: 'ng2-builder') {

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
            withNPM(npmrcConfig: 'private-registry-npmrc') {
                sh 'cd dist && npm publish'
            }
        }
    }
}
