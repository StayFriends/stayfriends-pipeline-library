#!/usr/bin/groovy

def call(body) {

    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def utils = new io.fabric8.Utils()

    //sfCheckout {}
    //def buildVersion = sfMavenBuildRelease {}
    //sfMavenIntegrationTest {}
    //sfDeployStaging {}

    // resolve project name
    // get version / input / last version
    // get proper kubernetes rc from nexus staging repo
    //  config.version = "0.${env.BUILD_NUMBER}"

    // sfApprove {
    //   version = buildVersion
    // }

    // deploy kubernetes rc

    if ( !config.name ) {
    	config.name = env.JOB_NAME.replace "production-",""
    }
    if ( !config.version ) {
    	config.version = "${env.VERSION}"
    }
    if ( !config.image ) {
		// default full image name including registry
		config.image = "${env.FABRIC8_DOCKER_REGISTRY_SERVICE_HOST}:${env.FABRIC8_DOCKER_REGISTRY_SERVICE_PORT}/${utils.getNamespace()}/${config.name}:${config.version}"
    }
    if ( !config.rcRepo ) {
	    config.rcRepo = "http://nexus/content/repositories/staging/"
    }
    if ( !config.rcPath ) {
    	config.rcPath = "com/stayfriends/activitystream" // TODO from pom
    }
    if ( !config.rcFile ) {
    	config.rcUrl = "${config.name}-${config.version}-kubernetes.yaml"
	}
    if ( !config.rcUrl ) {
    	config.rcUrl = "${config.rcRepo}/${config.rcPath}/${config.rcFile}"
	}

    echo "deploy config = " + config

    sh "wget ${config.rcUrl}"
    sh "ls -al"

 //    //container(name: 'client') {
	// stage 'Rollout Production'
	// 	def envNamespace = utils.environmentNamespace('production')
	// 	echo "deploying to environment: " + envNamespace

	// 	// get rc from nexus repo

	// 	// this file is read as default, as it is produced by maven plugin f-m-p
	// 	rcName = "target/classes/META-INF/fabric8/kubernetes.yml"
	// 	rc = ""
	// 	if ( fileExists(rcName) ) {
	// 		rc = readFile file: rcName
	// 	} else {
	// 		// generate default resources
	// 		rc = sfKubernetesResourceWebapp {
	// 			name = config.name
	// 			group = config.group
	// 			version = config.version
	// 			port = 80
	// 			image = config.image
	// 			icon = "https://cdn.rawgit.com/fabric8io/fabric8/dc05040/website/src/images/logos/nodejs.svg"
	// 		}
	// 	}

	// 	echo "applying kubernetes rc: " + rc
	// 	kubernetesApply(file: rc, environment: envNamespace)
	// 	//sh "kubectl apply -f target/classes/META-INF/fabric8/kubernetes.yml"
	// //}
}
