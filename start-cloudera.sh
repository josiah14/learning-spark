# Ports: 80 - tutorial, 8888 - Hue, 5140 - Flume HTTP, 6000 - Flume Websockets, 7180 - Cloudera Manager
sudo docker run --hostname=quickstart.cloudera --privileged=true -it -v /home/josiah/Development/learning-spark:/learning-spark -p 80:80 -p 8888:8888 -p 5140:5140 -p 7180:7180 -p 6000:6000 josiah14/cloudera:5.7 docker-quickstart
