#/bin/bash
#set -x

VIB_PATH=/tmp/scripts/vmware-esx-vmdkops-0.8.1.8613173.vib
VIB_NAME=esx-vmdkops-service
LOG_FILE=/tmp/dod-be-scripts.log
LOG_TERSE=3
LOG_DEBUG=2
LOG_WARNING=1
LOG_ERROR=0
LOG_LEVEL=$LOG_DEBUG

# check if VIB is already present
# takes ESX IP as parameter
isDockerVibInstalled() {
   vib_details=`REXEC "root@$ESX_IP" "esxcli software vib list | grep vmdkops"`
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
      LOG $LOG_ERROR "$2"
      exit $3;
   fi
}

# Log a given message.
#param1 : LOG LEVEL
#param2 : lOG MESSAGE
LOG() {
   if [ $LOG_LEVEL -le $1 ] ; then
      echo $2 >> $LOG_FILE
   fi
}

# Function to run a given command on given machine.
# param1: machine IP along with User name user@machine-ip
# param2: command to execute
REXEC() {
   LOG $LOG_DEBUG "Executing on $1, cmd : $2"
   output=`ssh $1 $2`
   ret=`echo $?`
   LOG $LOG_DEBUG "RET : $ret , OUTPUT: $output"
   echo $output
   return $ret
}



