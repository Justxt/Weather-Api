def pipelineUtils

pipeline {
    agent none

    options {
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        timestamps()
    }

    environment {
        MAVEN_IMAGE = 'maven:3.9.9-eclipse-temurin-21'
    }

    stages {
        stage('Checkout') {
            agent any
            steps {
                checkout scm
                script {
                    pipelineUtils = load('jenkins/WeatherPipeline.groovy')
                    env.PIPELINE_BRANCH = pipelineUtils.normalizeBranch(
                        env.CHANGE_BRANCH ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'main'
                    )
                    env.PIPELINE_BRANCH_TYPE = pipelineUtils.branchLabel(env.PIPELINE_BRANCH)
                }

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
