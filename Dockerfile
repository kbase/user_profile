FROM kbase/kb_jre:latest AS build
# Multistage Build Setup
RUN apt-get -y update && apt-get -y install ant git openjdk-8-jdk
RUN cd /tmp && \
    git clone https://github.com/kbase/jars

COPY . /tmp/user_profile

# Bypass the makefile, which has perl dependencies, and just use
# the underlying ant compile and ant buildwar
RUN cd /tmp/user_profile && \
    ant compile && \
    ant buildwar

FROM kbase/kb_jre:latest
# These ARGs values are passed in via the docker build command
ARG BUILD_DATE
ARG VCS_REF
ARG BRANCH=develop

COPY deployment/ /kb/deployment/
COPY jettybase/ /kb/deployment/jettybase/
COPY --from=build /tmp/user_profile/dist/UserProfileService.war /kb/deployment/jettybase/webapps/ROOT.war

# The BUILD_DATE value seem to bust the docker cache when the timestamp changes, move to
# the end
LABEL org.label-schema.build-date=$BUILD_DATE \
      org.label-schema.vcs-url="https://github.com/kbase/user_profile.git" \
      org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.schema-version="1.0.0-rc1" \
      us.kbase.vcs-branch=$BRANCH \
      maintainer="Steve Chan sychan@lbl.gov"

WORKDIR /kb/deployment/jettybase
ENV KB_DEPLOYMENT_CONFIG=/kb/deployment/conf/deployment.cfg

ENTRYPOINT [ "/kb/deployment/bin/dockerize" ]

# Here are some default params passed to dockerize. They would typically
# be overidden by docker-compose at startup
CMD [  "-template", "/kb/deployment/conf/.templates/deployment.cfg.templ:/kb/deployment/conf/deployment.cfg", \
       "/bin/bash", "-c", "java -DSTOP.PORT=8079 -DSTOP.KEY=foo -Djetty.home=$JETTY_HOME -jar $JETTY_HOME/start.jar" ]