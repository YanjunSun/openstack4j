package org.openstack4j.openstack.storage.object.internal;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openstack4j.openstack.storage.object.domain.SwiftHeaders.ACCOUNT_METADATA_PREFIX;
import static org.openstack4j.openstack.storage.object.domain.SwiftHeaders.ACCOUNT_REMOVE_METADATA_PREFIX;
import static org.openstack4j.openstack.storage.object.domain.SwiftHeaders.ACCOUNT_TEMPORARY_URL_KEY;

import java.util.Map;

import org.openstack4j.api.storage.ObjectStorageAccountService;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.core.transport.HttpRequest;
import org.openstack4j.core.transport.HttpResponse;
import org.openstack4j.model.storage.object.SwiftAccount;
import org.openstack4j.openstack.internal.BaseOpenStackService;
import org.openstack4j.openstack.storage.object.domain.MetaHeaderRequestWrapper;
import org.openstack4j.openstack.storage.object.functions.MetadataToHeadersFunction;
import org.openstack4j.openstack.storage.object.functions.ParseAccountFunction;

/**
 * The Object Storage Account based services
 * 
 * @author Jeremy Unruh
 */
public class ObjectStorageAccountServiceImpl extends BaseOpenStackService implements ObjectStorageAccountService {

    public ObjectStorageAccountServiceImpl() {
        super(ServiceType.OBJECT_STORAGE);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SwiftAccount get() {
        return ParseAccountFunction.INSTANCE.apply(head(Void.class, "").executeWithResponse());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateMetadata(Map<String, String> metadata) {
        checkNotNull(metadata);
        return invokeMetadata(ACCOUNT_METADATA_PREFIX, metadata);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteMetadata(Map<String, String> metadata) {
        checkNotNull(metadata);
        return invokeMetadata(ACCOUNT_REMOVE_METADATA_PREFIX, metadata);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateTemporaryUrlKey(String temporaryUrlKey) {
        checkNotNull(temporaryUrlKey);
        return isResponseSuccess(post(Void.class, "").header(ACCOUNT_TEMPORARY_URL_KEY, temporaryUrlKey).executeWithResponse(), 204);
    }

    private boolean invokeMetadata(String prefix, Map<String, String> metadata) {
        Invocation<Void> invocation = post(Void.class, "");
        applyMetaData(prefix, metadata, invocation.getRequest());
        return isResponseSuccess(invocation.executeWithResponse(), 204);
    }
    
    private <R> void applyMetaData(String prefix, Map<String, String> metadata, HttpRequest<R> req) {
        MetaHeaderRequestWrapper<R> wrapper = MetaHeaderRequestWrapper.of(prefix, metadata, req);
        MetadataToHeadersFunction.<R>create().apply(wrapper);
    }
    
    private boolean isResponseSuccess(HttpResponse res, int status) {
        return res.getStatus() == status;
    }
}
