#/bin/bash
#set -x
# script to add vms for given tenant on a given esx hosts
source /tmp/scripts/common.sh

ALL_ARGs="$0 called with arguments - $*"
LOG $LOG_DEBUG "$ALL_ARGs"
# Validate arguments
if [ $# -gt 1 ] && [ $1 == "--help" ] || [ $1 == "--h" ]; then
   LOG $LOG_ERROR "$0  <comma-separated-esx-IP-list> <tenant-name> <comma-separated-vm-list>"
   echo "$0  <comma-separated-esx-IP-list> <tenant-name>  <comma-separated-vm-list>"

   exit 1
fi

if [ $# -ne 3 ] ; then
   LOG $LOG_ERROR "$0  <comma-separated-esx-IP-list> <tenant-name> <comma-separated-vm-list>"
   echo "$0  <comma-separated-esx-IP-list> <tenant-name> <comma-separated-vm-list>"
   exit 1
fi

IP_LIST=$1
tenantName=$2
vms=$3

for esx_ip in `echo $1 | sed s/,/\ /` ; do
   #echo $esx_ip
   ret=`REXEC "root@$esx_ip" "/usr/lib/vmware/vmdkops/bin/vmdkops_admin.py nant vm add --name $tenantName --vm-list $vms" 2>&1`
   if [ "$ret" == "tenant vm add succeeded" ] ; then
      exit 0
   fi
   # we will ignore message in case given VM is not on this ESX.
done

LOG $LOG_ERROR "Failed to add vms to tenant $tenantName"
exit 1;

