FROM debian:stretch

# install debian packages:
ENV DEBIAN_FRONTEND=noninteractive
RUN set -x -e; \
    apt-get update; \
    apt-get install -y --no-install-recommends \
        default-jdk-headless maven \
        gosu sudo

# setup sudo
RUN set -x -e; \
    echo 'ALL ALL=NOPASSWD:ALL' > /etc/sudoers.d/all; \
    chmod 0400 /etc/sudoers.d/all;

# download and backup dependencies
COPY pom.xml /tmp/
RUN set -x -e; \
    mkdir -p /tmp/deps; \
    cp -v /tmp/pom.xml /tmp/deps/; \
    cd /tmp/deps; \
    mvn install; \
    mv $HOME/.m2 /usr/share/cg-brutaltester-m2; \
    rm -rf /tmp/deps

# default command that builds cg-brutaltester
CMD set -x -e; \
    mvn package -o

# setup entrypoint with user UID/GID from host
RUN set -x -e; \
    (\
    echo '#!/bin/bash'; \
    echo 'MY_UID=${MY_UID:-1000}'; \
    echo 'set -x -e'; \
    echo 'useradd -M -u "$MY_UID" -o user'; \
    echo 'echo export PATH="/usr/local/cargo/bin:$PATH" >> /home/user/.bashrc'; \
    echo 'cp -R /usr/share/cg-brutaltester-m2 /home/user/.m2'; \
    echo 'chown -R user:user /home/user'; \
    echo 'cd /home/user/work'; \
    echo 'exec gosu user "${@:-/bin/bash}"'; \
    ) > /usr/local/bin/entrypoint.sh; \
    chmod a+x /usr/local/bin/entrypoint.sh
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]

# If your UID is 1000, you can simply run the container as
# docker run -it --rm -v $PWD:/home/user/work cg-brutaltester
# otherwise, run it as:
# docker run -it --rm -v $PWD:/home/user/work -e MY_UID=$UID cg-brutaltester 
