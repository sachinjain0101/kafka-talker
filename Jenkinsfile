// Need the line below to tell gitlab plugin where to send commit/build status
properties([[$class: 'GitLabConnectionProperty', gitLabConnection: 'BHSource']])

node('v2_builder') {
    String BRANCH = env.BRANCH_NAME.replaceFirst(/^.*\//, '')
    def URL = env.JOB_URL
    def ERROR_MESSAGE = "[chronos] Build FAILED for branch $BRANCH, see $URL for details."
    def SUCCESS_MESSAGE = "[chronos] Build SUCCESS for branch $BRANCH"
    def SLACK_TOKEN = 'Y8ut7kD8Cinc55TSFEIhBZkz'

    try {
        timeout(10) {
            stage('Checkout') {
                checkout scm
                def mavenHome = tool 'Maven 3.0.5'
                def javaHome = tool 'JDK Latest 1.8'
                gitlabCommitStatus('Build') {
                    withEnv(["PATH+MAVEN=${mavenHome}/bin", "PATH+JAVA=${javaHome}/bin"]) {
                        stage('Build and Publish') {
                            if (BRANCH == 'master') {
                                // deploy version as set in pom.xml
                                sh 'mvn -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true clean deploy'
                            }
                            // deploy {branch}-SNAPSHOT version
                            sh "mvn versions:set -DnewVersion=$BRANCH-SNAPSHOT"
                            sh 'mvn -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true clean deploy'
                            slackSend channel: '#q-bits-private', color: 'green', message: SUCCESS_MESSAGE, teamDomain: 'bullhorn', token: SLACK_TOKEN
                        }
                    }
                }
            }
        }
    } catch(err) {
        slackSend channel: '#q-bits-private', color: 'danger', message: ERROR_MESSAGE, teamDomain: 'bullhorn', token: SLACK_TOKEN
        throw err
    }
}

