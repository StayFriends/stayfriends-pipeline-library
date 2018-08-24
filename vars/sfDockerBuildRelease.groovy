#!/usr/bin/groovy

def call(body) {

    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    echo "build config = " + config

    container('client') {
      stage 'Build Release'

      def registry = "${env.FABRIC8_DOCKER_REGISTRY_SERVICE_HOST}:${env.FABRIC8_DOCKER_REGISTRY_SERVICE_PORT}"
      def imageName = "${registry}/${config.group}/${config.name}:${config.version}"
      config.image = imageName
      
      sh "docker build -t ${imageName} ."
      sh "docker push ${imageName}"
    }

    return config
}
