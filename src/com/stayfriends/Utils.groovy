#!/usr/bin/groovy
package com.stayfriends

import com.cloudbees.groovy.cps.NonCPS

@NonCPS
def buildHelm(config) {
  KubernetesClient kubernetes = new DefaultKubernetesClient();
  return kubernetes.getNamespace() + "-${environment}"
}

@NonCPS
def getNamespace() {
  KubernetesClient kubernetes = new DefaultKubernetesClient();
  return kubernetes.getNamespace()
}

return this;
