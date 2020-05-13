FROM registry.cn-hangzhou.aliyuncs.com/macula-cloud/java-base:0.8.2
WORKDIR /work
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN echo -e "https://mirror.tuna.tsinghua.edu.cn/alpine/v3.9/main" > /etc/apk/repositories
RUN apk add ttf-dejavu && wget http://10.86.65.99:8080/apache-skywalking-apm-6.3.0.tar.gz && tar -xf apache-skywalking-apm-6.3.0.tar.gz && mv apache-skywalking-apm-bin/agent skywalking-agent && rm -rf apache-skywalking-apm-*
ARG JAR_FILE
ARG SERVER_PORT
ARG SERVICE_NAME
ENV JAR_FILE=${JAR_FILE}
ENV SERVER_PORT=${SERVER_PORT}
ENV SERVICE_NAME=${SERVICE_NAME}
ENV AGENT_PATH=./skywalking-agent/skywalking-agent.jar
ADD target/${JAR_FILE} .
EXPOSE ${SERVER_PORT}
ENV MC_JAVA_OPTS="-javaagent:${AGENT_PATH} -Dskywalking.agent.service_name=${SERVICE_NAME} -Dskywalking.agent.instance_uuid=${HOSTNAME}"
ENTRYPOINT [ "sh", "-c", "java ${JAVA_OPTS} ${MC_JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar ${JAR_FILE}" ]