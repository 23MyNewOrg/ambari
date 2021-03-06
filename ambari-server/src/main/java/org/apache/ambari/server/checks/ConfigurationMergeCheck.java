/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ambari.server.checks;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.controller.PrereqCheckRequest;
import org.apache.ambari.server.orm.entities.RepositoryVersionEntity;
import org.apache.ambari.server.state.Cluster;
import org.apache.ambari.server.state.ConfigMergeHelper;
import org.apache.ambari.server.state.ConfigMergeHelper.ThreeWayValue;
import org.apache.ambari.server.state.stack.PrereqCheckStatus;
import org.apache.ambari.server.state.stack.PrerequisiteCheck;
import org.apache.commons.lang.StringUtils;

import com.google.inject.Inject;

/**
 * Checks for configuration merge conflicts.
 */
public class ConfigurationMergeCheck extends AbstractCheckDescriptor {

  @Inject
  ConfigMergeHelper m_mergeHelper;

  public ConfigurationMergeCheck() {
    super(CheckDescription.CONFIG_MERGE);
  }

  @Override
  public boolean isApplicable(PrereqCheckRequest request) throws AmbariException {
    if (!super.isApplicable(request)) {
      return false;
    }

    String repoVersion = request.getRepositoryVersion();
    if (null == repoVersion) {
      return false;
    }

    RepositoryVersionEntity rve = repositoryVersionDaoProvider.get().findMaxByVersion(repoVersion);
    if (null == rve) {
      return false;
    }

    Cluster cluster = clustersProvider.get().getCluster(request.getClusterName());

    if (rve.getStackId().equals(cluster.getCurrentStackVersion())) {
      return false;
    }

    return true;
  }


  /**
   * The following logic determines if a warning is generated for config merge
   * issues:
   * <ul>
   *   <li>A value that has been customized from HDP 2.2.x.x no longer exists in HDP 2.3.x.x</li>
   *   <li>A value that has been customized from HDP 2.2.x.x has changed its default value between HDP 2.2.x.x and HDP 2.3.x.x</li>
   * </ul>
   */
  @Override
  public void perform(PrerequisiteCheck prerequisiteCheck, PrereqCheckRequest request)
      throws AmbariException {

    RepositoryVersionEntity rve = repositoryVersionDaoProvider.get().findMaxByVersion(request.getRepositoryVersion());

    Map<String, Map<String, ThreeWayValue>> changes =
        m_mergeHelper.getConflicts(request.getClusterName(), rve.getStackId());

    Set<String> failedTypes = new HashSet<String>();

    for (Entry<String, Map<String, ThreeWayValue>> entry : changes.entrySet()) {
      for (Entry<String, ThreeWayValue> configEntry : entry.getValue().entrySet()) {

        ThreeWayValue twv = configEntry.getValue();
        if (null == twv.oldStackValue) { // !!! did not exist and in the map means changed
          failedTypes.add(entry.getKey());
          prerequisiteCheck.getFailedOn().add(entry.getKey() + "/" + configEntry.getKey());
        } else if (!twv.oldStackValue.equals(twv.savedValue)) {  // !!! value customized
          if (null == twv.newStackValue || // !!! not in new stack
              !twv.oldStackValue.equals(twv.newStackValue)) { // !!! or the default value changed
            failedTypes.add(entry.getKey());
            prerequisiteCheck.getFailedOn().add(entry.getKey() + "/" + configEntry.getKey());
          }
        }
      }
    }

    if (prerequisiteCheck.getFailedOn().size() > 0) {
      prerequisiteCheck.setStatus(PrereqCheckStatus.WARNING);
      String failReason = getFailReason(prerequisiteCheck, request);

      prerequisiteCheck.setFailReason(String.format(failReason, StringUtils.join(
          failedTypes, ", ")));

    } else {
      prerequisiteCheck.setStatus(PrereqCheckStatus.PASS);
    }
  }



}
