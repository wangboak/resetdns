# 使用官方提供的 java 开发镜像作为基础镜像
FROM openjdk:8

# 将工作目录切换为 /app
WORKDIR /app

# 将当前目录下的所有内容复制到 /app 下
ADD . /app

# 使用 pip 命令安装这个应用所需要的依赖
# RUN pip install --trusted-host pypi.python.org -r requirements.txt

# 允许外界访问容器的 80 端口
# EXPOSE 80

# 设置环境变量
# ENV NAME World

# 设置容器进程为：java ，即：这个 java 应用的启动命令
CMD ["java", "-Dfile=/etc/resetdns/config.conf", "-jar", "resetdns-1.0.jar", ">>", "/etc/resetdns/reset.log"]