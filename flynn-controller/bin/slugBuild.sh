#!/bin/sh

CMD="docker run -i -v /tmp:/tmp -e SVN_URL=$SVN_URL -e APP_NAME=$APP_NAME $IMAGE_SVN"
echo $CMD
$CMD

CMD="cat /tmp/$APP_NAME.tar |  docker run -i -e HTTP_SERVER_URL=$HTTP_SERVER_URL -a stdin -a stdout -a stderr $IMAGE_SLUGBUILDER $HTTP_SERVER_URL/slugs/$APP_NAME.tgz"
echo $CMD
sh -c "$CMD"