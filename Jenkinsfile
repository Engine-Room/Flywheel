import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException
import org.jenkinsci.plugins.workflow.support.steps.input.Rejection

pipeline {
    agent none

    options {
        // After the Build stage the pipeline stalls, waiting for user input to confirm the release stage.
        // This leaves the builds in a running state even though no work is being done. Enable this option
        // to automatically cancel earlier stalled builds when a new one is started.
        disableConcurrentBuilds(abortPrevious: true)
    }

    tools {
        jdk "jdk-25"
    }

    stages {
        stage('Build') {
            agent any

            steps {
                echo 'Setup project.'
                sh 'chmod +x gradlew'
                sh './gradlew clean'

                withCredentials([
                    usernamePassword(credentialsId: 'maven_secrets', usernameVariable: 'MAVEN_USERNAME', passwordVariable: 'MAVEN_PASSWORD')
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

        stage('Approve Release') {
            agent none

            steps {
                script {
                    env.RELEASE_APPROVED = 'false'
                    try {
                        input(message: 'Publish without build number?', ok: 'Yes', cancel: 'No')
                        env.RELEASE_APPROVED = 'true'
                    } catch (FlowInterruptedException e) {
                        // only catch the error emitted by "No"
                        if (!e.causes.any { it instanceof Rejection }) {
                             throw e
                        }
                        echo 'Skipping release.'
                    }
                }
            }
        }

        stage('Release') {
            when {
                expression { env.RELEASE_APPROVED == 'true' }
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
                    usernamePassword(credentialsId: 'maven_secrets', usernameVariable: 'MAVEN_USERNAME', passwordVariable: 'MAVEN_PASSWORD')
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
