#!/usr/bin/groovy
def call(String channel, body) {
  // the start notification of the pipeline
  notifyBuild(channel)
  // call the inner pipeline
  try {
    body()
    notifyBuild(channel, currentBuild.result)
  } catch (e) {
    notifyBuild(channel, 'FAILURE', e.message)
  }
}

def notifyBuild(String channel, String buildStatus = 'STARTED', String reason = '') {
  // build status of null means successful
  buildStatus =  buildStatus ?: 'SUCCESSFUL'

  // Default values
  def colorName = 'RED'
  def colorCode = '#dc3545'
  def subject = "${env.JOB_NAME} - #${env.BUILD_NUMBER}"
  def summary = "${subject} ${buildStatus} (<${env.BUILD_URL}|Open>)"
  if (reason != '') {
    summary = "${summary}\n${reason}"
  }

  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = 'YELLOW'
    colorCode = '#ffc107'
  } else if (buildStatus == 'SUCCESSFUL') {
    color = 'GREEN'
    colorCode = '#28a745'
  } else {
    color = 'RED'
    colorCode = '#dc3545'
  }

  // Send notifications
  slackSend (color: colorCode, message: summary, channel: channel)
}