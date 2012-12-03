cloudify-widget
===============

The Cloudify Widget for embedding a Cloudify "recipe player" in other website, to allow visitors to launch a certain recipe with a single click.
The widget supports installation only on [HPCloud](http://www.hpcloud.com) at the moment, but provisioning and interaction with the cloud APIs is done via the [jclouds](http://www.jclouds.org) library.

Supported Platforms
===================
The widget server was tested on CentOs 5.6 and ec2-linux. Technically it should also support Debian/Ubuntu flavors (although it has never been tested).

Installation
============
* Install a Java SDK (1.6 or 1.7, OpenJDK will also work), e.g.

```
yum install java-1.6.0-openjdk-devel
```

Note that the above command requires sudo permissions.
* set the JAVA_HOME environment variable, e.g.

```
export JAVA_HOME=/usr/lib/jvm/java-1.6.0-openjdk.x86_64
```

* From your home directory, install Play framework 2.0.x, e.g.

```
wget 'http://download.playframework.org/releases/play-2.0.4.zip'
unzip play-2.0.4.zip
```

* From your home directory, install Cloudify:

```
wget 'http://repository.cloudifysource.org/org/cloudifysource/2.2.0-RELEASE/gigaspaces-cloudify-2.2.0-ga-b2500.zip'
unzip gigaspaces-cloudify-2.2.0-ga-b2500.zip
```

* Install git on the server. You can do so using yum or apt-get depending on the linux flavor you use, e.g.:

```
yum install git
```

* Clone this repository with git

```
git clone https://github.com/CloudifySource/cloudify-widget.git
```

* Configure cloud credentials (cloud provider credentials, pem file, ssh user)
Copy your cloud ssh keypair file to a known location on the server machine, and set the location of the ssh key file in the `conf/cloudify.conf` file, e.g.

```
server.bootstrap.ssh-private-key=/bin/hpcloud.pem
```

Set the HPCloud credentials in the `conf/cloudify.conf` file. You should set the following two properties to your corresponding HPCloud account details:

```
server.bootstrap.api-key=<HP Cloud Password>
server.bootstrap.username=<tenant>:<user>
```

* Configure the location of the Cloudify distribution
In the file `bin/deployer.sh`, edit the following line to point to the root of the cloudify distribution you installed on the server:

```
GS_HOME=~/gigaspaces-cloudify-2.2.0-ga
```

* Start Play:
cd to the directory in which you installed the Play framework. If you're in development mode, type `run <port number>`, otherwise type `start <port>`

* The login page for widget admins is located at `/admin/signin.html'

Configuration Options
=====================
All configuration options can be found at the `cloudify.conf` file, here are the major ones you should be aware of:
```
// The instance ID of the server on HPCloud. Used to filter it out when tearing down VMs on startup
widget.server-id=466999

// Determines whether to clear all the data from the DB and shutdown all running VMs in the account,
// or leave things as is
server-pool.cold-init=false

// The minimum and maximum number of nodes in the server pool
server-pool.min-nodes=2
server-pool.max-nodes=5

//The availability zone on which tp deploy the server and VMs
server.bootstrap.zone-name=az-1.region-a.geo-1

//The name of the keypair used for ssh'ing to the created VMs
server.bootstrap.key-pair=cloudify

//the security group to use when launching machine
server.bootstrap.security-group=default

//the image flavor and id of the widget VMs created by the server
server.bootstrap.flavor-id=102
server.bootstrap.image-id=1358

//ssh user for the created VMs
server.bootstrap.ssh-user=root

//ssh port for the created VMs
server.bootstrap.ssh-port=22

//the location of the ssh key file on the server machine
server.bootstrap.ssh-private-key=/bin/hpcloud.pem

//the HPCloud account API key, tenant and user ID. Used when starting new VMs
server.bootstrap.api-key=<HP Cloud Password>
server.bootstrap.username=<tenant>:<user>
```