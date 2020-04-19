##English
## Resetdns introduce

It is an open-source software that automatically detects its own Internet IP, dynamically sets domain name resolution in Alibaba cloud, and automatically maps the specified host name service.

A typical application scenario is to automatically obtain and set domain name resolution in the home network environment, so that you can access home services at any location, such as NAS, server, etc.

## Working principle

1. Access the Internet IP by visiting the website of ip.taobao.com.
2. Obtain the resolution IP of the specified host name through the alicloud domain name service SDK. If it is inconsistent with the current IP, set a new domain name resolution.
3. Wait for a few minutes and repeat steps 1 and 2 again.

It can be deployed on low-power Linux servers such as raspberry pie.

##### Config file example：

_Note that domain name resolution only supports aksk of ALICLOUD Iam's primary account。_

```
accessKey=your accessKey
secretKey=your secretKey
hostname=your hostname
domain=your domain，如 example.com
```

### Run 

```
##package
mvn clean package
##run
java -Dfile=/my/path/config.conf -jar ./target/resetdns-1.0.jar &>> reset.log &
```

### Support Docker image
https://cloud.docker.com/u/wangboak/repository/docker/wangboak/resetdns




## 中文
## resetdns

是一个开源软件，通过自动检测自身互联网IP，动态设置在阿里云的域名解析，并自动映射指定主机名的服务。

典型的应用场景是在家庭网络环境下，自动获取并设置域名解析，让你在任何位置都可以访问家用服务，比如 NAS、服务器等。

## 工作原理

1、通过访问ip.taobao.com的网站获取自身互联网IP。  
2、通过阿里云域名服务的SDK，获取指定主机名的解析IP，如果和当前IP不一致，则设置新的域名解析。  
3、等待几分钟后再次重复1、2步骤。  

可以部署在树莓派等低能耗的linux服务器上。

##### 配置文件示例：

_注意域名解析只支持阿里云IAM的主账户的AKSK。_

```
accessKey=你的阿里云主账户的accessKey
secretKey=你的阿里云主账户的secretKey
hostname=你期望设置的主机名
domain=你的域名，如 example.com
```

### 运行命令

```
##编译打包
mvn clean package
##运行
java -Dfile=/my/path/config.conf -jar ./target/resetdns-1.0.jar &>> reset.log &
```

### 支持Docker运行
https://cloud.docker.com/u/wangboak/repository/docker/wangboak/resetdns

