#!/usr/bin/env python
"""
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

"""

from resource_management import *
from ambari_commons.os_check import OSCheck

config = Script.get_config()

if OSCheck.is_windows_family():
  hive_metastore_win_service_name = "metastore"
  hive_client_win_service_name = "hwi"
  hive_server_win_service_name = "hiveserver2"
  webhcat_server_win_service_name = "templeton"
else:
  hive_pid_dir = config['configurations']['hive-env']['hive_pid_dir']
  hive_pid = 'hive-server.pid'

  hive_metastore_pid = 'hive.pid'

  hcat_pid_dir = config['configurations']['hive-env']['hcat_pid_dir'] #hcat_pid_dir
  webhcat_pid_file = format('{hcat_pid_dir}/webhcat.pid')

  process_name = 'mysqld'
  if OSCheck.is_suse_family() or OSCheck.is_ubuntu_family():
    daemon_name = 'mysql'
  else:
    daemon_name = 'mysqld'

  # Security related/required params
  hostname = config['hostname']
  security_enabled = config['configurations']['cluster-env']['security_enabled']
  hadoop_conf_dir = "/etc/hadoop/conf"
  kinit_path_local = functions.get_kinit_path(default('/configurations/kerberos-env/executable_search_paths', None))
  tmp_dir = Script.get_tmp_dir()
  hdfs_user = config['configurations']['hadoop-env']['hdfs_user']
  hive_user = config['configurations']['hive-env']['hive_user']
  hive_conf_dir = "/etc/hive/conf"
  webhcat_user = config['configurations']['hive-env']['webhcat_user']
  webhcat_conf_dir = '/etc/hive-webhcat/conf'