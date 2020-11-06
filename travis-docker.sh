./gradlew createDockerImage
docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
docker tag $DOCKER_USERNAME/bread-auth-module:$BREAD_AUTH_MODULE_VERSION $DOCKER_USERNAME/bread-auth-module
docker push $DOCKER_USERNAME/bread-auth-module:$BREAD_AUTH_MODULE_VERSION
docker push $DOCKER_USERNAME/bread-auth-module