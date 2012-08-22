package org.jboss.picketlink.cdi.permission.internal;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.persistence.Entity;

import org.jboss.picketlink.cdi.internal.util.properties.Property;
import org.jboss.picketlink.cdi.internal.util.properties.query.AnnotatedPropertyCriteria;
import org.jboss.picketlink.cdi.internal.util.properties.query.PropertyQueries;
import org.jboss.picketlink.cdi.permission.annotations.ACLIdentifier;
import org.jboss.picketlink.cdi.permission.annotations.ACLPermission;
import org.jboss.picketlink.cdi.permission.annotations.ACLRecipient;
import org.jboss.picketlink.cdi.permission.annotations.ACLResourceClass;
import org.jboss.picketlink.cdi.permission.annotations.ACLStore;
import org.jboss.picketlink.idm.SecurityConfigurationException;

/**
 *
 */
@ApplicationScoped
public class JPAPermissionStoreConfig implements Extension
{
    private StoreMetadata generalStore = null;
    
    private Map<Class<?>, StoreMetadata> storeMap = new HashMap<Class<?>, StoreMetadata>();
    
    public StoreMetadata getGeneralStore()
    {
        return generalStore;
    }
    
    public Map<Class<?>, StoreMetadata> getStores()
    {
        return storeMap;
    }
    
    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event,
            final BeanManager beanManager) 
    {
        if (event.getAnnotatedType().isAnnotationPresent(Entity.class)) 
        {
            AnnotatedType<X> type = event.getAnnotatedType();
            
            if (type.isAnnotationPresent(ACLStore.class)) 
            {
                ACLStore store = type.getAnnotation(ACLStore.class);
                if (ACLStore.GENERAL.class.equals(store.value()))
                {
                    if (generalStore == null)
                    {
                        generalStore = new StoreMetadata(type.getJavaClass());
                    }
                    else
                    {
                        throw new SecurityConfigurationException(
                                "More than one entity bean has been configured as a general ACL store - " +
                                "conflicting bean classes: " + generalStore.getStoreClass().getName() + " and " +
                                type.getJavaClass().getName());
                    }
                }
                else
                {
                    if (storeMap.containsKey(store.value()))
                    {
                        throw new SecurityConfigurationException(
                                "More than one entity bean has been configured to store ACL permissions for class " +
                                store.value().getName() + " - conflicting classes: " +
                                storeMap.get(store.value()).getStoreClass().getName() + " and " + 
                                type.getJavaClass().getName());
                    }
                    else
                    {
                        storeMap.put(store.value(), new StoreMetadata(type.getJavaClass()));
                    }
                }
            }                
        }
    }
    
    class StoreMetadata
    {
        private Class<?> storeClass;
        private Property<String> aclIdentifier;
        private Property<Object> aclPermission;
        private Property<String> aclRecipient;
        private Property<String> aclResourceClass;
        
        public StoreMetadata(Class<?> storeClass)
        {
            this.storeClass = storeClass;
            validateStore();             
        }
        
        private void validateStore()
        {
            aclIdentifier = PropertyQueries.<String>createQuery(storeClass)
                    .addCriteria(new AnnotatedPropertyCriteria(ACLIdentifier.class))
                    .getFirstResult();
            
            if (aclIdentifier == null)
            {
                throw new SecurityConfigurationException("Permission storage class " + storeClass.getName() + 
                        " must have a field annotated @ACLIdentifier");
            }
            
            aclPermission = PropertyQueries.createQuery(storeClass)
                    .addCriteria(new AnnotatedPropertyCriteria(ACLPermission.class))
                    .getFirstResult();
            
            if (aclPermission == null)
            {
                throw new SecurityConfigurationException("Permission storage class " + storeClass.getName() + 
                        " must have a field annotated @ACLPermission");
            }
            
            aclRecipient = PropertyQueries.<String>createQuery(storeClass)
                    .addCriteria(new AnnotatedPropertyCriteria(ACLRecipient.class))
                    .getFirstResult();
            
            if (aclRecipient == null)
            {
                throw new SecurityConfigurationException("Permission storage class " + storeClass.getName() + 
                        " must have a field annotated @ACLRecipient");
            }
            
            aclResourceClass = PropertyQueries.<String>createQuery(storeClass)
                    .addCriteria(new AnnotatedPropertyCriteria(ACLResourceClass.class))
                    .getFirstResult();
        }
        
        public Class<?> getStoreClass()
        {
            return storeClass;
        }
        
        public Property<String> getAclIdentifier()
        {
            return aclIdentifier;
        }
        
        public Property<Object> getAclPermission()
        {
            return aclPermission;
        }
        
        public Property<String> getAclRecipient()
        {
            return aclRecipient;
        }
        
        public Property<String> getAclResourceClass()
        {
            return aclResourceClass;
        }
    }      
}
