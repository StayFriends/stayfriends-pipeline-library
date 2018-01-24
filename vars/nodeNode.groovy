#!/usr/bin/groovy
def call(body) {

    def utils = new io.fabric8.Utils()
    def namespace = utils.getNamespace()
    // TODO - check why env not available here, hardcoded f8 namespace service
    //def builderImage = "${env.FABRIC8_DOCKER_REGISTRY_SERVICE_HOST}:${env.FABRIC8_DOCKER_REGISTRY_SERVICE_PORT}/${namespace}/ng2-builder:latest"
    def builderImage = "10.3.0.169:80/${namespace}/ng2-builder:1.0.15"
    echo 'using builder image: ' + builderImage

    def nlabel = "buildpod.${env.JOB_NAME}.${env.BUILD_NUMBER}".replace('-', '_').replace('/', '_')
    echo "podTemplate label: " + nlabel

    podTemplate(name: nlabel, label: nlabel, serviceAccount: 'jenkins', containers: [
        [name: 'ng2-builder', image: builderImage, command: 'cat', ttyEnabled: true, envVars: [
                [key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/'],
                [key: 'KUBERNETES_MASTER', value: 'kubernetes.default']]],
        [name: 'client', image: '10.3.0.169:80/f8/builder-clients:1.0.5', command: 'cat', ttyEnabled: true, envVars: [
                [key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/'],
                [key: 'KUBERNETES_MASTER', value: 'kubernetes.default']]],
        [name: 'jnlp', image: 'iocanel/jenkins-jnlp-client:latest', command:'/usr/local/bin/start.sh', args: '${computer.jnlpmac} ${computer.name}', ttyEnabled: false,
                envVars: [[key: 'DOCKER_HOST', value: 'unix:/var/run/docker.sock']]]],
        volumes: [
                [$class: 'SecretVolume', mountPath: '/home/jenkins/.docker', secretName: 'jenkins-docker-cfg'],
                [$class: 'SecretVolume', mountPath: '/root/.ssh', secretName: 'jenkins-ssh-config'],
                [$class: 'SecretVolume', mountPath: '/home/jenkins/npm-config', secretName: 'jenkins-npm-settings'],
                [$class: 'SecretVolume', mountPath: '/home/jenkins/.aws', secretName: 'helmrepo-aws-config'],
                [$class: 'HostPathVolume', mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock'],
        ]) {
        node(nlabel) {
            body()
        }
    }
}
