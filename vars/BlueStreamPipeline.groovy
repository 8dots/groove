#!/usr/bin/env groovy
import hudson.model.*
jenkins = Jenkins.instance

def call() {
  node('jenkins-build-slave') {
    stage('checkout') {
      checkout scm
    }
    def p = pipelineCfg()
    if (p.runTests == true) {
      stage('Unit-Testing') {
        container('jenkins-build-slave') {
          withCredentials([
              string(credentialsId: 'ACRUSER', variable: 'ACRUSER'), 
              string(credentialsId: 'S3_ENDPOINT', variable: 'S3_ENDPOINT'), 
              string(credentialsId: 'ACRPASS', variable: 'ACRPASS'), 
              string(credentialsId: 'S3_SECRET_ACCESS_KEY', variable: 'S3_SECRET_ACCESS_KEY'), 
              string(credentialsId: 'S3_ACCESS_KEY_ID', variable: 'S3_ACCESS_KEY_ID'), 
              string(credentialsId: 'BS_RMQ_SERVER', variable: 'RMQ_SERVER'),
              string(credentialsId: 'ACR_ENDPOINT', variable: 'ACR_ENDPOINT'), 
              string(credentialsId: 'BS_DB_SERVER', variable: 'DB_SERVER'),
              string(credentialsId: 'S3_REGION', variable: 'S3_REGION'),
              string(credentialsId: 'S3_VERSION', variable: 'S3_VERSION'),
              string(credentialsId: 'BS_RMQ_SERVER', variable: 'RMQ_LOGGER_HOST'),
              string(credentialsId: 'S3_BUCKET', variable: 'S3_BUCKET')
              ]) {    
            checkout scm
            GitShortCommit = sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
            sh "docker login bluehub.azurecr.io -u $ACRUSER -p $ACRPASS"
            sh "docker build -t bluehub.azurecr.io/${p.repoName}:${GitShortCommit} ."
            sh "docker run --env S3_BUCKET=$S3_BUCKET --env RMQ_LOGGER_HOST=$RMQ_LOGGER_HOST --env S3_VERSION=$S3_VERSION --env S3_REGION=$S3_REGION --env S3_ENDPOINT=$S3_ENDPOINT --env S3_ACCESS_KEY_ID=$S3_ACCESS_KEY_ID --env S3_SECRET_ACCESS_KEY=$S3_SECRET_ACCESS_KEY --env DB_SERVER=$DB_SERVER --env RMQ_HOST=$RMQ_SERVER $ACR_ENDPOINT/${p.repoName}:${GitShortCommit} npm test"
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