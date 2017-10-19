#!/usr/bin/groovy
def call(body) {

    def nlabel = "buildpod.${env.JOB_NAME}.${env.BUILD_NUMBER}".replace('-', '_').replace('/', '_')

    echo "podTemplate label: " + nlabel

    podTemplate(name: nlabel, label: nlabel, serviceAccount: 'jenkins', containers: [
        [name: 'maven', image: 'fabric8/maven-builder', command: 'cat', ttyEnabled: true, envVars: [
                [key: 'MAVEN_OPTS', value: '-Duser.home=/home/jenkins/'],
                [key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/'],
                [key: 'KUBERNETES_MASTER', value: 'kubernetes.default']]],
        [name: 'client', image: '10.3.0.169:80/f8/builder-clients:1.0.5', command: 'cat', ttyEnabled: true, envVars: [
                secretEnvVar(secretName: "helmrepo-minio-user", key: 'AWS_ACCESS_KEY_ID', secretKey: 'accesskey'),
                secretEnvVar(secretName: "helmrepo-minio-user", key: 'AWS_SECRET_ACCESS_KEY', secretKey: 'secretkey'),
                [key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/'],
                [key: 'KUBERNETES_MASTER', value: 'kubernetes.default']]],
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

            try {
                body()
            } catch (e) {
                // If there was an exception thrown, the build failed
                currentBuild.result = "FAILED"
                throw e
            } finally {
                // Success or failure, always send notifications
                notifyBuild(currentBuild.result)
            }

        }
    }
}

def notifyBuild(String buildStatus = 'STARTED') {
  // build status of null means successful
  buildStatus = buildStatus ?: 'SUCCESS'

  // Default values
  def colorName = 'RED'
  def colorCode = '#FF0000'
  def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
  def summary = "${subject} (${env.BUILD_URL})"
  def details = """<p>${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
    <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>"""

  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = 'YELLOW'
    colorCode = '#FFFF00'
  } else if (buildStatus == 'SUCCESS') {
    color = 'GREEN'
    colorCode = '#00FF00'
  } else {
    color = 'RED'
    colorCode = '#FF0000'
  }

  // Send notifications
  // slackSend (color: colorCode, message: summary)

  // hipchatSend (color: color, notify: true, message: summary)

  emailext (
      subject: subject,
      body: details,
      recipientProviders: [[$class: 'DevelopersRecipientProvider']]
    )
}
