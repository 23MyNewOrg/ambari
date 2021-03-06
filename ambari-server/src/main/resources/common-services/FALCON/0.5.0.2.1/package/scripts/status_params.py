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
from ambari_commons import OSCheck

config = Script.get_config()

if OSCheck.is_windows_family():
  falcon_win_service_name = "falcon"
  hadoop_user = config["configurations"]["cluster-env"]["hadoop.user.name"]
  falcon_user = hadoop_user
else:
  falcon_pid_dir = config['configurations']['falcon-env']['falcon_pid_dir']
  server_pid_file = format('{falcon_pid_dir}/falcon.pid')

  # Security related/required params
  hostname = config['hostname']
  security_enabled = config['configurations']['cluster-env']['security_enabled']
  hadoop_conf_dir = "/etc/hadoop/conf"
  kinit_path_local = functions.get_kinit_path(default('/configurations/kerberos-env/executable_search_paths', None))
  tmp_dir = Script.get_tmp_dir()
  falcon_conf_dir_prefix = "/etc/falcon"
  falcon_conf_dir = format("{falcon_conf_dir_prefix}/conf")
  hdfs_user = config['configurations']['hadoop-env']['hdfs_user']
  falcon_user = config['configurations']['falcon-env']['falcon_user']
