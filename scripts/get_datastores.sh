#/bin/bash
#set -x
# script to install vmdkops VIB on a given esx host
source common.sh

ALL_ARGs="$0 called with arguments - $*"
LOG $LOG_DEBUG "$ALL_ARGs"
# Validate arguments
if [ $# > 1 ] && [ $1 == "--help" ] || [ $1 == "--h" ]; then
   LOG $LOG_ERROR "$0  <comma-separated-esx-IP-list>"
   echo "$0  <comma-separated-esx-IP-list>"
   exit 1
fi
if [ $# != 1 ]; then
   LOG $LOG_ERROR "$0  <comma-separated-esx-IP-list>"
   echo "$0  <comma-separated-esx-IP-list>"
   exit 1
fi

IP_LIST=$1
DS_LIST=""
for esx_ip in `echo $1 | sed s/,/\ /` ; do
   echo $esx_ip
   list=`REXEC "root@$esx_ip" "esxcli storage filesystem list | sed 1,2d | awk '{ print \\\$2 }'"`
   DS_LIST=`echo $DS_LIST $list`
done
echo $DS_LIST | sed -e s/\s\+/\n/g | sort -u
exit 0;

