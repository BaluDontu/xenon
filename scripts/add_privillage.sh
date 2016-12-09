#/bin/bash
#set -x
# script to add privillages for given tenant on a given esx hosts
source /tmp/scripts/common.sh

ALL_ARGs="$0 called with arguments - $*"
LOG $LOG_DEBUG "$ALL_ARGs"
# Validate arguments
if [ $# -gt 1 ] && [ $1 == "--help" ] || [ $1 == "--h" ]; then
   LOG $LOG_ERROR "$0  <comma-separated-esx-IP-list> <tenant-name> <datastore> <rights> [<max_size> <total_size>]"
   echo "$0  <comma-separated-esx-IP-list> <tenant-name> <datastore> <rights> [<max_size> <total_size>]"

   exit 1
fi

if [ $# -lt 4 -o $# -gt 6 ] ; then
   LOG $LOG_ERROR "$0  <comma-separated-esx-IP-list> <tenant-name> <datastore> <rights> [<max_size> <total_size>]"
   echo "$0  <comma-separated-esx-IP-list> <tenant-name> <datastore> <rights> [<max_size> <total_size>]"
   exit 1
fi

IP_LIST=$1
tenantName=$2
datastore=$3
rights=$4
if [ $# -ge 5 ]; then
   max_size="--volume-maxsize $5"
fi
if [ $# -ge 6 ]; then
   total_size="--volume-totalsize $6"
fi

for esx_ip in `echo $1 | sed s/,/\ /` ; do
   echo $esx_ip
   REXEC "root@$esx_ip" "/usr/lib/vmware/vmdkops/bin/vmdkops_admin.py tenant access add --name $tenantName --datastore $datastore --rights $rights $total_size $max_size"
   ExitOnFail 0 "Failed to add access $rights for datastore $datastore to Tenant $tenantName on $esx_ip" 1
done

exit 0;

