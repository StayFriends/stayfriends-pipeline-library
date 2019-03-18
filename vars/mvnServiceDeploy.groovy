#!/usr/bin/groovy

def call(body) {

    echo "******************************************* YOU ARE NOW IN TESTING BRANCH ***********************************"
    mavenNode() {

        sfCheckout {}

        // Need to bind the variables before the closure
        def config = sfMavenBuildRelease {}
        def envTarget = env.TARGET_ENV

        sfDeployStaging {
            name = config.name
            version = config.version
            environment = envTarget
        }
    }
}
