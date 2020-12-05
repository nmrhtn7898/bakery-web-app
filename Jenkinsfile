def modules = ['bakery-api', 'bakery-auth']

void createAndPushDockerImage(name) {
    if (TAG == "latest") {
        sh "docker build -t nmrhtn7898/${name}-server:latest --build-arg NAME=${name} ${name}/build/libs"
        sh "docker push nmrhtn7898/${name}-server:latest"
    } else {
        try {
            sh "curl --silent -f https://hub.docker.com/v2/repositories/nmrhtn7898/${name}-server/tags/${TAG}"
            print 'docker image is already exists in docker image repository'
        } catch (e) {
            sh "docker build -t nmrhtn7898/${name}-server:${TAG} --build-arg NAME=${name} ${name}/build/libs"
            sh "docker push nmrhtn7898/${name}-server:${TAG}"
        }
    }
    sh "echo 'y' | docker image prune"
}

node {

    // 소스 체크아웃
    stage('Checkout') {
        cleanWs()
        sshagent(['7d91e9db-2264-4f14-bdb7-cc443e273bd5']) {
            if (TAG == "latest") {
                sh "git clone -b ${BRANCH} --single-branch git@github.com:nmrhtn7898/bakery-web-app.git ."
            } else {
                sh "git clone -b ${TAG} git@github.com:nmrhtn7898/bakery-web-app.git ."
            }
        }
    }

    // 빌드
    stage('Build') {
        sh 'chmod +x gradlew'
        if (MODULE_NAME == "all") {
            sh './gradlew clean build'
        } else {
            sh "./gradlew :${MODULE_NAME}:clean :${MODULE_NAME}:build"
        }
        if (TAG == "latest") {
                sh "./gradlew jacocoRootReport coveralls"
        }
    }

    // 도커 이미지 생성 및 도커 허브 푸시
    stage('Dockerize') {
        if (MODULE_NAME == "all") {
            modules.each { item ->
                createAndPushDockerImage(item)
            }
        } else {
            createAndPushDockerImage(MODULE_NAME)
        }
    }

    // 애플리케이션 기동 서버로 배포 및 배포 후 커맨드 처리
    stage('Deploy') {
        sh 'zip -r deploy *'
        sshPublisher(
            continueOnError: false,
            failOnError: true,
            publishers: [
                sshPublisherDesc(
                    configName: "${BRANCH}",
                    verbose: true,
                    transfers: [
                        sshTransfer(
                            sourceFiles: "deploy.zip",
                            remoteDirectory: "/sources",
                            execCommand: "/home/ec2-user/deploy/deploy.sh ${MODULE_NAME}-server ${TAG}"
                        )
                    ]
                )
            ]
        )
    }

}