# RGB clustering example

## Build assembly JAR
`$ sbt assembly`

## Run application on local standalone cluster
```
$ spark-submit \
--master 'local[*]' \
--name 'RGB clustering application' \
--files data/rgb.csv \
--class example.RGB \
target/scala-2.11/rgb-clustering-assembly-1.0.0.jar
```

## Run application on Amazon EC2

### Launch a cluster on EC2
`$ spark-ec2 --key-pair=somclusterkeys --identity-file=somclusterkeys.pem --region=eu-west-1 launch som-cluster`
Note: Don't forget to create the corresponding keypair for your region (e.g. eu-west-1) first.

### Run JAR on EC2
Note: Make sure that your master accepts inbound TCP connections from your public IP on 7077 (see Network & Security section on your EC2 Dashboard).
```
$ spark-submit \
--master spark://ec2-xx-xxx-xx-xxx.eu-west-1.compute.amazonaws.com:7077 \
--deploy-mode cluster \
--name 'RGB clustering application' \
--files data/rgb.csv \
--class example.RGB \
target/scala-2.11/rgb-clustering-assembly-1.0.0.jar
```

### Start/stop cluster
`spark-ec2 --region=us-west-1 start|stop som-cluster`

### SSH into your master node
`spark-ec2 --key-pair=somclusterkeys --identity-file=somclusterkeys.pem --region=eu-west-1 login som-cluster`

### Copy files manually to master node
`scp -i somclusterkeys.pem data/rgb.csv target/scala-2.11/rgb-clustering-assembly-1.0.0.jar root@ec2-xx-xxx-xx-xx.eu-west-1.compute.amazonaws.com:`