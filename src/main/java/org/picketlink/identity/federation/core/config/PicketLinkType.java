package org.picketlink.identity.federation.core.config;

import org.picketlink.identity.federation.core.handler.config.Handlers;

/**
 * Overall consolidated config holder type
 * @author anil saldhana
 */
public class PicketLinkType {
    private ProviderType idpOrSP;
    private Handlers handlers;
    private STSType stsType;

    public ProviderType getIdpOrSP() {
        return idpOrSP;
    }

    public void setIdpOrSP(ProviderType idpOrSP) {
        this.idpOrSP = idpOrSP;
    }

    public Handlers getHandlers() {
        return handlers;
    }

    public void setHandlers(Handlers handlers) {
        this.handlers = handlers;
    }

    public STSType getStsType() {
        return stsType;
    }

    public void setStsType(STSType stsType) {
        this.stsType = stsType;
    }
}