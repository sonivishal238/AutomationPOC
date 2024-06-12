def call(Map config = [:]) {
    pipeline {
        agent any

        parameters {
            string(name: 'SERVICE_NAME', defaultValue: config.serviceName ?: 'TestRestService', description: 'Name of the service')
            string(name: 'SWAGGER_URL', defaultValue: config.swaggerUrl ?: 'https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json', description: 'URL to the swagger.json file')
        }

        environment {
            REPO_URL = "https://github.com/sonivishal238/AutomationPOC.git"
            BRANCH_NAME = "featurepoc2/nswag-update-${env.JOB_NAME}-${env.BUILD_ID}"
        }

        stages {
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
                        def nswagCommand = "nswag openapi2csclient /input:${params.SWAGGER_URL} /namespace:VishalUserActions.APIs.${params.SERVICE_NAME} /className:${params.SERVICE_NAME}Api /generateExceptionClasses:false /exceptionClass:VishalUserActions.VishalApiException /output:VishalUserActions\\NSwagGeneratedAPI\\${params.SERVICE_NAME}Api.cs"
                        bat nswagCommand
                        echo "Generated NSwag client for ${params.SERVICE_NAME} using ${params.SWAGGER_URL}."
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
                        The NSwag client for ${params.SERVICE_NAME} has been generated and pushed to branch ${env.BRANCH_NAME}.
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
}
