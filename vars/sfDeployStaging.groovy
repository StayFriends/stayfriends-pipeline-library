#!/usr/bin/groovy

def call(body) {

    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def utils = new io.fabric8.Utils()

    if ( !config.name ) {
    	config.name = env.JOB_NAME
    }
    if ( !config.environment ) {
    	config.environment = "staging"
    }
    if ( !config.group ) {
    	config.group = "webapp"
    }
    if ( !config.version ) {
    	config.version = "0.${env.BUILD_NUMBER}"
    }
    if ( !config.image ) {
		// default full image name including registry
		config.image = "${env.FABRIC8_DOCKER_REGISTRY_SERVICE_HOST}:${env.FABRIC8_DOCKER_REGISTRY_SERVICE_PORT}/${utils.getNamespace()}/${config.name}:${config.version}"
    }
    if ( !config.rcName ) {
		// this file is read as default, as it is produced by maven plugin f-m-p
		config.rcName = "target/classes/META-INF/fabric8/kubernetes.yml"
    }
    echo "deploy config = " + config

    //container(name: 'client') {

	stage 'Deploy Staging' 

	def envStage = utils.environmentNamespace(config.environment)
	echo "deploying to environment: " + envStage

    //deployWithFabric8(envStage,config)
    def deployWith = "fabric8"
	if ( fileExists("helm") ) {
		deployWith = "helm"
	}

    if (deployWith == "fabric8") {
		rc = ""
		if ( fileExists(config.rcName) ) {
			rc = readFile file: config.rcName
		} else {
			rc = sfKubernetesResourceWebapp {
				name = config.name
				group = config.group
				version = config.version
				port = 80
				image = config.image
				icon = "https://cdn.rawgit.com/fabric8io/fabric8/dc05040/website/src/images/logos/nodejs.svg"
			}

			// save and upload generated rc file
			echo "uploading kubernetes rc to nexus: ${config.name}/${config.version}/kubernetes.json"
			writeFile file: "kubernetes.json", text: rc
			sh "curl -v -u admin:admin123 --upload-file kubernetes.json http://nexus/content/repositories/staging/${config.name}/${config.version}/kubernetes.json"

		}

		echo "applying kubernetes rc: " + rc
		kubernetesApply(file: rc, environment: envStage)
		//sh "kubectl apply -f target/classes/META-INF/fabric8/kubernetes.yml"
	}

    if (deployWith == "helm") {
		container(name: 'client') {
			sh "helm lint helm/${config.name}"
			sh "helm delete ${config.name}-${envStage}"
			sh "helm upgrade ${config.name}-${envStage} helm/${config.name} --namespace ${envStage} --install"
		}
	}
}
