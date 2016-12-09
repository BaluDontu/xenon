#/bin/bash
#set -x
# script to list volumes for given tenant from  given esx host list
source /tmp/scripts/common.sh

ALL_ARGs="$0 called with arguments - $*"
LOG $LOG_DEBUG "$ALL_ARGs"
# Validate arguments
if [ $# > 1 ] && [ $1 == "--help" ] || [ $1 == "--h" ]; then
   LOG $LOG_ERROR "$0  <comma-separated-esx-IP-list> <tenant-name>"
   exit 1
fi
if [ $# != 2 ]; then
   LOG $LOG_ERROR "$0  <comma-separated-esx-IP-list> <tenant-name>"
   exit 1
fi

IP_LIST=$1
tenantName=$2
VOL_LIST=""
for esx_ip in `echo $1 | sed s/,/\ /` ; do
   echo $esx_ip
   list=`REXEC "root@$esx_ip" "/usr/lib/vmware/vmdkops/bin/vmdkops_admin.py tenant ls | sed 1,2d | awk '{ print \\\$1 \":\" \\\$2 \":\" \\\$3}' " 2>&1`
   VOL_LIST=`echo $VOL_LIST $list`
done
echo $VOL_LIST
exit 0;



