#!/usr/bin/groovy

// Create Kubernetes resource description for a generic web application,
// including deployment and service descriptions.

def call(body) {

    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    echo "k8s resource config = " + config

    if ( !config.name ) error("name is null")
    if ( !config.version ) error("version is null")

    // json must start with "{"
    def rc = """{
      "apiVersion" : "v1",
      "kind" : "List",
      "items" : [{
        "kind": "Service",
        "apiVersion": "v1",
        "metadata": {
            "name": "${config.name}",
            "creationTimestamp": null,
            "labels": {
                "group": "${config.group}",
                "project": "${config.name}",
                "provider": "stayfriends",
                "expose": "true",
                "version": "${config.version}"
            },
            "annotations": {
                "fabric8.${config.name}/iconUrl" : "${config.icon}"
            }
        },
        "spec": {
            "ports": [
                {
                    "protocol": "TCP",
                    "port": 80,
                    "targetPort": ${config.port}
                }
            ],
            "selector": {
                "project": "${config.name}"
            },
            "type": "LoadBalancer"
        }
    },
    {
        "kind": "Deployment",
        "apiVersion": "extensions/v1beta1",
        "metadata": {
            "name": "${config.name}",
            "creationTimestamp": null,
            "labels": {
                "group": "${config.group}",
                "project": "${config.name}",
                "provider": "stayfriends",
                "version": "${config.version}"
            },
            "annotations": {
                "fabric8.${config.name}/iconUrl" : "${config.icon}"
            }
        },
        "spec": {
            "replicas": 1,
            "selector": {
                "matchLabels": {
                    "project": "${config.name}"
                }
            },
            "template": {
                "metadata": {
                    "creationTimestamp": null,
                    "labels": {
                        "group": "${config.group}",
                        "project": "${config.name}",
                        "provider": "stayfriends",
                        "version": "${config.version}"
                    }
                },
                "spec": {
                    "containers": [
                        {
                            "name": "${config.name}",
                            "image": "${config.image}",
                            "ports": [
                                {
                                    "name": "http",
                                    "containerPort": ${config.port},
                                    "protocol": "TCP"
                                }
                            ],
                            "env": [
                                {
                                    "name": "KUBERNETES_NAMESPACE",
                                    "valueFrom": {
                                        "fieldRef": {
                                            "apiVersion": "v1",
                                            "fieldPath": "metadata.namespace"
                                        }
                                    }
                                }
                            ],
                            "resources": {},
                            "imagePullPolicy": "IfNotPresent",
                            "securityContext": {}
                        }
                    ],
                    "securityContext": {}
                }
            }
        }
    }]}
"""

    echo 'using Kubernetes resources:\n' + rc
    return rc

}
