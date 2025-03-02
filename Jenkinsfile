#!/usr/bin/env groovy

pipeline {

    agent any

    tools {
        jdk "jdk-17.0.1"
    }

    options {
        // Sometimes builds freeze, but this doesn't have to be super aggressive.
        timeout(time: 30, unit: 'MINUTES')
    }

    parameters {
        booleanParam(name: 'RELEASE', defaultValue: false, description: 'Publish artifacts without a build number.')
    }

    stages {

        stage('Setup') {

            steps {

                echo 'Setup Project'
                sh 'chmod +x gradlew'
                sh './gradlew clean'
            }
        }

        stage('Build') {

            environment {
                RELEASE="${params.RELEASE}"
            }

            steps {
                withCredentials([
                    // build_secrets is parsed in SubprojectExtension#loadSecrets
                    file(credentialsId: 'build_secrets', variable: 'ORG_GRADLE_PROJECT_secretFile'),
                ]) {
                    echo 'Building project.'
                    sh './gradlew build publish --stacktrace --warn'
                }
            }
        }
    }

    post {

        always {

            archiveArtifacts artifacts: '**/build/libs/**/*.jar', fingerprint: true

            withCredentials([
                    string(credentialsId: 'discord_webhook_url', variable: 'DISCORD_URL')
            ]) {
                echo 'Notifying Discord..'
                discordSend description: "Build: #${currentBuild.number}", link: env.BUILD_URL, result: currentBuild.currentResult, title: env.JOB_NAME, webhookURL: env.DISCORD_URL, showChangeset: true, enableArtifactsList: true
            }
        }
    }
}
