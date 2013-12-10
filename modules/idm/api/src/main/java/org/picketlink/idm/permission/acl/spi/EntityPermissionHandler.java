package org.picketlink.idm.permission.acl.spi;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.IdentityManagementException;

/**
 *
 * @author Shane Bryzak
 *
 */
public class EntityPermissionHandler extends BaseAbstractPermissionHandler {

    private static final String SEPARATOR = ":";

    private Class<? extends Annotation> entityAnnotationClass = null;

    private Class<? extends Annotation> idAnnotationClass = null;

    private Map<Class<?>, List<Property<Serializable>>> idProperties =
            new ConcurrentHashMap<Class<?>, List<Property<Serializable>>>();

    public EntityPermissionHandler() {
        try {
            entityAnnotationClass = (Class<? extends Annotation>) Class.forName("javax.persistence.Entity");
            idAnnotationClass = (Class<? extends Annotation>) Class.forName("javax.persistence.Id");
        } catch (ClassNotFoundException ex) {
            // Entity permissions not supported
        }
    }

    @Override
    public boolean canHandle(Class<?> resourceClass) {
        return entityAnnotationClass != null && resourceClass.isAnnotationPresent(entityAnnotationClass);
    }

    private List<Property<Serializable>> getIdProperties(Object resource) {
        Class<?> resourceClass = unwrapResourceClass(resource);

        if (!idProperties.containsKey(resourceClass)) {
            queryIdProperties(resourceClass);
        }
        return idProperties.get(resourceClass);
    }

    private synchronized void queryIdProperties(Class<?> resourceClass) {
        if (!idProperties.containsKey(resourceClass)) {
            List<Property<Serializable>> props = PropertyQueries.<Serializable>createQuery(resourceClass)
                .addCriteria(new AnnotatedPropertyCriteria(idAnnotationClass))
                .getResultList();

            // If there is more than one property sort them in ascending alphabetical order
            if (props.size() > 1) {
                Collections.sort(props, new Comparator<Property<Serializable>>() {
                    @Override
                    public int compare(Property<Serializable> a, Property<Serializable> b) {
                        return a.getName().compareTo(b.getName());
                    }
                });
            }

            idProperties.put(resourceClass, props);
        }
    }

    /**
     * TODO we only support @Id identifiers at the moment, still need to add support for @EmbeddedId etc
     *
     * @param resource
     * @return
     */
    @Override
    public Serializable getIdentifier(Object resource) {
        List<Property<Serializable>> props = getIdProperties(resource);

        // If the entity has a single @Id property, return it
        if (props.size() == 1) {
            return props.get(0).getValue(resource);
        // Otherwise return a colon-separated list
        } else if (props.size() > 1) {
            StringBuilder sb = new StringBuilder();
            for (Property<Serializable> p : props) {
                if (sb.length() > 0) {
                    sb.append(SEPARATOR);
                }
                sb.append(p.getValue(resource).toString());
            }
            return sb.toString();
        } else {
            throw new IdentityManagementException(
                    String.format("Could not locate @Id property for specified resource [%s]", resource));
        }
    }

    @Override
    public Class<?> unwrapResourceClass(Object resource) {
        return resource.getClass();
    }


}
