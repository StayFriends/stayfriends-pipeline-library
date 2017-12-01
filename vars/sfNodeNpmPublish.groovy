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
        sh 'npm run dist'

        if ( fileExists("dist") ) {
            stage 'NPM Publish'
            writeFile file: "/home/jenkins/dist/.npmrc", text: "registry=http://nexus.f8.test-kublet-cluster.nbg1.stayfriends.de/content/repositories/private-npm-registry/\r\ninit.author.name=Jenkins\r\nemail=jenk@ins.com\r\nalways-auth=true\r\n_auth=YWRtaW46YWRtaW4xMjM="
            sh 'cd dist && npm publish'
        }
    }
}
