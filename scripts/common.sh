#/bin/bash
#set -x

VIB_PATH=vmware-esx-vmdkops-0.8.1.8613173.vib
VIB_NAME=esx-vmdkops-service

# check if VIB is already present
# takes ESX IP as parameter
isDockerVibInstalled() {
   vib_details=`ssh root@$ESX_IP "esxcli software vib list | grep vmdkops"`
   ret=`echo $?`
   vib_name=`echo $vib_details | awk ' { print $1 }'`
   if [ $ret == 0 ]; then
      if [ $vib_name == "esx-vmdkops-service" ] ; then
         return 1
      fi
   fi
   return 0
}

# exit if return value is not as expected
# param1 : expected return value
# param2 : log message.
# param3 : exit value
ExitOnFail() {
   ret=`echo $?`
   if [ $ret != $1 ] ; then
      echo $2
      exit $3;
   fi
}

