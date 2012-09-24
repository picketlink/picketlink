package org.picketlink.cdi.permission.internal;

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

import org.picketlink.cdi.permission.Permission;
import org.picketlink.cdi.permission.PermissionQuery;
import org.picketlink.cdi.permission.internal.JPAPermissionStoreConfig.StoreMetadata;
import org.picketlink.cdi.permission.spi.PermissionStore;

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
    private PermissionHandlerPolicy permissionHandlerPolicy;

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
        else
        {
            // TODO - we could probably do a reverse lookup of the resource if we had a ResourceLocator API or something like that
            throw new SecurityException("Invalid permission query - must specify resource or resources");
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
            // Resources have been specified by the query, use them to locate
            // the permissions
            List<Permission> results = new ArrayList<Permission>();
            
            Collection<Object> knownResources = new ArrayList<Object>(); 
            if (query.getResource() != null)
            {
                knownResources.add(query.getResource());
            }
            else if (query.getResources() != null)
            {
                knownResources.addAll(query.getResources());
            }            
            
            // Iterate through each permission store and execute a separate query
            for (StoreMetadata meta : resourceMetadata.keySet())
            {
                Query permissionQuery = buildPermissionQuery(meta, query, em);
                               
                for (Object result : permissionQuery.getResultList())
                {
                    Object identifier = meta.getAclIdentifier().getValue(result);
                    
                    Object resource = permissionHandlerPolicy.lookupResource(identifier.toString(), knownResources);
                                        
                    Set<String> resourcePermissions = permissionHandlerPolicy.convertResourcePermissions(
                            resource, meta.getAclPermission().getValue(result));
                    
                    for (String permission : resourcePermissions)
                    {
                     // TODO still need to add the recipient
                        results.add(new Permission(resource, null, permission));    
                    }
                }               
            }
            
            return results;
        }
                
        // TODO Auto-generated method stubobj
        return null;
    }
    
    private Query buildPermissionQuery(StoreMetadata meta, PermissionQuery query, EntityManager em)
    {                
        Map<String,Object> paramValues = new HashMap<String,Object>();
        
        StringBuilder queryText = new StringBuilder();
        StringBuilder criteriaText = new StringBuilder();
        
        queryText.append("SELECT P FROM ");
        queryText.append(meta.getStoreClass().getName());
        queryText.append(" P WHERE ");
        
        if (query.getResource() != null)
        {
            criteriaText.append("P.");
            criteriaText.append(meta.getAclIdentifier().getName());
            criteriaText.append(" = :IDENTIFIER");
            
            /*
             * IF the resource has an exclusive ACLStore, then we will use the natural value of the identifier
             * to set the parameter value.  If the resource permissions are stored in the general store, 
             * then we'll use the general purpose "generated" identifier.
             */
            if (meta.getResourceClass() != null)
            {
                paramValues.put("IDENTIFIER", permissionHandlerPolicy.getNaturalIdentifier(query.getResource()));   
            }
            else
            {
                paramValues.put("IDENTIFIER", permissionHandlerPolicy.getGeneratedIdentifier(query.getResource()));
            }
        }
        else if (query.getResources() != null)
        {
            
        }
        
        if (query.getRecipient() != null)
        {
            if (criteriaText.length() > 0)
            {
                criteriaText.append(" AND ");
            }
            criteriaText.append("P.");
            criteriaText.append(meta.getAclRecipient().getName());
            criteriaText.append(" = :RECIPIENT");      
            paramValues.put("RECIPIENT", query.getRecipient().getKey());
        }
        
        queryText.append(criteriaText);
        
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
                
                if (store.getResourceClass() != null)
                {
                    store.getAclIdentifier().setValue(p, permissionHandlerPolicy.getNaturalIdentifier(permission.getResource()));
                }
                else
                {
                    store.getAclIdentifier().setValue(p, permissionHandlerPolicy.getGeneratedIdentifier(permission.getResource()));
                }
                store.getAclRecipient().setValue(p, permission.getRecipient().getKey());
                store.getAclPermission().setValue(p, permission.getPermission());                
                
                em.persist(p);
                
                return true;
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
