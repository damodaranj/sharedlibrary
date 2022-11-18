def call(body){
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    config = pipelineParams
    body()
    projectname = getprojectName()
    branch = getbranchName()
    credid = getcredId()
    dockerurl = getImagename()
    projecturl = "https://github.com/" + "${projectname}" + ".git"
    echo "${projecturl}"
    
    pipeline {
      environment {
        imagename = "${dockerurl}"
        registryCredential = 'jenkins-dockerhub'
        dockerImage = ''
      }
     agent any
     stages {
      stage('Cloning Git') {
       steps {
           git credentialsId: "${credid}", url: "${projecturl}" 

       }
     }
     stage('Building image') {
      steps{
        script {
          dockerImage = docker.build imagename
        }
      }
     }
     stage('Deploy Image') {
      steps{
        script {
          docker.withRegistry( '', registryCredential ) {
            dockerImage.push("$BUILD_NUMBER")
             dockerImage.push('latest')

           }
         }
       }
     }
    stage('Remove Unused docker image') {
      steps{
         sh "docker rmi $imagename:$BUILD_NUMBER"
         sh "docker rmi $imagename:latest"

      }
    }
  }
}
}

def getprojectName(){
    return config.projectname
}
def getbranchName() {
    return config.branchname
}

def getcredId(){
  return config.credid
}

def getImagename(){
  return config.imageid
}
