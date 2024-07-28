# Based on centos 7
FROM centos:7
LABEL maintainers="KubeEdge Community Developer"
LABEL description="KubeEdge Raspi Yeelight App"

# Install Git, Python, and its dependencies
#RUN yum -y update 
#RUN yum -y install git 
#RUN yum -y install epel-release
#RUN yum -y install centos-release-scl
#RUN yum -y install rh-python38
#RUN alternatives --set python /usr/bin/python3.8
#RUN yum -y install python-pip
#RUN yum -y install libffi-devel openssl-devel

#RUN set -ex 
    # 预安装所需组件
RUN yum update -y 
RUN yum install -y wget tar libffi-devel zlib-devel bzip2-devel openssl-devel ncurses-devel sqlite-devel readline-devel tk-devel gcc make initscripts 
RUN yum clean all 
RUN wget https://www.python.org/ftp/python/3.8.0/Python-3.8.0.tgz
RUN tar -zxvf Python-3.8.0.tgz
RUN cd Python-3.8.0 \
    && ./configure prefix=/usr/local/python3 \
    && make \
    && make install \
    && make clean 
RUN yum install -y epel-release 
RUN yum install -y python-pip
# 设置默认为python3
#RUN set -ex \
    # 备份旧版本python
RUN mv /usr/bin/python /usr/bin/python27 
RUN mv /usr/bin/pip /usr/bin/pip-python2.7 
    # 配置默认为python3
RUN ln -s /usr/local/python3/bin/python3.8 /usr/bin/python 
RUN ln -s /usr/local/python3/bin/pip3 /usr/bin/pip
# 修复因修改python版本导致yum失效问题
#RUN set -ex \
RUN sed -i "s#/usr/bin/python#/usr/bin/python2.7#" /usr/bin/yum 
RUN sed -i "s#/usr/bin/python#/usr/bin/python2.7#" /usr/libexec/urlgrabber-ext-down 
RUN yum install -y deltarpm

RUN yum -y install git 

# Install setuptools_rust
RUN pip install setuptools_rust

RUN pip install --upgrade pip
RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y


# Install python-miio package
#RUN pip3 install python-miio
RUN pip install git+https://github.com/rytilahti/python-miio.git

RUN pip install urllib3==1.26.7

# Copy from build directory
COPY yeelight-app /yeelight-app
COPY scripts/on.py /on.py
COPY scripts/off.py /off.py
COPY scripts/getPower.py /getPower.py
COPY scripts/setBrightness.py /setBrightness.py
COPY scripts/getBrightness.py /getBrightness.py
COPY scripts/setRGB.py /setRGB.py
COPY scripts/getRGB.py /getRGB.py


# Define default command
ENTRYPOINT ["/yeelight-app"]

# Run the executable
CMD ["yeelight-app"]

