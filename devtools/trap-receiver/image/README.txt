
[Useful Information]

- All captured traps are logged into /var/log/snmptrap.log

- All configuration files are placed in the /etc/snmp directory

- All relevant MIBs are placed in the /usr/share/snmp/mibs directory

[Tips]

- To watch incoming traps use the following command:

  (Hint: Execute outside of this pod)

  kubectl -n 5g-bsf-$USER logs eric-snmp-trap-receiver --follow

- To spawn trap locally and capture it use the following command:
  
  [SNMPv2]

  (Hint: Replace <COMMUNITY_NAME> and execute inside this pod)
  
  snmptrap -v 2c -c <COMMUNITY_NAME> localhost '' NET-SNMP-EXAMPLES-MIB::netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 123456

