#/bin/bash
#set -x
# script to remove tenant from  given esx host list
source common.sh

ALL_ARGs="$0 called with arguments - $*"
LOG $LOG_DEBUG "$ALL_ARGs"
# Validate arguments
if [ $# > 1 ] && [ $1 == "--help" ] || [ $1 == "--h" ]; then
   LOG $LOG_ERROR "$0  <comma-separated-esx-IP-list> <tenant-name>"
   echo "$0  <comma-separated-esx-IP-list> <tenant-name>"
   exit 1
fi
if [ $# != 2 ]; then
   LOG $LOG_ERROR "$0  <comma-separated-esx-IP-list> <tenant-name>"
   echo "$0  <comma-separated-esx-IP-list> <tenant-name>"
   exit 1
fi

IP_LIST=$1
tenantName=$2

for esx_ip in `echo $1 | sed s/,/\ /` ; do
   echo $esx_ip
   REXEC "root@$esx_ip" "/usr/lib/vmware/vmdkops/bin/vmdkops_admin.py tenant rm --name $tenantName"
   ExitOnFail 0 "Failed to remove Tenant $tenantName on $esx_ip" 1
done

exit 0;

