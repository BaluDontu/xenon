#/bin/bash
set -x
# script to install vmdkops VIB on a given esx host
source common.sh

# Validate arguments
if [ $# != 1 ]; then
   echo "$0  <esx-IP>"
   exit 1
fi

if [ $1 == "--help" ] || [ $1 == "--h" ]; then
   echo "$0  <esx-IP>"
   exit 1
fi

ESX_IP=$1

`isDockerVibInstalled $ESX_IP`
ExitOnFail 0 "VIB $VIB_NAME already installed on $ESX_IP" 0

scp $VIB_PATH root@$ESX_IP:/tmp/ >> /dev/null
ssh root@$ESX_IP "esxcli software vib install --no-sig-check -v /tmp/vmware-esx-vmdkops-0.8.1.8613173.vib | grep \"VIBs Installed\" | grep \"vmdkops\""
ExitOnFail 0 "Failed to install VIB $VIB_NAME on $ESX_IP" 1
exit 0;

