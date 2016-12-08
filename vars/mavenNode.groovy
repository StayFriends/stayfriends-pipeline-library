#!/usr/bin/groovy
def call(body) {

    def nlabel = "buildpod.${env.JOB_NAME}.${env.BUILD_NUMBER}".replace('-', '_').replace('/', '_')

    echo "podTemplate label: " + nlabel

    podTemplate(name: nlabel, label: nlabel, serviceAccount: 'jenkins', containers: [
        [name: 'maven', image: 'fabric8/maven-builder', command: 'cat', ttyEnabled: true, envVars: [
                [key: 'MAVEN_OPTS', value: '-Duser.home=/home/jenkins/'],
                [key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/'],
                [key: 'KUBERNETES_MASTER', value: 'kubernetes.default']]],
        // [name: 'client', image: 'fabric8/builder-clients', command: 'cat', ttyEnabled: true, envVars: [
        //         [key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/'],
        //         [key: 'KUBERNETES_MASTER', value: 'kubernetes.default']]],
        [name: 'jnlp', image: 'iocanel/jenkins-jnlp-client:latest', command:'/usr/local/bin/start.sh', args: '${computer.jnlpmac} ${computer.name}', ttyEnabled: false,
                envVars: [[key: 'DOCKER_HOST', value: 'unix:/var/run/docker.sock']]]],
        volumes: [
                [$class: 'PersistentVolumeClaim', mountPath: '/home/jenkins/.mvnrepository', claimName: 'jenkins-mvn-local-repo'],
                [$class: 'PersistentVolumeClaim', mountPath: '/home/jenkins/workspace', claimName: 'jenkins-workspace'],
                [$class: 'SecretVolume', mountPath: '/home/jenkins/.m2/', secretName: 'jenkins-maven-settings'],
                [$class: 'SecretVolume', mountPath: '/home/jenkins/.docker', secretName: 'jenkins-docker-cfg'],
                [$class: 'SecretVolume', mountPath: '/root/.ssh', secretName: 'jenkins-ssh-config'],
                [$class: 'HostPathVolume', mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock']
        ]) {
        node(nlabel) {
            echo "execute in maven node"
            body()
        }
    }
}
