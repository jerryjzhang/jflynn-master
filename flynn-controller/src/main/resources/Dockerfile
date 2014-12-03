FROM docker.oa.com:8080/tegdsf/centos
RUN svn checkout http://tc-svn.tencent.com/doss/doss_openapi_rep/openapi_proj/trunk/jflynn /tmp/jflynn/
RUN cd /tmp/jflynn; mvn install
RUN mv /tmp/jflynn/flynn-controller/release /root
RUN chmod +x /root/release/bin/*
EXPOSE 58080
ENTRYPOINT /root/release/bin/bootstrap.sh