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
```

Check the resources

You should see that the operator is up and running, along with the shipped controllers.  

## Modify

## Create Free DB

## Deploy Demo App

## Uninstall