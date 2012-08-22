package org.jboss.picketlink.cdi.permission.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.picketlink.cdi.permission.Permission;
import org.jboss.picketlink.cdi.permission.PermissionQuery;
import org.jboss.picketlink.cdi.permission.internal.JPAPermissionStoreConfig.StoreMetadata;
import org.jboss.picketlink.cdi.permission.spi.PermissionStore;

/**
 * A PermissionStore implementation backed by a JPA datasource
 *
 */
@ApplicationScoped
public class JPAPermissionStore implements PermissionStore
{   
    @Inject 
    private Instance<EntityManager> entityManagerInstance;    
    
    @Inject 
    private JPAPermissionStoreConfig config;
    
    @Inject
    private IdentifierPolicy identifierPolicy;

    @Override
    public List<Permission> getPermissions(PermissionQuery query)
    {
        EntityManager em = entityManagerInstance.get();
        
        Map<StoreMetadata, Set<Object>> resourceMetadata = new HashMap<StoreMetadata, Set<Object>>();                
                
        if (query.getResources() != null)
        {            
            for (Object resource : query.getResources()) 
            {
                Class<?> resourceClass = resource.getClass();
                StoreMetadata meta = (config.getStores().containsKey(resourceClass)) ? 
                        config.getStores().get(resourceClass) : config.getGeneralStore();
                
                if (!resourceMetadata.containsKey(meta))
                {
                    resourceMetadata.put(meta, new HashSet<Object>());
                }
                resourceMetadata.get(meta).add(resource);
            }
        }
        else if (query.getResource() != null)
        {
            Class<?> resourceClass = query.getResource().getClass();
            StoreMetadata meta = (config.getStores().containsKey(resourceClass)) ? 
                    config.getStores().get(resourceClass) : config.getGeneralStore();
            
            if (!resourceMetadata.containsKey(meta))
            {
                resourceMetadata.put(meta, new HashSet<Object>());
            }
            resourceMetadata.get(meta).add(query.getResource());
        }
                
        if (resourceMetadata.isEmpty())
        {
            // No resources specified in query - we need to query every known permission store and retrieve
            // all permissions for the specified query parameters
            
            for (StoreMetadata meta : config.getStores().values())
            {
                Query permissionQuery = buildPermissionQuery(meta, query, em);
                
            }
        }
        else
        {
            List<Permission> results = new ArrayList<Permission>();
            
            // Iterate through each permission store and execute a separate query
            for (StoreMetadata meta : resourceMetadata.keySet())
            {
                Query permissionQuery = buildPermissionQuery(meta, query, em);
            }
        }
                
        // TODO Auto-generated method stubobj
        return null;
    }
    
    private Query buildPermissionQuery(StoreMetadata meta, PermissionQuery query, EntityManager em)
    {                
        Map<String,Object> paramValues = new HashMap<String,Object>();
        
        StringBuilder queryText = new StringBuilder();
        queryText.append("SELECT P FROM ");
        queryText.append(meta.getStoreClass().getName());
        queryText.append(" P WHERE ");
        
        if (query.getResource() != null)
        {
            queryText.append(meta.getAclIdentifier().getName());
            queryText.append(" = :IDENTIFIER");            
            paramValues.put("IDENTIFIER", identifierPolicy.getIdentifier(query.getResource()));
        }
        else if (query.getResources() != null)
        {
            
        }
        
        if (query.getRecipient() != null)
        {
            queryText.append(meta.getAclRecipient().getName());
            queryText.append(" = :RECIPIENT");      
            paramValues.put("RECIPIENT", query.getRecipient().getKey());
        }
        
        Query q = em.createQuery(queryText.toString());
        
        for (String param : paramValues.keySet())
        {
            q.setParameter(param, paramValues.get(param));
        }            
        
        // TODO apply the range if specified
        
        return q;
    }

    @Override
    public boolean grantPermission(Permission permission)
    {
        EntityManager em = entityManagerInstance.get();
        
        StoreMetadata store = findStoreForResource(permission.getResource());
        
        // First query for existing permission records
        PermissionQuery pq = new PermissionQuery(this);
        pq.setResource(permission.getResource());
        pq.setRecipient(permission.getRecipient());
        
        Query q = buildPermissionQuery(store, pq, em);
        List<?> results = q.getResultList();
        
        if (results.isEmpty())
        {
            // If there is no existing record, create a new one
            try
            {
                Object p = store.getStoreClass().newInstance();
                
                store.getAclIdentifier().setValue(p, identifierPolicy.getIdentifier(permission.getResource()));
                store.getAclRecipient().setValue(p, permission.getRecipient().getKey());
                store.getAclPermission().setValue(p, permission.getPermission());                
                
                em.persist(p);
            }
            catch (IllegalAccessException ex)
            {
                throw new SecurityException("Error creating new permission", ex);
            }
            catch (InstantiationException ex)
            {
                throw new SecurityException("Error creating new permission", ex);
            }
        }
        else
        {
            // Otherwise update the existing record with the new permission
        }
        
        return false;
    }

    @Override
    public boolean grantPermissions(Collection<Permission> permissions)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean revokePermission(Permission permission)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean revokePermissions(Collection<Permission> permissions)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> listAvailableActions(Object target)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clearPermissions(Object target)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isEnabled()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    private StoreMetadata findStoreForResource(Object resource)
    {
        for (Class<?> cls : config.getStores().keySet())
        {
            if (cls.isInstance(resource))
            {
                return config.getStores().get(cls);
            }
        }

        return config.getGeneralStore();
    }    
}
