#!/usr/bin/env groovy
import hudson.model.*

def call() {
  node {
    stage('Checkout') {
      checkout scm
    }
    def p = pipelineCfg()

    if (p.runTests == true) {
      stage('Unit-Testing') {
        container('jenkins-build-slave') {
          withCredentials([string(credentialsId: 'ACRUSER', variable: 'ACRUSER'), string(credentialsId: 'ACRPASS', variable: 'ACRPASS'), string(credentialsId: 'BS_RMQ_SERVER', variable: 'RMQ_SERVER'), string(credentialsId: 'BS_DB_SERVER', variable: 'DB_SERVER')]) {    
            sh "docker login bluehub.azurecr.io -u $ACRUSER -p $ACRPASS"
            sh "docker build -t bluehub.azurecr.io/${repoName}:${GIT_SHORT_COMMIT} ."
            sh "docker run --env DB_SERVER=$DB_SERVER --env RMQ_HOST=$RMQ_SERVER bluehub.azurecr.io/${repoName}:${GIT_SHORT_COMMIT} npm test"
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