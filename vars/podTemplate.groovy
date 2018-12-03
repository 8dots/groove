#!/usr/bin/env groovy
import hudson.model.*

def call(String label, code) {
  podTemplate(label: label, yaml:
  """
  apiVersion: v1
  kind: Pod
  spec:
  containers:
  - name: jenkins-build-slave
      image: adobeplatform/jenkins-dind
      command: ['cat']
      tty: true
      env: 
      - name: DOCKER_HOST 
        value: tcp://localhost:2375 
  - name: dind-daemon 
      image: docker:dind 
      securityContext: 
        privileged: true 
      volumeMounts: 
        - name: docker-graph-storage 
          mountPath: /var/lib/docker 
  volumes: 
    - name: docker-graph-storage 
      emptyDir: {}
  """) {
  code() 
  }
}  
