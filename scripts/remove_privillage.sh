#/bin/bash
#set -x
# script to add privillages for given tenant on a given esx hosts
source common.sh

ALL_ARGs="$0 called with arguments - $*"
LOG $LOG_DEBUG "$ALL_ARGs"
# Validate arguments
if [ $# -gt 1 ] && [ $1 == "--help" ] || [ $1 == "--h" ]; then
   LOG $LOG_ERROR "$0  <comma-separated-esx-IP-list> <tenant-name> <datastore>"
   echo "$0  <comma-separated-esx-IP-list> <tenant-name> <datastore>"
   exit 1
fi

if [ $# -ne 3 ] ; then
   LOG $LOG_ERROR "$0  <comma-separated-esx-IP-list> <tenant-name> <datastore>"
   echo "$0  <comma-separated-esx-IP-list> <tenant-name> <datastore>"
   exit 1
fi

IP_LIST=$1
tenantName=$2
datastore=$3

for esx_ip in `echo $1 | sed s/,/\ /` ; do
   echo $esx_ip
   REXEC "root@$esx_ip" "/usr/lib/vmware/vmdkops/bin/vmdkops_admin.py tenant access rm --name $tenantName --datastore $datastore"
   ExitOnFail 0 "Failed to remove access for datastore $datastore to Tenant $tenantName on $esx_ip" 1
done

exit 0;

