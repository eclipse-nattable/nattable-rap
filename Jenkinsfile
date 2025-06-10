pipeline {
    agent {
        kubernetes {
            label 'migration'
        }
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    tools {
        maven 'apache-maven-latest'
        jdk 'openjdk-jdk21-latest'
    }
    environment {
        SNAPSHOTS_DIR = "/home/data/httpd/download.eclipse.org/nattable-rap/snapshots"
        SNAPSHOT_BUILD_DIR = "${SNAPSHOTS_DIR}/${BUILD_NUMBER}"
        SSH_HOST = "genie.nattable@projects-storage.eclipse.org"
    }
    stages {
        stage('Build') {
            steps {
                wrap([$class: 'Xvnc', takeScreenshot: false, useXauthority: true]) {
                  sh 'mvn clean compile javadoc:aggregate verify -Psign -B'
                }
            }
        }
        stage('Deploy') {
            when {
                allOf {
                    not {
                        changeRequest()
                    }
                    anyOf { branch 'releases/*'; branch 'master' }
                }
            }
            steps {
                sshagent(['projects-storage.eclipse.org-bot-ssh']) {
                    sh '''
                       echo "Deploying..."
                       # Copy from Jenkins to download.eclipse.org
                       ssh ${SSH_HOST} mkdir -p ${SNAPSHOT_BUILD_DIR}/repository
                       scp -r ${WORKSPACE}/org.eclipse.nebula.widgets.nattable.rap.updatesite/target/repository/* ${SSH_HOST}:${SNAPSHOT_BUILD_DIR}/repository
                       scp -r ${WORKSPACE}/target/site/apidocs ${SSH_HOST}:${SNAPSHOT_BUILD_DIR}

                       # ssh on download.eclipse.org
                       ssh ${SSH_HOST} << EOF

                       # create the .metadata file
                       echo ${BUILD_ID} > ${SNAPSHOT_BUILD_DIR}/.metadata

                       # Zip
                       cd ${SNAPSHOT_BUILD_DIR}/repository
                       zip -r ../repository.zip *

                       # Delete old latest snapshot
                       rm -rf ${SNAPSHOTS_DIR}/latest

                       # Delete old snapshot directories; only keep latest 5
                       ls -dt ${SNAPSHOTS_DIR}/*/ | tail -n +7 | xargs rm -rf

                       # Copy latest snapshot build to latest snapshot repository
                       cp -r ${SNAPSHOT_BUILD_DIR} ${SNAPSHOTS_DIR}/latest
                       << EOF
                       '''
                }
            }
        }
    }
    post {
        // send a mail on unsuccessful and fixed builds
        unsuccessful { // means unstable || failure || aborted
            emailext subject: 'Build $BUILD_STATUS $PROJECT_NAME #$BUILD_NUMBER!', 
            body: '''Check console output at $BUILD_URL to view the results.''',
            recipientProviders: [culprits(), requestor()], 
            to: 'nattable-dev@eclipse.org'
        }
        fixed { // back to normal
            emailext subject: 'Build $BUILD_STATUS $PROJECT_NAME #$BUILD_NUMBER!', 
            body: '''Check console output at $BUILD_URL to view the results.''',
            recipientProviders: [culprits(), requestor()], 
            to: 'nattable-dev@eclipse.org'
        }
    }
}
