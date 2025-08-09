pipeline {
  agent any

  environment {
    commit_hash = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
  }

  stages {
    stage('build') {
      steps {
        sh './mvnw clean package'
      }
    }
//
//     stage('tag') {
//       steps {
//         sh "docker build -t bankapp-demo:${env.commit_hash} ."
//         sh "docker tag bankapp-demo local/bankapp-demo:${env.commit_hash}"
//       }
//     }
//
//     stage('deploy') {
//       steps {
//         sh "kubectl create deployment spring-cicd-demo --image=local/spring-cicd-demo:${env.commit_hash} --port=8080"
//       }
//     }
  }
}