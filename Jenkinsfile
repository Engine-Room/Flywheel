#!/usr/bin/env groovy

pipeline {
    agent none

    options {
        // After the Build stage the pipeline stalls, waiting for user input to confirm the release stage.
        // This leaves the builds in a running state even though no work is being done. Enable this option
        // to automatically cancel earlier stalled builds when a new one is started.
        disableConcurrentBuilds(abortPrevious: true)
    }

    tools {
        jdk "jdk-21"
    }

    stages {
        stage('Build') {
            agent any

            steps {
                echo 'Setup project.'
                sh 'chmod +x gradlew'
                sh './gradlew clean'

                withCredentials([
                    // build_secrets is parsed in SubprojectExtension#loadSecrets
                    file(credentialsId: 'build_secrets', variable: 'ORG_GRADLE_PROJECT_secretFile'),
                ]) {
                    echo 'Building project.'
                    // Sometimes builds freeze, so wrap in a timeout.
                    timeout(time: 30, unit: 'MINUTES') {
                        sh './gradlew build publish --stacktrace --warn'
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

        stage('Release') {
            // Take the input in our when block so that we don't block any agents.
            when {
                expression {
                    input(message: 'Publish without build number?', ok: 'Yes', cancel: 'No')
                    // If input is cancelled the entire step will abort. Return true so we continue on confirmation.
                    return true
                }
                beforeAgent true
            }

            agent any

            environment {
                RELEASE="true"
            }

            steps {
                // Prevent older builds from being released.
                milestone(ordinal: 1, label: 'Release Guardian')

                echo 'Setup project for release.'
                sh 'chmod +x gradlew'

                // Clean again because the agent may have changed.
                sh './gradlew clean'

                withCredentials([
                    // build_secrets is parsed in SubprojectExtension#loadSecrets
                    file(credentialsId: 'build_secrets', variable: 'ORG_GRADLE_PROJECT_secretFile'),
                ]) {
                    echo 'Building project for release.'
                    // Sometimes builds freeze, so wrap in a timeout.
                    timeout(time: 30, unit: 'MINUTES') {
                        sh './gradlew build publish --stacktrace --warn'
                    }
                }

                milestone(ordinal: 2, label: 'Release')
            }
        }
    }
}
