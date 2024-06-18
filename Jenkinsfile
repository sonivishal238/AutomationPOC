pipeline {
    agent any

    environment {
        REPO_URL = "https://github.com/sonivishal238/AutomationPOC.git"
        BRANCH_NAME = "featurepoc2/nswag-update-${env.JOB_NAME}-${env.BUILD_ID}"
    }

    triggers {
        GenericTrigger(
            genericVariables: [
                [key: 'action', value: '$.action'],
                [key: 'merged', value: '$.pull_request.merged'],
                [key: 'repository', value: '$.repository.full_name'],
                [key: 'branch', value: '$.pull_request.base.ref']
            ],
            causeString: 'Triggered by PR merge on $repository',
            token: 'nswag',
            printContributedVariables: true,
            printPostContent: true,
            regexpFilterText: '$action $merged',
            regexpFilterExpression: '^closed true$'
        )
    }

    stages {
        stage('Log Environment Variables and Parameters') {
            steps {
                script {
                    echo "Action: ${env.action}"
                    echo "Merged: ${env.merged}"
                    echo "Repository: ${env.repository}"
                    echo "Branch: ${env.branch}"
                }
            }
        }

        stage('Clean Workspace') {
            steps {
                cleanWs()
                echo "Workspace cleaned up."
            }
        }

        stage('Checkout Code') {
            steps {
                script {
                    bat "git clone ${env.REPO_URL} ."
                    bat "git checkout main"
                    echo "Checked out code from ${env.REPO_URL}."
                }
            }
        }

        stage('Read Configuration') {
            steps {
                script {
                    // Read the YAML configuration file from the workspace
                    echo "Read Configuration stage trigered"
                    def configFile = readYaml file: "service_config"
                    echo configFile
                    def repoName = env.repository.split('/')[1]
                    echo repoName 
                    def serviceConfig = configFile.services[repoName]

                    if (serviceConfig) {
                        env.SERVICE_NAME = serviceConfig.service_name
                        echo "Service Name: ${env.SERVICE_NAME}"
                    } else {
                        error "Service configuration for repository '${repoName}' not found in service_config.yml!"
                    }
                }
            }
        }

        stage('Create Branch') {
            steps {
                script {
                    bat "git checkout -b ${env.BRANCH_NAME}"
                    echo "Created and checked out new branch ${env.BRANCH_NAME}."
                }
            }
        }

        stage('Setup NSwag') {
            steps {
                script {
                    bat '''
                    if not exist %USERPROFILE%\\.dotnet\\tools\\nswag.exe (
                        dotnet tool install --global NSwag.ConsoleCore
                    )
                    '''
                    echo "NSwag is installed and ready."
                }
            }
        }
        
        stage('Generate NSwag Client') {
            steps {
                script {
                    def nswagCommand = "nswag openapi2csclient /input:${params.SWAGGER_URL} /namespace:VishalUserActions.APIs.${env.SERVICE_NAME} /className:${env.SERVICE_NAME}Api /generateExceptionClasses:false /exceptionClass:VishalUserActions.VishalApiException /output:VishalUserActions\\NSwagGeneratedAPI\\${env.SERVICE_NAME}Api.cs"
                    bat nswagCommand
                    echo "Generated NSwag client for ${env.SERVICE_NAME}."
                }
            }
        }

        stage('Build Verification') {
            steps {
                script {
                    bat 'dotnet build'
                    echo "Build verification completed successfully."
                }
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    bat 'dotnet test'
                    echo "Tests ran successfully."
                }
            }
        }

        stage('Commit and Push Changes') {
            steps {
                script {
                    bat 'git add .'
                    bat 'git commit -m "Auto-generated NSwag client update"'
                    bat "git push --set-upstream origin ${env.BRANCH_NAME}"
                    echo "Pushed new branch ${env.BRANCH_NAME} to the remote repository."
                    echo """
                    The NSwag client for ${env.SERVICE_NAME} has been generated and pushed to branch ${env.BRANCH_NAME}.
                    Please create a pull request to merge this branch into the main branch.

                    Branch: ${env.BRANCH_NAME}
                    Repository: ${env.REPO_URL}
                    """
                }
            }
        }

        stage('Create Pull Request') {
            steps {
                script {
                    bat """
                        git status
                        gh pr create --title "Trying to create PR through pipeline" --body "PR through CMD part 2" --head ${env.BRANCH_NAME}
                        """
                    echo "Created pull request for branch ${env.BRANCH_NAME}."
                }
            }
        }
    }

    post {
        always {
            cleanWs()
            echo "Workspace cleaned up."
        }
    }
}
