#!/usr/bin/groovy

def call(body) {

    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    if ( !config.env ) {
    	config.env = "Staging"
    }
    if ( !config.version ) {
    	error "No version set for approval"
    }

    def utils = new io.fabric8.Utils()
	def fabric8Console = "${env.FABRIC8_CONSOLE ?: ''}"

	stage 'Approve'
	approve {
		room = null
		version = config.version
		console = fabric8Console
		environment =  config.env
	}

}
