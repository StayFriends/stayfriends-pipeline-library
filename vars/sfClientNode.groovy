#!/usr/bin/groovy
def call(body) {

    def utils = new io.fabric8.Utils()
    def namespace = utils.getNamespace()

    def nlabel = "buildpod.${env.JOB_NAME}.${env.BUILD_NUMBER}".replace('-', '_').replace('/', '_')
    echo "podTemplate label: " + nlabel

    podTemplate(name: nlabel, label: nlabel, serviceAccount: 'jenkins', containers: [
        [name: 'client', image: '10.3.0.169:80/f8/builder-clients:1.0.5', command: 'cat', ttyEnabled: true, envVars: [
                [key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/'],
                [key: 'KUBERNETES_MASTER', value: 'kubernetes.default']]],
        [name: 'jnlp', image: 'iocanel/jenkins-jnlp-client:latest', command:'/usr/local/bin/start.sh', args: '${computer.jnlpmac} ${computer.name}', ttyEnabled: false,
                envVars: [[key: 'DOCKER_HOST', value: 'unix:/var/run/docker.sock']]]],
        volumes: [
                [$class: 'SecretVolume', mountPath: '/home/jenkins/.docker', secretName: 'jenkins-docker-cfg'],
                [$class: 'HostPathVolume', mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock'],
                [$class: 'SecretVolume', mountPath: '/root/.ssh', secretName: 'jenkins-ssh-config'],
                [$class: 'SecretVolume', mountPath: '/home/jenkins/.aws', secretName: 'helmrepo-aws-config']
        ]) {
        node(nlabel) {
            body()
        }
    }
}
