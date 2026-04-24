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
        stage('Preparacion') {
            agent {
                docker {
                    image "${MAVEN_IMAGE}"
                    reuseNode true
                }
            }
            steps {
                checkout scm
                script {
                    pipelineUtils = load('jenkins/WeatherPipeline.groovy')
                    env.PIPELINE_BRANCH = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'local'
                    env.PIPELINE_BRANCH_TYPE = pipelineUtils.branchLabel(env.PIPELINE_BRANCH)
                }
                echo "Rama detectada: ${env.PIPELINE_BRANCH}"
                echo "Tipo de pipeline: ${env.PIPELINE_BRANCH_TYPE}"
                script {
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
                            reuseNode true
                        }
                    }
                    steps {
                        checkout scm
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

                stage('Calidad') {
                    agent {
                        docker {
                            image "${MAVEN_IMAGE}"
                            reuseNode true
                        }
                    }
                    steps {
                        checkout scm
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
                    reuseNode true
                }
            }
            steps {
                checkout scm
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
            echo 'El pipeline termino correctamente.'
        }
        failure {
            echo 'El pipeline fallo. Revisa pruebas, calidad o compilacion.'
        }
    }
}
