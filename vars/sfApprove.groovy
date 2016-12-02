#!/usr/bin/groovy

def call(body) {

    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    if ( !config.name ) {
    	config.name = env.JOB_NAME
    }
    if ( !config.environment ) {
    	config.environment = "staging"
    }
    if ( !config.version ) {
    	error "No version set for approval"
    }
    if ( !config.proceedMessage ) {
      config.proceedMessage = """
Would you like to promote ${config.name} ${config.version} to the ${config.environment} namespace?
"""
    }

    def utils = new io.fabric8.Utils()

	stage 'Approve'

    //hubotApprove message: config.proceedMessage, room: config.room
    def id = approveRequestedEvent(app: "${env.JOB_NAME}", environment: config.environment)

    try {
      input id: 'Proceed', message: "\n${config.proceedMessage}"
    } catch (err) {
      approveReceivedEvent(id: id, approved: false)
      throw err
    }
    approveReceivedEvent(id: id, approved: true)

}
