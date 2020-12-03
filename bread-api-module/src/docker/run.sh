#!/bin/sh
#if [[ $ACTIVE_PROFILE == "default" ]]; then
#  echo "********************************************************"
#  echo "waiting for the database server to start on port $DATABASESERVER_PORT"
#  echo "********************************************************"
#  while ! `nc -z database $DATABASESERVER_PORT`; do sleep 3; done
#  echo ">>>>>>>>>>>> Database Server has started"
#fi
echo "*******************************************"
echo "run bread auth server(profiles:$ACTIVE_PROFILE)"
echo "*******************************************"
java -jar -Dencrypt.key=$ENCRYPT_KEY \
-Dencrypt.alg=$ENCRYPT_ALG \
-Dspring.profiles.active=$ACTIVE_PROFILE app.jar