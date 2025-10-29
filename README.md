# Example of the Oracle Database Operator on OpenShift
Tested with
- Oracle DB Operator 1.1
- Red Hat OpenShift 4.15

## Install cert Manager
The operator uses webhooks for validating user input before persisting it in etcd. Webhooks require TLS certificates that are generated and managed by a certificate manager.

Install the certificate manager with the following command:

## Install Oracle Database Operator

### Namespace Scoped Deployment
In this mode, OraOperator can be deployed to operate in a namespace, and to monitor one or many namespaces.

Grant serviceaccount:oracle-database-operator-system:default service account with resource access in the required namespaces. For example, to monitor only the default namespace, apply the default-ns-role-binding.yaml

```shell script
kubectl apply -f rbac/default-ns-role-binding.yaml
```

To watch additional namespaces, create different role binding files for each namespace, using default-ns-role-binding.yaml as a template, and changing the metadata.name and metadata.namespace fields

Next, edit the oracle-database-operator.yaml to add the required namespaces under WATCH_NAMESPACE. Use comma-delimited values for multiple namespaces.

```shell script
- name: WATCH_NAMESPACE
  value: "default"

```

Finally, apply the edited oracle-database-operator.yaml to deploy the Operator
```shell script
kubectl apply -f oracle-database-operator.yaml

# get the value of WATCH_NAMESPACE
kubectl get deploy -n oracle-database-operator-system oracle-database-operator-controller-manager -o jsonpath='{.spec.template.spec.containers[?(@.name=="manager")].env[?(@.name=="WATCH_NAMESPACE")].value}{"\n"}'
oracle,default  

# patch directly without kowing the index
kubectl patch deployment -n oracle-database-operator-system oracle-database-operator-controller-manager --type=json -p='[{"op": "replace", "path": "/spec/template/spec/containers/0/env/0/value", "value": "aaa"}]' 

# patch by searching the index
# get index of pod container
container_index=$(kubectl get deployment -n oracle-database-operator-system oracle-database-operator-controller-manager -o json | jq ".spec.template.spec.containers | to_entries | .[] | select(.value.name | contains (\"manager\")) | .key")

# get index of value
value_index=$(kubectl get deployment -n oracle-database-operator-system oracle-database-operator-controller-manager -o json | jq ".spec.template.spec.containers[] | select (.name == \"manager\").env | to_entries | .[] | select(.value.name | contains (\"WATCH_NAMESPACE\")) | .key")

kubectl patch deployment -n oracle-database-operator-system oracle-database-operator-controller-manager --type=json -p="[{'op': 'replace', 'path': '/spec/template/spec/containers/$container_index/env/$value_index/value', 'value': 'oracle'}]" 
```

### ClusterRole and ClusterRoleBinding for NodePort services
To expose services on each node's IP and port (the NodePort) apply the node-rbac.yaml. Note that this step is not required for LoadBalancer services.

```shell script
kubectl apply -f rbac/node-rbac.yaml
```

### Install Oracle DB Operator
After you have completed the preceding prerequisite changes, you can install the operator. To install the operator in the cluster quickly, you can apply the modified oracle-database-operator.yaml file from the preceding step.

Run the following command

```shell script
kubectl apply -f oracle-database-operator.yaml
```

Ensure that the operator pods are up and running. For high availability, Operator pod replicas are set to a default of 3. You can scale this setting up or down.

```shell script
kubectl get pods -n oracle-database-operator-system

NAME                                                                 READY   STATUS    RESTARTS   AGE
pod/oracle-database-operator-controller-manager-78666fdddb-s4xcm     1/1     Running   0          11d
pod/oracle-database-operator-controller-manager-78666fdddb-5k6n4     1/1     Running   0          11d
pod/oracle-database-operator-controller-manager-78666fdddb-t6bzb     1/1     Running   0          11d

oc scale deploy -n oracle-database-operator-system oracle-database-operator-controller-manager --replicas 1
deployment.apps/oracle-database-operator-controller-manager scaled
```

Check the resources

You should see that the operator is up and running, along with the shipped controllers.  

## Modify

# Create Free DB

```shell script
oc create secret generic freedb-admin-secret --from-literal=oracle-pwd=changeme

oc apply -f free.yaml
```

when ready 

```shell script
oc get singleinstancedatabase freedb -o "jsonpath={.status.status}"
```

Clients can get the connect-string to the CDB from .status.connectString and PDB from .status.pdbConnectString. For example:

```shell script
$ kubectl get singleinstancedatabase freedb -o "jsonpath={.status.clusterConnectString}"
  freedb-ext.oracle:1521/FREE

$ kubectl get singleinstancedatabase freedb -o "jsonpath={.status.connectString}"
  10.0.25.54:1521/ORCL
  192.168.50.10:32746/FREE

$ kubectl get singleinstancedatabase freedb -o "jsonpath={.status.pdbConnectString}"
  10.0.25.54:1521/ORCLPDB
  192.168.50.10:32746/FREEPDB1
```  

Use any supported client or SQLPlus to connect to the database using the above connect strings as follows

```shell script
$ sqlplus sys/<.spec.adminPassword>@10.0.25.54:1521/ORCL as sysdba
```

The Oracle Database inside the container also has Oracle Enterprise Manager Express (OEM Express) as a basic observability console. To access OEM Express, start the browser, and paste in a URL similar to the following example:

```shell script
$ kubectl get singleinstancedatabase sidb-sample -o "jsonpath={.status.oemExpressUrl}"

  https://10.0.25.54:5500/em
  Unavailable
```

xedb:1521/xepdb1  
Free/XE: system/changeme

Auto: admin/OracleDB1234

## Deploy Demo App

```shell
$ oc apply -f getting-started-reactive-crud-oracle/deployment.yaml 
deployment.apps/oracle-crud-jvm configured
service/oracle-crud-jvm unchanged
route.route.openshift.io/oracle-crud-jvm unchanged

$ CONNECT=`kubectl get singleinstancedatabase freedb -o "jsonpath={.status.clusterConnectString}"`
$ oc set env deployment/oracle-crud-jvm CONNECTION=$CONNECT

$ oc get route oracle-crud-jvm -o jsonpath={.spec.host}
oracle-crud-jvm-oracle.apps.ocp4.openshift.freeddns.org
```

## Uninstall

```shell script
$ oc delete -f free.yaml
```

# Create XE DB

```shell script
oc create secret generic xedb-admin-secret --from-literal=oracle-pwd=changeme

oc apply -f xe.yaml
```

when ready 

```shell script
oc get singleinstancedatabase xedb -o "jsonpath={.status.status}"
```

Clients can get the connect-string to the CDB from .status.connectString and PDB from .status.pdbConnectString. For example:

```shell script
$ kubectl get singleinstancedatabase xedb -o "jsonpath={.status.clusterConnectString}"
  xedb-ext.oracle:1521/XE

$ kubectl get singleinstancedatabase xedb -o "jsonpath={.status.connectString}"
  10.0.25.54:1521/ORCL
  192.168.50.10:32746/FREE

$ kubectl get singleinstancedatabase xedb -o "jsonpath={.status.pdbConnectString}"
  10.0.25.54:1521/ORCLPDB
  192.168.50.10:32746/FREEPDB1
```  

Use any supported client or SQLPlus to connect to the database using the above connect strings as follows

```shell script
$ sqlplus sys/<.spec.adminPassword>@10.0.25.54:1521/ORCL as sysdba
```

The Oracle Database inside the container also has Oracle Enterprise Manager Express (OEM Express) as a basic observability console. To access OEM Express, start the browser, and paste in a URL similar to the following example:

```shell script
$ kubectl get singleinstancedatabase sidb-sample -o "jsonpath={.status.oemExpressUrl}"

  https://10.0.25.54:5500/em
  Unavailable
```

```shell
$ CONNECT=`kubectl get singleinstancedatabase xedb -o "jsonpath={.status.clusterConnectString}"`
$ oc set env deployment/oracle-crud-jvm CONNECTION=$CONNECT

$ oc get route oracle-crud-jvm -o jsonpath={.spec.host}
oracle-crud-jvm-oracle.apps.ocp4.openshift.freeddns.org
```



kubectl create secret docker-registry ocirsecret --docker-server=container-registry.oracle.com --docker-username=robert.baumgartner@gmx.at --docker-password='myUserPassword1' --docker-email=robert.baumgartner@gmx.at

oc create -f siddb.yaml

kubectl get singleinstancedatabases.database.oracle.com siddb -o jsonpath={.status.status}
Pending
Healthy

kubectl get singleinstancedatabases.database.oracle.com siddb -o jsonpath={.status.connectString}
siddb-ext.oracle:1521/ORCL1
192.168.50.13:30504/ORCL1 (loadBalacer=false)

## Certificates

attantion: DNS must comply with the <service>.<namespace>

openssl genrsa -out ca.key 2048
openssl req -new -x509 -days 365 -key ca.key -subj "/C=US/ST=California/L=SanFrancisco/O=oracle /CN=cdb-dev-ords /CN=localhost  Root CA " -out ca.crt
openssl req -newkey rsa:2048 -nodes -keyout tls.key -subj "/C=US/ST=California/L=SanFrancisco/O=oracle /CN=cdb-dev-ords /CN=localhost" -out server.csr
echo "subjectAltName=DNS:cdb-dev-ords,DNS:cdb-dev-ords.oracle,DNS:cdb-dev-ords-oracle.apps.ocp4.openshift.freeddns.org" > extfile.txt
openssl x509 -req -extfile extfile.txt -days 365 -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out tls.crt
kubectl delete secret db-tls db-ca
kubectl create secret tls db-tls --key="tls.key" --cert="tls.crt"  -n oracle
kubectl create secret generic db-ca --from-file=ca.crt -n oracle

## Enterprise Db log

TNSLSNR for Linux: Version 21.0.0.0.0 - Production
System parameter file is /opt/oracle/homes/OraDB21Home1/network/admin/listener.ora
Log messages written to /opt/oracle/diag/tnslsnr/siddb-4pwth/listener/alert/log.xml
Listening on: (DESCRIPTION=(ADDRESS=(PROTOCOL=ipc)(KEY=EXTPROC1)))
Listening on: (DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=0.0.0.0)(PORT=1521)))
Connecting to (DESCRIPTION=(ADDRESS=(PROTOCOL=IPC)(KEY=EXTPROC1)))
STATUS of the LISTENER
------------------------
Alias LISTENER
Version TNSLSNR for Linux: Version 21.0.0.0.0 - Production
Start Date 27-SEP-2024 11:31:32
Uptime 0 days 0 hr. 0 min. 0 sec
Trace Level off
Security ON: Local OS Authentication
SNMP OFF
Listener Parameter File /opt/oracle/homes/OraDB21Home1/network/admin/listener.ora
Listener Log File /opt/oracle/diag/tnslsnr/siddb-4pwth/listener/alert/log.xml
Listening Endpoints Summary...
(DESCRIPTION=(ADDRESS=(PROTOCOL=ipc)(KEY=EXTPROC1)))
(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=0.0.0.0)(PORT=1521)))
The listener supports no services
The command completed successfully
Prepare for db operation
8% complete
Copying database files
31% complete
Creating and starting Oracle instance
32% complete
36% complete
40% complete
43% complete
46% complete
Completing Database Creation
51% complete
PDB$SEED(2):Undo initialization finished serial:0 start:612128247 end:612128797 diff:550 ms (0.6 seconds)
PDB$SEED(2):Database Characterset for PDB$SEED is AL32UTF8
2024-09-27T11:45:46.158045+00:00
PDB$SEED(2):SUPLOG: Set PDB SUPLOG SGA at PDB OPEN, old 0x0, new 0x0 (no suplog)
PDB$SEED(2):Opening pdb with no Resource Manager plan active
2024-09-27T11:45:48.489155+00:00
Pluggable database PDB$SEED opened read write
Completed: alter pluggable database "PDB$SEED" open READ WRITE services=NONE
2024-09-27T11:45:50.562073+00:00
oracle :
QPI: opatch file present, opatch
oracle :
QPI: qopiprep.bat file present
2024-09-27T11:45:56.001371+00:00
alter pluggable database "PDB$SEED" close immediate force instances=('ORCL1')
2024-09-27T11:45:56.004816+00:00
PDB$SEED(2):Pluggable database PDB$SEED closing
PDB$SEED(2):JIT: pid 3272 requesting stop
PDB$SEED(2):TABLE SYS.ACTIVITY_TABLE$: ADDED INTERVAL PARTITION SYS_P301 (1) VALUES LESS THAN (106)
PDB$SEED(2):Closing sequence subsystem (612139846161).
PDB$SEED(2):Buffer Cache flush started: 2
PDB$SEED(2):Buffer Cache flush finished: 2
Pluggable database PDB$SEED closed
Completed: alter pluggable database "PDB$SEED" close immediate force instances=('ORCL1')
2024-09-27T11:45:56.953109+00:00
alter pluggable database "PDB$SEED" open READ ONLY instances=('ORCL1')
PDB$SEED(2):Pluggable database PDB$SEED opening in read only
PDB$SEED(2):Autotune of undo retention is turned on.
PDB$SEED(2):Endian type of dictionary set to little
PDB$SEED(2):Undo initialization finished serial:0 start:612140677 end:612140677 diff:0 ms (0.0 seconds)
PDB$SEED(2):Database Characterset for PDB$SEED is AL32UTF8
PDB$SEED(2):SUPLOG: Set PDB SUPLOG SGA at PDB OPEN, old 0x0, new 0x0 (no suplog)
2024-09-27T11:45:58.273169+00:00
PDB$SEED(2):Opening pdb with no Resource Manager plan active
Pluggable database PDB$SEED opened read only
Completed: alter pluggable database "PDB$SEED" open READ ONLY instances=('ORCL1')


$ oc get singleinstancedatabases.database.oracle.com siddb -o jsonpath={.status}|jq
{
  "archiveLog": "false",
  "charset": "AL32UTF8",
  "clusterConnectString": "siddb-ext.oracle:1521/ORCL1",
  "conditions": [
    {
      "lastTransitionTime": "2024-09-27T11:45:23Z",
      "message": "no reconcile errors",
      "observedGeneration": 1,
      "reason": "LastReconcileCycleQueued",
      "status": "True",
      "type": "ReconcileQueued"
    },
    {
      "lastTransitionTime": "2024-09-27T11:45:39Z",
      "message": "processing datapatch execution",
      "observedGeneration": 1,
      "reason": "LastReconcileCycleBlocked",
      "status": "True",
      "type": "ReconcileBlocked"
    },
    {
      "lastTransitionTime": "2024-09-27T11:46:43Z",
      "message": "no reconcile errors",
      "observedGeneration": 1,
      "reason": "LastReconcileCycleCompleted",
      "status": "True",
      "type": "ReconcileComplete"
    }
  ],
  "connectString": "192.168.50.13:30418/ORCL1",
  "createdAs": "primary",
  "datafilesCreated": "true",
  "datafilesPatched": "true",
  "edition": "Enterprise",
  "flashBack": "false",
  "forceLog": "false",
  "initParams": {
    "cpuCount": 4,
    "pgaAggregateTarget": 512,
    "processes": 300,
    "sgaTarget": 1536
  },
  "isTcpsEnabled": false,
  "oemExpressUrl": "https://192.168.50.13:30420/em",
  "pdbConnectString": "192.168.50.13:30418/ORCLPDB1",
  "pdbName": "orclpdb1",
  "persistence": {
    "accessMode": "ReadWriteOnce",
    "setWritePermissions": true,
    "size": "10Gi",
    "storageClass": "nfs-csi-storage"
  },
  "prebuiltDB": true,
  "releaseUpdate": "21.3.0.0.0",
  "replicas": 1,
  "role": "PRIMARY",
  "sid": "ORCL1",
  "status": "Healthy",
  "tcpsConnectString": "Unavailable",
  "tcpsPdbConnectString": "Unavailable",
  "tcpsTlsSecret": ""
}


############
Defaulted container "siddb" out of: siddb, init-permissions (init), init-prebuiltdb (init)
[2024:09:27 11:50:01]: Acquiring lock .ORCL1.create_lck with heartbeat 30 secs
[2024:09:27 11:50:01]: Lock acquired
[2024:09:27 11:50:01]: Starting heartbeat
[2024:09:27 11:50:02]: Lock held .ORCL1.create_lck
ORACLE EDITION: ENTERPRISE

LSNRCTL for Linux: Version 21.0.0.0.0 - Production on 27-SEP-2024 11:50:02

Copyright (c) 1991, 2021, Oracle.  All rights reserved.

Starting /opt/oracle/product/21c/dbhome_1/bin/tnslsnr: please wait...

TNSLSNR for Linux: Version 21.0.0.0.0 - Production
System parameter file is /opt/oracle/homes/OraDB21Home1/network/admin/listener.ora
Log messages written to /opt/oracle/diag/tnslsnr/siddb-kmpsy/listener/alert/log.xml
Listening on: (DESCRIPTION=(ADDRESS=(PROTOCOL=ipc)(KEY=EXTPROC1)))
Listening on: (DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=0.0.0.0)(PORT=1521)))

Connecting to (DESCRIPTION=(ADDRESS=(PROTOCOL=IPC)(KEY=EXTPROC1)))
STATUS of the LISTENER
------------------------
Alias                     LISTENER
Version                   TNSLSNR for Linux: Version 21.0.0.0.0 - Production
Start Date                27-SEP-2024 11:50:03
Uptime                    0 days 0 hr. 0 min. 1 sec
Trace Level               off
Security                  ON: Local OS Authentication
SNMP                      OFF
Listener Parameter File   /opt/oracle/homes/OraDB21Home1/network/admin/listener.ora
Listener Log File         /opt/oracle/diag/tnslsnr/siddb-kmpsy/listener/alert/log.xml
Listening Endpoints Summary...
  (DESCRIPTION=(ADDRESS=(PROTOCOL=ipc)(KEY=EXTPROC1)))
  (DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=0.0.0.0)(PORT=1521)))
The listener supports no services
The command completed successfully
Prepare for db operation
8% complete
Copying database files
31% complete
Creating and starting Oracle instance
32% complete
36% complete
40% complete
43% complete
46% complete
Completing Database Creation
51% complete
54% complete
Creating Pluggable Databases
58% complete
77% complete
Executing Post Configuration Actions
100% complete
Database creation complete. For details check the logfiles at:
 /opt/oracle/cfgtoollogs/dbca/ORCL1.
Database Information:
Global Database Name:ORCL1
System Identifier(SID):ORCL1
Look at the log file "/opt/oracle/cfgtoollogs/dbca/ORCL1/ORCL1.log" for further details.

SQL*Plus: Release 21.0.0.0.0 - Production on Fri Sep 27 12:04:32 2024
Version 21.3.0.0.0

Copyright (c) 1982, 2021, Oracle.  All rights reserved.


Connected to:
Oracle Database 21c Enterprise Edition Release 21.0.0.0.0 - Production
Version 21.3.0.0.0

SQL> 
System altered.

SQL> 
System altered.

SQL> 
Pluggable database altered.

SQL> 
PL/SQL procedure successfully completed.

SQL> SQL> 
Session altered.

SQL> 
User created.

SQL> 
Grant succeeded.

SQL> 
Grant succeeded.

SQL> 
Grant succeeded.

SQL> 
User altered.

SQL> SQL> Disconnected from Oracle Database 21c Enterprise Edition Release 21.0.0.0.0 - Production
Version 21.3.0.0.0
The Oracle base remains unchanged with value /opt/oracle

Executing user defined scripts
/opt/oracle/runUserScripts.sh: running /opt/oracle/scripts/extensions/setup/swapLocks.sh
[2024:09:27 12:04:34]: Releasing lock .ORCL1.create_lck
[2024:09:27 12:04:34]: Lock released .ORCL1.create_lck
[2024:09:27 12:04:34]: Acquiring lock .ORCL1.exist_lck with heartbeat 30 secs
[2024:09:27 12:04:34]: Lock acquired
[2024:09:27 12:04:34]: Starting heartbeat
[2024:09:27 12:04:34]: Lock held .ORCL1.exist_lck

DONE: Executing user defined scripts

The Oracle base remains unchanged with value /opt/oracle
#########################
DATABASE IS READY TO USE!
#########################
The following output is now a tail of the alert.log:
ORCLPDB1(3):CREATE SMALLFILE TABLESPACE "USERS" LOGGING  DATAFILE  '/opt/oracle/oradata/ORCL1/ORCLPDB1/users01.dbf' SIZE 5M REUSE AUTOEXTEND ON NEXT  1280K MAXSIZE UNLIMITED  EXTENT MANAGEMENT LOCAL  SEGMENT SPACE MANAGEMENT  AUTO
ORCLPDB1(3):Completed: CREATE SMALLFILE TABLESPACE "USERS" LOGGING  DATAFILE  '/opt/oracle/oradata/ORCL1/ORCLPDB1/users01.dbf' SIZE 5M REUSE AUTOEXTEND ON NEXT  1280K MAXSIZE UNLIMITED  EXTENT MANAGEMENT LOCAL  SEGMENT SPACE MANAGEMENT  AUTO
ORCLPDB1(3):ALTER DATABASE DEFAULT TABLESPACE "USERS"
ORCLPDB1(3):Completed: ALTER DATABASE DEFAULT TABLESPACE "USERS"
2024-09-27T12:04:32.637131+00:00
ALTER SYSTEM SET control_files='/opt/oracle/oradata/ORCL1/control01.ctl' SCOPE=SPFILE;
2024-09-27T12:04:32.661843+00:00
ALTER SYSTEM SET local_listener='' SCOPE=BOTH;
ALTER PLUGGABLE DATABASE ORCLPDB1 SAVE STATE
Completed: ALTER PLUGGABLE DATABASE ORCLPDB1 SAVE STATE


$ oc get singleinstancedatabases.database.oracle.com siddb -o jsonpath={.status}|jq
{
  "archiveLog": "false",
  "charset": "AL32UTF8",
  "clusterConnectString": "siddb-ext.oracle:1521/ORCL1",
  "conditions": [
    {
      "lastTransitionTime": "2024-09-27T12:04:52Z",
      "message": "no reconcile errors",
      "observedGeneration": 1,
      "reason": "LastReconcileCycleQueued",
      "status": "True",
      "type": "ReconcileQueued"
    }
  ],
  "connectString": "Unavailable",
  "createdAs": "primary",
  "datafilesCreated": "true",
  "datafilesPatched": "false",
  "edition": "Enterprise",
  "flashBack": "false",
  "forceLog": "false",
  "initParams": {},
  "isTcpsEnabled": false,
  "oemExpressUrl": "Unavailable",
  "pdbConnectString": "Unavailable",
  "pdbName": "orclpdb1",
  "persistence": {
    "accessMode": "ReadWriteOnce",
    "setWritePermissions": true,
    "size": "10Gi",
    "storageClass": "nfs-csi-storage"
  },
  "prebuiltDB": true,
  "releaseUpdate": "Unavailable",
  "replicas": 1,
  "role": "Unavailable",
  "sid": "ORCL1",
  "status": "Updating",
  "tcpsConnectString": "Unavailable",
  "tcpsPdbConnectString": "Unavailable",
  "tcpsTlsSecret": ""
}

############

## ORDS log

Oracle REST Data Services - Non-Interactive Install

Retrieving information....
Your database connection is to a CDB.  ORDS common user ORDS_PUBLIC_USER will be created in the CDB.  ORDS schema will be installed in the PDBs.
Root CDB$ROOT - create ORDS common user 
PDB PDB$SEED - install ORDS 23.4.0.r3461619 (mode is READ ONLY, open for READ/WRITE)
PDB MYPDB1 - install ORDS 23.4.0.r3461619 
PDB MYPDB2 - install ORDS 23.4.0.r3461619 

The setting named: db.connectionType was set to: basic in configuration: default
The setting named: db.serviceNameSuffix was set to: .public.vcn2.oraclevcn.com in configuration: default
The setting named: db.username was set to: ORDS_PUBLIC_USER in configuration: default
The setting named: db.password was set to: ****** in configuration: default
The setting named: security.requestValidationFunction was set to: ords_util.authorize_plsql_gateway in configuration: default
2024-09-27T10:59:21.013Z INFO        Installing Oracle REST Data Services version 23.4.0.r3461619 in CDB$ROOT
2024-09-27T10:59:32.668Z INFO        ... Verified database prerequisites
2024-09-27T10:59:34.621Z INFO        ... Created Oracle REST Data Services proxy user
2024-09-27T10:59:34.907Z INFO        Completed installation for Oracle REST Data Services version 23.4.0.r3461619. Elapsed time: 00:00:07.661 

2024-09-27T10:59:36.167Z INFO        Installing Oracle REST Data Services version 23.4.0.r3461619 in PDB$SEED
2024-09-27T10:59:37.640Z INFO        ... Verified database prerequisites
2024-09-27T10:59:40.401Z INFO        ... Created Oracle REST Data Services proxy user
2024-09-27T10:59:43.686Z INFO        ... Created Oracle REST Data Services schema
2024-09-27T10:59:45.994Z INFO        ... Granted privileges to Oracle REST Data Services
2024-09-27T10:59:57.977Z SEVERE      Closed Connection
Closed Connection
Installation error


Oracle REST Data Services - Non-Interactive Install

Retrieving information....
PDB PDB$SEED - Error the ORDS_VERSION does not exist.

Installation error