package org.picketlink.idm.internal;

import java.util.UUID;

import org.picketlink.idm.IdGenerator;

/**
 * Default IdGenerator implementation
 * 
 * @author Shane Bryzak
 *
 */
public class DefaultIdGenerator implements IdGenerator {

    @Override
    public final String generate() {
        return UUID.randomUUID().toString();
    }

}
