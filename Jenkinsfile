#!/usr/bin/env groovy

pipeline {

    agent any

    tools {
        jdk "jdk-17.0.1"
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

            steps {

                withCredentials([
                    file(credentialsId: 'build_secrets', variable: 'ORG_GRADLE_PROJECT_secretFile'),
                    //file(credentialsId: 'java_keystore', variable: 'ORG_GRADLE_PROJECT_keyStore'),
                    file(credentialsId: 'gpg_key', variable: 'ORG_GRADLE_PROJECT_pgpKeyRing')
                ]) {

                    echo 'Building project.'
                    sh './gradlew build publish --stacktrace --warn'
                }
            }
        }
    }

    post {

        always {

            archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true

            withCredentials([
                    string(credentialsId: 'discord_webhook_url', variable: 'DISCORD_URL')
            ]) {
                echo 'Notifying Discord..'
                discordSend description: "Build: #${currentBuild.number}", link: env.BUILD_URL, result: currentBuild.currentResult, title: env.JOB_NAME, webhookURL: env.DISCORD_URL, showChangeset: true, enableArtifactsList: true
            }
        }
    }
}
