FROM kbase/sdkbase2:latest AS build

WORKDIR /tmp/up

# dependencies take a while to D/L, so D/L & cache before the build so code changes don't cause
# a new D/L
# can't glob *gradle because of the .gradle dir
COPY build.gradle gradlew settings.gradle /tmp/up/
COPY gradle/ /tmp/up/gradle/
RUN ./gradlew dependencies

# Now build the code
# copy the deployment dir first since it's unlikely to change often
COPY deployment/ /kb/deployment
COPY jettybase /kb/deployment/jettybase
COPY src /tmp/up/src/
COPY war /tmp/up/war/
RUN ./gradlew war

# Build the deployment directory
ENV DEPL=/kb/deployment/jettybase
RUN mkdir -p $DEPL/webapps
RUN cp /tmp/up/build/libs/user_profile.war $DEPL/webapps/ROOT.war

FROM kbase/kb_jre:latest

# These ARGs values are passed in via the docker build command
ARG BUILD_DATE
ARG VCS_REF
ARG BRANCH=develop

COPY --from=build /kb/deployment/ /kb/deployment/

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
CMD [  "-env", "/kb/deployment/conf/example.ini", \
       "-template", "/kb/deployment/conf/.templates/deployment.cfg.templ:/kb/deployment/conf/deployment.cfg", \
       "-template", "/kb/deployment/conf/.templates/http.ini.templ:/kb/deployment/jettybase/start.d/http.ini", \
       "-template", "/kb/deployment/conf/.templates/server.ini.templ:/kb/deployment/jettybase/start.d/server.ini", \
       "-template", "/kb/deployment/conf/.templates/start-server.sh.templ:/kb/deployment/bin/start-server.sh", \
       "-stdout", "/kb/deployment/jettybase/logs/request.log", \
       "-poll", \
       "/bin/sh", "-x", "/kb/deployment/bin/start-server.sh" ]