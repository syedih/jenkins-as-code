pipeline{
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '100', daysToKeepStr: '30'))
        disableConcurrentBuilds()
        timestamps()
    }

    parameters{
        choice(name: 'EB_Action',  choices: ['deploy','create'], description: 'deploy : Deploys to existing environment <br> create :  Creates a new environment ')
    }
    stages{
        stage("Pull Code From github"){
            steps{
                cleanWs()
                dir('scheduler-scripts'){
                    checkout([$class: 'GitSCM', branches: [[name: scripts_branch ]],
                              userRemoteConfigs: [[
                                                          credentialsId: 'askmas_id',
                                                          url: 'https://github.com/askmas/tutorials1' ]]
                    ])
                }

            }
        }

        stage("Build Docker image"){
            steps{
                script{
                    sh 'docker build -t masoodmjan/tutorials .'
                }
            }
        }


        stage("Push Image to Docker Hub"){
            steps{
                script{
                    sh '  cat /my_pass.txt | docker login --username=masoodmjan --password-stdin '
                    sh ' docker push masoodmjan/tutorials'
                }
            }
        }

        stage("Deploy To AWS ebs"){
            steps{
                script{
                    sh   'cd ElasticBeanstalk\n' +
                          '. ./BuildAndDeployDockerEBs.sh'

                }
            }
        }
    }
}
