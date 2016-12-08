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

import com.vmware.xenon.common.FactoryService;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceDocument;
import com.vmware.xenon.common.ServiceDocumentDescription.PropertyDescription;
import com.vmware.xenon.common.ServiceDocumentDescription.PropertyIndexingOption;
import com.vmware.xenon.common.ServiceDocumentDescription.PropertyUsageOption;
import com.vmware.xenon.common.StatefulService;
import com.vmware.xenon.common.Utils;

/**
 * Example service
 */
public class VSphereDockerHost extends StatefulService {

    public static final String FACTORY_LINK = ServiceUriPaths.CORE + "/vSphereDockerHost";

    /**
     * Create a default factory service that starts instances of this service on POST.
     * This method is optional, {@code FactoryService.create} can be used directly
     */
    public static FactoryService createFactory() {
        return FactoryService.create(VSphereDockerHost.class);
    }

    public static class ExampleServiceState extends ServiceDocument {
        public static final String FIELD_NAME_ESX_IP = "esxIPAddress";
        public static final String FIELD_NAME_TENANT_NAME = "tenantName";

        @UsageOption(option = PropertyUsageOption.AUTO_MERGE_IF_NOT_NULL)
        @PropertyOptions(indexing = PropertyIndexingOption.SORT)
        @UsageOption(option = PropertyUsageOption.OPTIONAL)
        public String tenantName;
        @UsageOption(option = PropertyUsageOption.REQUIRED)
        public String esxIPAddress;
    }

    public VSphereDockerHost() {
        super(ExampleServiceState.class);
        toggleOption(ServiceOption.PERSISTENCE, true);
        toggleOption(ServiceOption.REPLICATION, true);
        toggleOption(ServiceOption.INSTRUMENTATION, true);
        toggleOption(ServiceOption.OWNER_SELECTION, true);
    }

    @Override
    public void handleStart(Operation startPost) {
        // Example of state validation on start:
        // 1) Require that an initial state is provided
        // 2) Require that the name field is not null
        // A service could also accept a POST with no body or invalid state and correct it

        if (!startPost.hasBody()) {
            startPost.fail(new IllegalArgumentException("initial state is required"));
            return;
        }

        ExampleServiceState s = startPost.getBody(ExampleServiceState.class);
        if (s.esxIPAddress == null) {
            startPost.fail(new IllegalArgumentException("name is required"));
            return;
        }

        startPost.complete();
    }

    @Override
    public void handlePut(Operation put) {

        put.complete();
    }

    @Override
    public void handlePatch(Operation patch) {
        updateState(patch);
        // updateState method already set the response body with the merged state
        patch.complete();
    }

    private ExampleServiceState updateState(Operation update) {
        // A DCP service handler is state-less: Everything it needs is provided as part of the
        // of the operation. The body and latest state associated with the service are retrieved
        // below.
        ExampleServiceState body = getBody(update);
        ExampleServiceState currentState = getState(update);

        // use helper that will merge automatically current state, with state supplied in body.
        // Note the usage option PropertyUsageOption.AUTO_MERGE_IF_NOT_NULL has been set on the
        // "name" field.
        boolean hasStateChanged = Utils.mergeWithState(getStateDescription(),
                currentState, body);

        updateDocument(body, currentState, hasStateChanged);

        if (body.documentExpirationTimeMicros != currentState.documentExpirationTimeMicros) {
            currentState.documentExpirationTimeMicros = body.documentExpirationTimeMicros;
        }

        // response has latest, updated state
        update.setBody(currentState);
        return currentState;
    }

    private boolean updateDocument(ExampleServiceState body,
            ExampleServiceState currentState, boolean hasStateChanged) {
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
        ExampleServiceState currentState = getState(delete);
        ExampleServiceState st = delete.getBody(ExampleServiceState.class);
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
                ExampleServiceState.FIELD_NAME_TENANT_NAME);

        // instruct the index to enable SORT on this field.
        pdTenantName.indexingOptions.add(PropertyIndexingOption.SORT);

        // instruct the index to only keep the most recent N versions
        return template;
    }
}
