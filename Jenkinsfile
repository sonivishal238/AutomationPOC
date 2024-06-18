pipeline {
    agent any

    triggers {
        GenericTrigger(
            genericVariables: [
                [key: 'action', value: '$.action'],
                [key: 'merged', value: '$.pull_request.merged'],
                [key: 'repository', value: '$.repository.full_name'],
                [key: 'branch', value: '$.pull_request.base.ref'],
                [key: 'SERVICE_NAME', value: '$.pull_request.head.repo.name'],
                [key: 'SWAGGER_URL', value: '$.pull_request.head.repo.url']
            ],
            causeString: 'Triggered by PR merge on $repository',
            token: 'your-webhook-token',
            printContributedVariables: true,
            printPostContent: true,
            regexpFilterText: '$action $merged',
            regexpFilterExpression: '^closed true$'
        )
    }

    environment {
        REPO_URL = "https://github.com/sonivishal238/AutomationPOC.git"
        BRANCH_NAME = "featurepoc2/nswag-update-${env.JOB_NAME}-${env.BUILD_ID}"
    }

    stages {
        stage('Log Environment Variables and Parameters') {
            steps {
                script {
                    echo "Environment Variables:"
                    sh 'printenv'
                    echo "Parameters:"
                    echo "SERVICE_NAME: ${env.SERVICE_NAME}"
                    echo "SWAGGER_URL: ${env.SWAGGER_URL}"
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
                    def nswagCommand = "nswag openapi2csclient /input:${env.SWAGGER_URL} /namespace:VishalUserActions.APIs.${env.SERVICE_NAME} /className:${env.SERVICE_NAME}Api /generateExceptionClasses:false /exceptionClass:VishalUserActions.VishalApiException /output:VishalUserActions\\NSwagGeneratedAPI\\${env.SERVICE_NAME}Api.cs"
                    bat nswagCommand
                    echo "Generated NSwag client for ${env.SERVICE_NAME} using ${env.SWAGGER_URL}."
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
                    commitAndPushChanges()
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

def commitAndPushChanges() {
    bat 'git add .'
    bat 'git commit -m "Auto-generated NSwag client update"'
    bat "git push --set-upstream origin ${env.BRANCH_NAME}"
    echo "Pushed new branch ${env.BRANCH_NAME} to the remote repository."
    echo """
    The NSwag client for ${params.SERVICE_NAME} has been generated and pushed to branch ${env.BRANCH_NAME}.
    Please create a pull request to merge this branch into the main branch.

    Branch: ${env.BRANCH_NAME}
    Repository: ${env.REPO_URL}
    """
}
