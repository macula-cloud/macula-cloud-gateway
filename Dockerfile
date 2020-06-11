FROM pdsd-registry.infinitus.com.cn/tools/javabase:0.8.2
ARG JAR_FILE 
ARG SERVICE_NAME
ENV JAR_FILE=${JAR_FILE}
ENV SERVICE_NAME=${SERVICE_NAME}
ADD target/${JAR_FILE} .
ENV SW_OPTS=${SW_OPTS} -Dskywalking.agent.service_name=${SERVICE_NAME} -Dskywalking.agent.instance_uuid=${HOSTNAME}
ENTRYPOINT [ "sh", "-c", "java ${JAVA_OPTS} ${SW_OPTS} -Djava.security.egd=file:/dev/./urandom -jar ${JAR_FILE}" ]