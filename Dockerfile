FROM openjdk:11

# 设定时区
ENV TZ=Asia/Shanghai
RUN set -eux;\
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime;\
    echo $TZ > /etc/timezone

# 新建用户 jt808
RUN set -eux;\
    addgroup --gid 1000 common-data;\
    adduser --system --uid 1000 -gid 1000 --home=/home/common-data/ --shell=/bin/sh --disabled-password common-data;\
    mkdir -p /home/common-data/data /home/common-data/logs /home/common-data/common-data;\
    chown -R common-data:common-data /home/common-data

# 导入启动脚本
COPY --chown=common-data:common-data docker-entrypoint.sh /home/common-data/docker-entrypoint.sh

# 导入代码
COPY --chown=common-data:common-data /build/libs/common-data-1.0.jar /home/common-data/common-data/common-data.jar

RUN ["chmod", "+x", "/home/common-data/docker-entrypoint.sh"]

USER common-data

ENTRYPOINT ["/home/common-data/docker-entrypoint.sh"]