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

import java.util.UUID;

import com.vmware.xenon.common.FactoryService;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceDocument;
import com.vmware.xenon.common.ServiceDocumentDescription;
import com.vmware.xenon.common.ServiceDocumentDescription.PropertyUsageOption;
import com.vmware.xenon.common.StatefulService;
import com.vmware.xenon.services.common.ExampleService.ExampleServiceState;

public class VcsTenantService extends StatefulService {
    public static final String FACTORY_LINK = ServiceUriPaths.CORE + "/VcsTenant";

    public static FactoryService createFactory() {
        return FactoryService.createIdempotent(VcsTenantService.class);
    }

    public static class VcsTenantState extends ServiceDocument {
        @UsageOption(option = PropertyUsageOption.ID)
        public String id;

        @UsageOption(option = PropertyUsageOption.REQUIRED)
        public String name;

        @UsageOption(option = PropertyUsageOption.OPTIONAL)
        public String description;
    }

    public VcsTenantService() {
        super(VcsTenantState.class);
        super.toggleOption(ServiceOption.PERSISTENCE, true);
        super.toggleOption(ServiceOption.REPLICATION, true);
        super.toggleOption(ServiceOption.INSTRUMENTATION, true);
        super.toggleOption(ServiceOption.OWNER_SELECTION, true);
    }

    @Override
    public void handleStart(Operation post) {
        if (!post.hasBody()) {
            post.fail(new IllegalArgumentException("body is required"));
            return;
        }
        VcsTenantState newState = post.getBody(VcsTenantState.class);
        if (newState.id == null || newState.id.isEmpty()) {
            newState.id = UUID.randomUUID().toString();
        }
        post.setBody(newState).complete();
    }

    @Override
    public void handlePut(Operation op) {
        if (!op.hasBody()) {
            op.fail(new IllegalArgumentException("body is required"));
            return;
        }

        VcsTenantState newState = op.getBody(VcsTenantState.class);
        VcsTenantState currentState = getState(op);
        ServiceDocumentDescription documentDescription = getStateDescription();
        if (ServiceDocument.equals(documentDescription, currentState, newState)) {
            op.setStatusCode(Operation.STATUS_CODE_NOT_MODIFIED);
        } else {
            setState(op, newState);
        }

        op.complete();
    }

    @Override
    public void handlePatch(Operation patch) {
        VcsTenantState currentState = getState(patch);
        VcsTenantState newState = patch.getBody(VcsTenantState.class);
        mergeState(currentState, newState);
        patch.complete();
    }

    private void mergeState(VcsTenantState currentState, VcsTenantState newState) {
        if (newState.name != null) {
            currentState.name = newState.name;
        }
        if (newState.description != null) {
            currentState.description = newState.description;
        }
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
}
