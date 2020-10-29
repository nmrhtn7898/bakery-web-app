#!/bin/sh
if [[-z $DATABASESERVER_PORT]]; then
  echo "********************************************************"
  echo "waiting for the database server to start on port $DATABASESERVER_PORT"
  echo "********************************************************"
  while ! `nc -z database $DATABASESERVER_PORT`; do sleep 3; done
  echo ">>>>>>>>>>>> Database Server has started"
fi
echo "*******************************************"
echo "run bread auth server($ACTIVE_PROFILE profiles)"
echo "*******************************************"
java -jar -Dencrypt.key=$ENCRYPT_KEY -Dencrypt.alg=$ENCRYPT_ALG -Dspring.profiles.active=$ACTIVE_PROFILE app.jar