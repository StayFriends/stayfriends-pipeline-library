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
		config.rcName = "target/classes/META-INF/fabric8/kubernetes.yml"
    }
    echo "deploy config = " + config

    //container(name: 'client') {

	stage 'Deploy Staging' 
		def envStage = utils.environmentNamespace('staging')
		echo "deploying to environment: " + envStage

		// this file is read as default, as it is produced by maven plugin f-m-p
		rc = ""
		if ( fileExists(config.rcName) ) {
			rc = readFile file: config.rcName
		} else {
			// alternative is for frontend project to generate the resource descriptions
   //    		withEnv(["KUBERNETES_NAMESPACE=${utils.getNamespace()}"]) {
   //      		rc = getKubernetesJson {
   //        			port = 80
			// 		label = 'nginx'
			// 		icon = 'https://cdn.rawgit.com/fabric8io/fabric8/dc05040/website/src/images/logos/nodejs.svg'
			// 		version = config.version
			//     }
			// }

			rc = sfKubernetesResourceWebapp {
				name = config.name
				group = config.group
				version = config.version
				port = 80
				image = config.image
				icon = "https://cdn.rawgit.com/fabric8io/fabric8/dc05040/website/src/images/logos/nodejs.svg"
			}
		}

		echo "applying kubernetes rc: " + rc
		kubernetesApply(file: rc, environment: envStage)
		//sh "kubectl apply -f target/classes/META-INF/fabric8/kubernetes.yml"
	//}
}
