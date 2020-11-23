def modules = ['bread-api-module', 'bread-auth-module']

void createAndPushDockerImage(name) {
    sh "docker build -t ${name}:${TAG} --build-arg NAME=${name} ${name}/build/libs"
    sh "docker tag ${name}:${TAG} nmrhtn7898/${name}:${TAG}"
    sh "docker push nmrhtn7898/${name}:${TAG}"
    if (TAG != "latest") {
        sh "docker tag ${name}:${TAG} nmrhtn7898/${name}"
        sh "docker push nmrhtn7898/${name}"
    }
    sh "echo 'y' | docker image prune"
}

node {

    // 소스 체크아웃
    stage('Checkout') {
        cleanWs()
        if (TAG == "latest") {
            sh "git clone -b ${BRANCH} --single-branch https://github.com/nmrhtn7898/bread-project.git ."
        } else {
            sh "git clone -b ${TAG} https://github.com/nmrhtn7898/bread-project.git ."
        }
    }

    // 빌드
    stage('Build') {
        sh 'chmod +x gradlew'
        if (MODULE_NAME == "all") {
            sh './gradlew clean build'
            sh './gradlew jacocoRootReport coveralls'
        } else {
            sh "./gradlew :${MODULE_NAME}:clean :${MODULE_NAME}:build"
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
                            execCommand: "/home/ec2-user/deploy/deploy.sh ${MODULE_NAME} ${TAG}"
                        )
                    ]
                )
            ]
        )
    }

}