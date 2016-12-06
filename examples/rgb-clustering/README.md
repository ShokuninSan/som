# RGB clustering example

## Build assembly JAR
`$ sbt assembly`

## Run application on local standalone cluster
```
$ spark-submit \
--master "local[*]" \
--name "RGB Clustering" \
--files "data/rgb.csv" \
--class RGB \
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
--master spark://ec2-52-211-75-225.eu-west-1.compute.amazonaws.com:7077 \
--deploy-mode cluster \
--name "RGB Clustering" \
--files "data/rgb.csv" \
--class RGB \
target/scala-2.11/rgb-clustering-assembly-1.0.0.jar
```

### Start/stop cluster
`spark-ec2 --region=us-west-1 start som-cluster`
`spark-ec2 --region=us-west-1 stop som-cluster`

### SSH into your master node
`spark-ec2 --key-pair=somclusterkeys --identity-file=somclusterkeys.pem --region=eu-west-1 login som-cluster`