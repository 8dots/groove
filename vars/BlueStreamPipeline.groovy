#!/usr/bin/env groovy
import hudson.model.*
def call(label) {
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
    node(label) {
      def p = pipelineCfg()
      if (p.runTests == true) {
        stage('Unit-Testing') {
          container('jenkins-build-slave') {
            withCredentials([string(credentialsId: 'ACRUSER', variable: 'ACRUSER'), string(credentialsId: 'ACRPASS', variable: 'ACRPASS'), string(credentialsId: 'BS_RMQ_SERVER', variable: 'RMQ_SERVER'), string(credentialsId: 'BS_DB_SERVER', variable: 'DB_SERVER')]) {    
              checkout scm
              GitShortCommit = sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
              sh "docker login bluehub.azurecr.io -u $ACRUSER -p $ACRPASS"
              sh "docker build -t bluehub.azurecr.io/${p.repoName}:${GitShortCommit} ."
              sh "docker run --env DB_SERVER=$DB_SERVER --env RMQ_HOST=$RMQ_SERVER bluehub.azurecr.io/${p.repoName}:${GitShortCommit} npm test"
            }
          }
        }
      }
      if (env.BRANCH_NAME == 'master' && p.deployUponTestSuccess == true) {
        stage('Deploy') {
            sh "echo deploy"
        }
      }
    }
  }
}