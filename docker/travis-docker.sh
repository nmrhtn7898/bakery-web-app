./gradlew :$MODULE_NAME:clean :$MODULE_NAME:build :$MODULE_NAME:createDockerImage
echo $DOCKER_PASSWORD | docker login --username $DOCKER_USERNAME --password-stdin
docker tag $DOCKER_USERNAME/$MODULE_NAME:$MODULE_VERSION $DOCKER_USERNAME/MODULE_NAME
docker push $DOCKER_USERNAME/MODULE_NAME:$MODULE_VERSION
docker push $DOCKER_USERNAME/MODULE_NAME