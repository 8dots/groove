#!/usr/bin/env groovy
import hudson.model.*
jenkins = Jenkins.instance

def call() {
  node('jenkins-build-slave') {
    stage ('checkout') {
      checkout scm
    }
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