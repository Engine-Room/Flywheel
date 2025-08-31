#!/usr/bin/env groovy

pipeline {
    agent none

    tools {
        jdk "jdk-17.0.1"
    }

    stages {
        stage('Build') {

            options {
                // Sometimes builds freeze, but this doesn't have to be super aggressive.
                timeout(time: 30, unit: 'MINUTES')
            }

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
                    sh './gradlew build publish --stacktrace --warn'
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
            when {
                expression {
                    input(message: 'Publish without build number?', ok: 'Yes', cancel: 'No')
                    // If input is cancelled the entire step will abort. Return true so we continue on confirmation.
                    return true
                }
                beforeAgent true
            }

            options {
                // Sometimes builds freeze, but this doesn't have to be super aggressive.
                timeout(time: 30, unit: 'MINUTES')
            }

            agent any

            environment {
                RELEASE="true"
            }

            steps {
                // Prevent older builds from being released.
                milestone(ordinal: 1, label: 'Release Guardian')

                echo 'Building for release.'
                echo '$RELEASE'
                echo './gradlew build publish --stacktrace --warn'

                milestone(ordinal: 2, label: 'Release')
            }
        }
    }
}
