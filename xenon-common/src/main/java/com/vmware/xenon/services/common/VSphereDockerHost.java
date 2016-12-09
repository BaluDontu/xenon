/*
 * Copyright (c) 2014-2015 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, without warranties or
 * conditions of any kind, EITHER EXPRESS OR IMPLIED.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.vmware.xenon.services.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.vmware.xenon.common.FactoryService;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.Service;
import com.vmware.xenon.common.ServiceDocument;
import com.vmware.xenon.common.ServiceDocumentDescription.PropertyDescription;
import com.vmware.xenon.common.ServiceDocumentDescription.PropertyIndexingOption;
import com.vmware.xenon.common.ServiceDocumentDescription.PropertyUsageOption;
import com.vmware.xenon.common.StatefulService;
import com.vmware.xenon.common.UriUtils;
import com.vmware.xenon.common.Utils;

/**
 * Example service
 */
public class VSphereDockerHost extends StatefulService {

    public static final String FACTORY_LINK = ServiceUriPaths.CORE + "/vSphereDockerHost";
    public Map<String, String> vmID_Name_Map = new HashMap<>();
    public Map<String, String> vmID_ESX = new HashMap<>();
    //Not handling multiple requests. (race condition)
    private static String vm_id = null;

    /**
     * Create a default factory service that starts instances of this service on POST.
     * This method is optional, {@code FactoryService.create} can be used directly
     */
    public static FactoryService createFactory() {
        FactoryService fs = new FactoryService(VSphereDockerHostState.class) {

            @Override
            public Service createServiceInstance() throws Throwable {
                return new VSphereDockerHost();
            }

            @Override
            public void handlePost(Operation request) {
                Map<String, String> uriParams = UriUtils.parseUriQueryParams(request.getUri());
                //                request.fail(new IllegalArgumentException(
                //                        "handlePost: Fake failure: " + hello + "url: " + request.getUri()));

                String paramValue = uriParams.get("targets");
                if (paramValue == null || paramValue.isEmpty()) {
                    this.logInfo("balu - paramValue is null");
                }
                for (String param : paramValue.split(":")) {
                    this.logInfo("balu - param:" + param);
                    if (param.startsWith("vm-")) {
                        vm_id = param;
                        this.logInfo("balu - vm_id is " + vm_id);
                        break;
                    }
                }
                super.handlePost(request);
            }
        };
        return fs;
    }

    public static class VSphereDockerHostState extends ServiceDocument {
        public static final String FIELD_NAME_ESX_IP = "esxIPAddress";
        public static final String FIELD_NAME_TENANT_NAME = "tenantName";
        public static final String FIELD_NAME_VM_ID = "vmID";
        public static final String FIELD_NAME_TARGETS = "targets";

        @UsageOption(option = PropertyUsageOption.AUTO_MERGE_IF_NOT_NULL)
        @PropertyOptions(indexing = PropertyIndexingOption.SORT)
        @UsageOption(option = PropertyUsageOption.OPTIONAL)
        public String tenantName;
        @UsageOption(option = PropertyUsageOption.REQUIRED)
        public String esxIPAddress;
        @UsageOption(option = PropertyUsageOption.REQUIRED)
        public String targets;
        @UsageOption(option = PropertyUsageOption.OPTIONAL)
        public String vmID;
    }

    public VSphereDockerHost() {
        super(VSphereDockerHostState.class);
        this.toggleOption(ServiceOption.PERSISTENCE, true);
        this.toggleOption(ServiceOption.REPLICATION, true);
        this.toggleOption(ServiceOption.INSTRUMENTATION, true);
        this.toggleOption(ServiceOption.OWNER_SELECTION, true);
        this.vmID_Name_Map.put("vm-23", "VM1");
        this.vmID_Name_Map.put("vm-18", "VM2");
        this.vmID_Name_Map.put("vm-23", "10.160.167.177");
        this.vmID_Name_Map.put("vm-18", "10.161.12.165");
    }

    private int readBashScript(String esxIp) throws IOException {
        BufferedReader read = null;
        try {
            Process proc = Runtime.getRuntime()
                    .exec("sh /tmp/scripts/install_vib.sh " + esxIp); //Whatever you want to execute
            read = new BufferedReader(new InputStreamReader(
                    proc.getInputStream(), "UTF-8"));
            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                return 1;
            }
        } catch (IOException e) {
            return 3;
        } finally {
            if (read != null) {
                read.close();
            }
        }
        return 0;
    }

    @Override
    public void handleStart(Operation startPost) {
        String esxIp = null;
        if (vm_id != null) {
            esxIp = this.vmID_Name_Map.get(vm_id);
            int ret;
            try {
                if (esxIp != null) {
                    ret = this.readBashScript(esxIp);
                    if (ret != 0) {
                        startPost.fail(
                                new IllegalArgumentException("readBashScript1 failed " + ret));
                    }
                } else {
                    startPost.fail(new IllegalArgumentException(
                            "ESX IP is not found for vm_ID: " + vm_id));
                }
            } catch (IOException e) {
                startPost.fail(new IllegalArgumentException("readBashScript failed"));
            }
        } else {
            startPost.fail(new IllegalArgumentException("vm_id is null"));
        }
        startPost.complete();
    }

    @Override
    public void handlePut(Operation put) {
        put.complete();
    }

    @Override
    public void handlePatch(Operation patch) {
        this.updateState(patch);
        // updateState method already set the response body with the merged state
        patch.complete();
    }

    private VSphereDockerHostState updateState(Operation update) {
        // A DCP service handler is state-less: Everything it needs is provided as part of the
        // of the operation. The body and latest state associated with the service are retrieved
        // below.
        VSphereDockerHostState body = this.getBody(update);
        VSphereDockerHostState currentState = this.getState(update);

        // use helper that will merge automatically current state, with state supplied in body.
        // Note the usage option PropertyUsageOption.AUTO_MERGE_IF_NOT_NULL has been set on the
        // "name" field.
        boolean hasStateChanged = Utils.mergeWithState(this.getStateDescription(),
                currentState, body);

        this.updateDocument(body, currentState, hasStateChanged);

        if (body.documentExpirationTimeMicros != currentState.documentExpirationTimeMicros) {
            currentState.documentExpirationTimeMicros = body.documentExpirationTimeMicros;
        }

        // response has latest, updated state
        update.setBody(currentState);
        return currentState;
    }

    private boolean updateDocument(VSphereDockerHostState body,
            VSphereDockerHostState currentState, boolean hasStateChanged) {
        if (body.esxIPAddress != null) {
            currentState.esxIPAddress = body.esxIPAddress;
            hasStateChanged = true;
        }
        return hasStateChanged;
    }

    @Override
    public void handleDelete(Operation delete) {
        if (!delete.hasBody()) {
            delete.complete();
            return;
        }

        // A DELETE can be used to both stop the service, mark it deleted in the index
        // so its excluded from queries, but it can also set its expiration so its state
        // history is permanently removed
        VSphereDockerHostState currentState = this.getState(delete);
        VSphereDockerHostState st = delete.getBody(VSphereDockerHostState.class);
        if (st.documentExpirationTimeMicros > 0) {
            currentState.documentExpirationTimeMicros = st.documentExpirationTimeMicros;
        }
        delete.complete();
    }

    /**
     * Provides a default instance of the service state and allows service author to specify
     * indexing and usage options, per service document property
     */
    @Override
    public ServiceDocument getDocumentTemplate() {
        ServiceDocument template = super.getDocumentTemplate();

        PropertyDescription pdTenantName = template.documentDescription.propertyDescriptions.get(
                VSphereDockerHostState.FIELD_NAME_TENANT_NAME);

        // instruct the index to enable SORT on this field.
        pdTenantName.indexingOptions.add(PropertyIndexingOption.SORT);

        // instruct the index to only keep the most recent N versions
        return template;
    }
}
