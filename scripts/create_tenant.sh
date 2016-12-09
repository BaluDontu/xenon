#/bin/bash
#set -x
# script to create tenant from  given esx host list
source common.sh

ALL_ARGs="$0 called with arguments - $*"
LOG $LOG_DEBUG "$ALL_ARGs"
# Validate arguments
if [ $# > 1 ] && [ $1 == "--help" ] || [ $1 == "--h" ]; then
   LOG $LOG_ERROR "$0  <comma-separated-esx-IP-list> <tenant-name> <tenant-description>"
   echo "$0  <comma-separated-esx-IP-list> <tenant-name> <tenant-description>"
   exit 1
fi
if [ $# != 3 ]; then
   LOG $LOG_ERROR "$0  <comma-separated-esx-IP-list> <tenant-name> <tenant-description>"
   echo "$0  <comma-separated-esx-IP-list> <tenant-name> <tenant-description>"
   exit 1
fi

IP_LIST=$1
tenantName=$2
tenantDesc=$3

for esx_ip in `echo $1 | sed s/,/\ /` ; do
   echo $esx_ip
   REXEC "root@$esx_ip" "/usr/lib/vmware/vmdkops/bin/vmdkops_admin.py tenant create --name $tenantName"
   ExitOnFail 0 "Failed to create Tenant $tenantName on $esx_ip" 1
done

exit 0;

