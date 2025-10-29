
# New ATP

$ oc create -f newadb.yaml 
Warning: resource secrets/adb-admin-password is missing the kubectl.kubernetes.io/last-applied-configuration annotation which is required by oc apply. oc apply should only be used on resources created declaratively by either oc create --save-config or oc apply. The missing annotation will be patched automatically.
secret/adb-admin-password configured
autonomousdatabase.database.oracle.com/adb-sample created

$ kubectl get autonomousdatabases.database.oracle.com -o json adb-sample|jq .status.allConnectionStrings
[
  {
    "connectionStrings": [
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_newadb_high.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "newadb_high"
      },
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_newadb_low.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "newadb_low"
      },
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_newadb_medium.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "newadb_medium"
      },
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_newadb_tp.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "newadb_tp"
      },
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_newadb_tpurgent.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "newadb_tpurgent"
      }
    ],
    "tlsAuthentication": "Mutual TLS"
  }
]

# copy one of the connectString into tnsnames.ora

$ oc create cm tnsnames --from-file=tnsnames.ora=tnsnames.ora

$ oc set volume deploy/oracle-crud-jvm --add --name=tnsnames -t configmap --configmap-name=tnsnames --mount-path="/tnsnames" --overwrite

$ oc set env deployment/oracle-crud-jvm CONNECTION="vertx-reactive:oracle:thin:@atprba_tp?TNS_ADMIN=/tnsnames/"


## Uninstall

$ oc delete -f newadb.yaml

# Existing ATP

cat <<EOF| kubectl apply -f -
apiVersion: database.oracle.com/v1alpha1
kind: AutonomousDatabase
metadata:
  name: atprba
spec:
  details:
    autonomousDatabaseOCID: ocid1.autonomousdatabase.oc1.eu-frankfurt-1.antheljrhw7kcgqagkfwaohxgzniu6fmmuesads6bv25rkibzcdtd3khzj4q
  ociConfig:
    configMapName: oci-cred
    secretName: oci-privatekey
EOF
autonomousdatabase.database.oracle.com/atprba created

$ kubectl get autonomousdatabases.database.oracle.com -o json atprba|jq .status.lifecycleState
"AVAILABLE"


$ kubectl get autonomousdatabases.database.oracle.com -o json atprba|jq .status.allConnectionStrings
[
  {
    "connectionStrings": [
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_atprba_high.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "atprba_high"
      },
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_atprba_low.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "atprba_low"
      },
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_atprba_medium.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "atprba_medium"
      },
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_atprba_tp.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "atprba_tp"
      },
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_atprba_tpurgent.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "atprba_tpurgent"
      }
    ],
    "tlsAuthentication": "Mutual TLS"
  },
  {
    "connectionStrings": [
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_atprba_high.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "atprba_high"
      },
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_atprba_low.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "atprba_low"
      },
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_atprba_medium.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "atprba_medium"
      },
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_atprba_tp.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "atprba_tp"
      },
      {
        "connectionString": "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.eu-frankfurt-1.oraclecloud.com))(connect_data=(service_name=ceeyoqrejitwmkh_atprba_tpurgent.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))",
        "tnsName": "atprba_tpurgent"
      }
    ],
    "tlsAuthentication": "TLS"
  }
]
