## Deploying Spring Boot Application in Minikube Cluster

If you're reading this article means you're now familiar with the docker components and its good time to move on to next step which is Kubernetes, an orchestration tool that helps in managing the containers on the next level.

If you've 2-3 containers that you can easily run with the docker but what if you have many containers and all of them are connected to each other, in that case, we need an orchestration tool that helps in managing the containers and monitor the health of our containers to make sure that our application remains always in the running condition even when there is huge traffic.

There are a lot more concepts in the Kubernetes world which obviously can't be explained in one post. If you like to learn more about Kubernetes then you can find some articles on this blog about this which will surely help you to learn this tool.

In this particular post, I'm going to explain to you how you can run your first Spring Boot application in the minikube cluster along with MySQL database but before we start if you don't know what minikube is and why there is a need for this, then read the below lines.

Kubernetes is not for local machines on which you can set up and run a cluster because it's comprised of nodes and each node represents either a physical machine or a virtual machine and on top of all these nodes there is one node which is called as master node whose task is to manage the worker nodes like balancing the load, scheduling the requests to nodes, replication of nodes etc.

What is minikube then? It's a tool that allows the developer to set up a single node cluster just to learn this technology and do some testing in the local environment rather than production. With the help of this, you can set up a single node cluster and deploy an application on the same which what we're going to do so let's start.

I hope you've installed minikube on your machine, if not then you can install it by going to this link.

The first step is to include the docker env in minikube by this.

```
eval $(minikube docker-env)
```

Note: First start the minikube cluster and then hit the above command.

Now you need two images:

1. Spring Boot App which we're going to build
2. MySQL which you'll pull from docker hub

Next steps would be:

1. Clone the source code from Github using "git clone https://github.com/rowhyt/springboot-docker-minikube"
2. Now go to src/main/resources, you'll see five YML files which will deploy your application to minikube cluster
3. Open all the files in a text editor and change according to your need else let it be as it is
4. Run "mvn install" to generate the artifact
5. Build the docker image out of it with the below command and pull the MySQL image also if not there

To build the docker image, use the below commands:

```
docker build -t 100598/springboot:1.0 .
docker pull mysql:5.7
```

Now you've both the images in your hand, now it's time to configure the YML files although they are already configured let's try to understand them.

/* Filename: mysql-configmap.yml */

```
apiVersion: v1
kind: ConfigMap
metadata:
  name: db-conf
data:
 host: mysql
 name: demo
```

What it does?

It will hold information about the database host and the database name. Here we will hardcode them so that next time if you want to change the host or database name, all you have to do is make a change here and apply them.

/* Filename: mysqldb-root-credentials.yml */

```
apiVersion: v1
kind: Secret
metadata:
  name: db-root-credentials
data:
 password: cm9vdA==
```

What it does?

It holds the sensitive data and that's why it's secret and not configmap, that's the difference between them. It holds the root user password but if you see then you will notice that this is in some kind of encryption. Yes! It's encrypted in base 64 and that's the policy of Secrets in Kubernetes.

To make a text into base 64 encrypted text. Unix provides you with the feature.

```
echo -n '{plaintext}' | base 64
```

Always put the base 64 texts into the secrets file and don't use plain text here.

/* Filename: mysqldb-credentials.yml */

```
apiVersion: v1
kind: Secret
metadata:
  name: db-credentials
data:
 username: cm9oaXQ=
 password: MTIzNA==
```

What it does?

It's similar to the above root credentials here, here we have stored the username and password of the database user which you're going to connect with. They too are in base 64 formats.

/* Filename: mysql-deployment.yml */ 

```
apiVersion: v1
kind: Service
metadata:
  name: mysql 
  labels:
    app: mysql
    tier: database
spec:
  ports:
    - port: 3306
      targetPort: 3306
  selector:      
    app: mysql
    tier: database
  clusterIP: None  
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pv-claim 
  labels:
    app: mysql
    tier: database
spec:
  accessModes:
    - ReadWriteOnce   
  resources:
    requests:
      storage: 1Gi    
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  labels:
    app: mysql
    tier: database
spec:
  selector: 
    matchLabels:
      app: mysql
      tier: database
  strategy:
    type: Recreate
  template:
    metadata:
      labels: 
        app: mysql
        tier: database
    spec:
      containers:
      - image: mysql:5.7 
        args:
          - "--ignore-db-dir=lost+found" 
        name: mysql  
        env:
        - name: MYSQL_ROOT_PASSWORD 
          valueFrom:
            secretKeyRef:
              name: db-root-credentials 
              key: password   
        - name: MYSQL_USER 
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: MYSQL_PASSWORD 
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
        - name: MYSQL_DATABASE 
            configMapKeyRef:
              name: db-conf
              key: name
        ports:
        - containerPort: 3306
          name: mysql
        volumeMounts:        
        - name: mysql-persistent-storage
          mountPath: /var/lib/mysql 
      volumes:
      - name: mysql-persistent-storage 
        persistentVolumeClaim:
          claimName: mysql-pv-claim
```

Note: Don't change anything here. It's loosely coupled.

That's a big file because it contains three things:

1. Service, to make it accessible
2. Deployment, to make node in the cluster
3. Persistent Volume, to make data persistent of the database

/* Filename: deployment.yml */

```
kind: Service
apiVersion: v1
metadata:
  name: springboot
  labels:
    name: springboot
spec:
  ports:
    - nodePort: 30164 
      port: 8080      
      targetPort: 8080  
      protocol: TCP
  selector:           
    app: springboot
  type: LoadBalancer       

---
apiVersion: apps/v1 
kind: Deployment    
metadata:              
  name: springboot
spec:                
  selector:         
    matchLabels:
      app: springboot
  replicas: 3        
  template:
    metadata:
      labels:        
        app: springboot
    spec:
      containers:
        - name: springboot
          image: 100598/springboot:1.0
          ports:
            - containerPort: 8080                
          env:   
          - name: DB_HOST  
            valueFrom: 
              configMapKeyRef:
                name: db-conf 
                key: host
          - name: DB_NAME  
            valueFrom:
              configMapKeyRef:
                name: db-conf 
                key: name
          - name: DB_USERNAME 
            valueFrom:
              secretKeyRef:
                name: db-credentials 
                key: username
          - name: DB_PASSWORD 
            valueFrom:
              secretKeyRef:
                name: db-credentials
                key: password
```

Note: Don't change anywhere, this too is loosely coupled. Changing the ports can also lead your application to not start. This is yet another important file that has two things, service and deployment for the Spring Boot application only. Every deployment file has it's own job.

Now you have done almost.

You have the Spring Boot App image and MySQL image on your machine and all the deployment-related files also present and now it's time to apply them for that.

1. Go to the src/main/resources
2. Now hit the below five command in the same order

```
kubectl apply -f mysql-configmap.yml
kubectl apply -f mysqldb-root-credentials.yml
kubectl apply -f mysqldb-credentials.yml
kubectl apply -f mysql-deployment.yml
kubectl apply -f deployment.yml
```

To check the deployments

```
kubectl get deployments
```

To check the pods

```
kubectl get pods
```

To check the services

```
kubectl get services
```

To check the replicasets

```
kubectl get replicasets
```

To open the application, hit the below command:

```
minikube service {service name}
```

This will give you the access URL of the deployment.
