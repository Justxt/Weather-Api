def pipelineUtils

pipeline {
    agent none

    options {
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        timestamps()
    }

    parameters {
        string(name: 'REPO_URL', defaultValue: 'https://github.com/Justxt/Weather-Api.git', description: 'Repositorio Git a construir')
        string(name: 'SCM_BRANCH', defaultValue: 'main', description: 'Rama a ejecutar cuando no existe BRANCH_NAME')
        string(name: 'GIT_CREDENTIALS_ID', defaultValue: '', description: 'Credencial Jenkins opcional para repositorios privados')
    }

    environment {
        MAVEN_IMAGE = 'maven:3.9.9-eclipse-temurin-21'
    }

    stages {
        stage('Checkout') {
            agent any
            steps {
                script {
                    def requestedBranch = env.CHANGE_BRANCH ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: params.SCM_BRANCH
                    def normalizedBranch = requestedBranch
                        .replace('refs/heads/', '')
                        .replace('origin/', '')
                        .replaceFirst('^\\*/', '')

                    env.PIPELINE_BRANCH = normalizedBranch
                    env.REPO_TO_BUILD = params.REPO_URL?.trim() ?: 'https://github.com/Justxt/Weather-Api.git'

                    def remoteConfig = [url: env.REPO_TO_BUILD]
                    if (params.GIT_CREDENTIALS_ID?.trim()) {
                        remoteConfig.credentialsId = params.GIT_CREDENTIALS_ID.trim()
                    }

                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "*/${normalizedBranch}"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [
                            [$class: 'CloneOption', depth: 1, noTags: false, shallow: true]
                        ],
                        userRemoteConfigs: [remoteConfig]
                    ])

                    pipelineUtils = load('jenkins/WeatherPipeline.groovy')
                    env.PIPELINE_BRANCH_TYPE = pipelineUtils.branchLabel(env.PIPELINE_BRANCH)
                }

                echo "Repositorio: ${env.REPO_TO_BUILD}"
                echo "Rama detectada: ${env.PIPELINE_BRANCH}"
                echo "Tipo de pipeline: ${env.PIPELINE_BRANCH_TYPE}"
                stash name: 'workspace-source', includes: '**/*', useDefaultExcludes: false
            }
        }

        stage('Compilacion') {
            agent {
                docker {
                    image "${MAVEN_IMAGE}"
                    args '--entrypoint="" -v /var/jenkins_home/.m2:/root/.m2'
                    reuseNode true
                }
            }
            steps {
                unstash 'workspace-source'
                script {
                    pipelineUtils = load('jenkins/WeatherPipeline.groovy')
                    pipelineUtils.mvnw('-B -ntp clean compile')
                }
            }
        }

        stage('Validacion') {
            parallel {
                stage('Tests') {
                    agent {
                        docker {
                            image "${MAVEN_IMAGE}"
                            args '--entrypoint="" -v /var/jenkins_home/.m2:/root/.m2'
                            reuseNode true
                        }
                    }
                    steps {
                        unstash 'workspace-source'
                        script {
                            pipelineUtils = load('jenkins/WeatherPipeline.groovy')
                            pipelineUtils.mvnw('-B -ntp test')
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                        }
                    }
                }

                stage('Calidad de codigo') {
                    agent {
                        docker {
                            image "${MAVEN_IMAGE}"
                            args '--entrypoint="" -v /var/jenkins_home/.m2:/root/.m2'
                            reuseNode true
                        }
                    }
                    steps {
                        unstash 'workspace-source'
                        script {
                            pipelineUtils = load('jenkins/WeatherPipeline.groovy')
                            pipelineUtils.mvnw('-B -ntp checkstyle:check')
                        }
                    }
                }
            }
        }

        stage('Build') {
            when {
                expression {
                    return env.PIPELINE_BRANCH_TYPE == 'protected'
                }
            }
            agent {
                docker {
                    image "${MAVEN_IMAGE}"
                    args '--entrypoint="" -v /var/jenkins_home/.m2:/root/.m2'
                    reuseNode true
                }
            }
            steps {
                unstash 'workspace-source'
                script {
                    pipelineUtils = load('jenkins/WeatherPipeline.groovy')
                    pipelineUtils.mvnw('-B -ntp -DskipTests package')
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finalizado para la rama ${env.PIPELINE_BRANCH ?: 'desconocida'}."
        }
        success {
            echo 'Pipeline completado correctamente.'
        }
        failure {
            echo 'Pipeline fallido. Revisa compilacion, pruebas o calidad.'
        }
    }
}
