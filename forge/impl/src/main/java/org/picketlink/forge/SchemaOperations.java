package org.picketlink.forge;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Date;

import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.parser.java.JavaSourceFactory;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.parser.java.Field;
import org.jboss.forge.parser.java.JavaClass;
import org.picketlink.idm.credential.storage.EncodedPasswordStorage;
import org.picketlink.idm.jpa.annotations.AttributeClass;
import org.picketlink.idm.jpa.annotations.AttributeName;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.CredentialClass;
import org.picketlink.idm.jpa.annotations.CredentialProperty;
import org.picketlink.idm.jpa.annotations.EffectiveDate;
import org.picketlink.idm.jpa.annotations.ExpiryDate;
import org.picketlink.idm.jpa.annotations.Identifier;
import org.picketlink.idm.jpa.annotations.IdentityClass;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.RelationshipClass;
import org.picketlink.idm.jpa.annotations.RelationshipDescriptor;
import org.picketlink.idm.jpa.annotations.RelationshipMember;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.jpa.annotations.entity.ManagedCredential;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;

/**
 * Creates a JPA entity schema for an identity model
 *
 * @author Shane Bryzak
 */
public class SchemaOperations {

    @Inject FacetFactory facetFactory;

    @Inject JavaSourceFactory javaSourceFactory;

    public void createIdentitySchema(Project project, String packageName) 
            throws FileNotFoundException {
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);

        java.saveJavaSource(createIdentityEntity(packageName));
        java.saveJavaSource(createIdentityAttributeEntity(packageName));
        java.saveJavaSource(createCredentialEntity(packageName));
        java.saveJavaSource(createRelationshipEntity(packageName));
        java.saveJavaSource(createRelationshipIdentityEntity(packageName));
    }

    private JavaClass createIdentityEntity(String packageName) {
        String lineSeparator = System.getProperty("line.separator");

        JavaClass javaClass = javaSourceFactory.create(JavaClass.class)
                .setName("IdentityEntity")
                .setPublic()
                .addInterface(Serializable.class);

        javaClass.setPackage(packageName);
        javaClass.addAnnotation(Entity.class);
        javaClass.addAnnotation(IdentityManaged.class).setClassValue(IdentityType.class);

        // Create the id property
        Field<JavaClass> f = javaClass.addField("private String id = null;");
        f.addAnnotation(Id.class);
        f.addAnnotation(Identifier.class);

        javaClass.addMethod("public String getId() {" + lineSeparator +
                "  return id;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setId(String id) {" + lineSeparator +
                "  this.id = id;" + lineSeparator + 
                "}");

        // Create the identity class property
        f = javaClass.addField("private String identityClass = null;");
        f.addAnnotation(IdentityClass.class);

        javaClass.addMethod("public String getIdentityClass() {" + lineSeparator +
                "  return identityClass;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setIdentityClass(String identityClass) {" + lineSeparator +
                "  this.identityClass = identityClass; " + lineSeparator +
                "}");

        // Create the created date property
        f = javaClass.addField("private Date createdDate = null;");
        f.addAnnotation(Temporal.class).setEnumValue(TemporalType.TIMESTAMP);
        f.addAnnotation(AttributeValue.class);

        javaClass.addMethod("public Date getCreatedDate() {" + lineSeparator +
                "  return createdDate;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setCreatedDate(Date createdDate) {" + lineSeparator +
                "  this.createdDate = createdDate;" + lineSeparator +
                "}");

        // Create the expiration date property
        f = javaClass.addField("private Date expirationDate = null;");
        f.addAnnotation(Temporal.class).setEnumValue(TemporalType.TIMESTAMP);
        f.addAnnotation(AttributeValue.class);

        javaClass.addMethod("public Date getExpirationDate() {" + lineSeparator +
                "  return expirationDate;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setExpirationDate(Date expirationDate) {" + lineSeparator +
                "  this.expirationDate = expirationDate;" + lineSeparator +
                "}");

        // Add the necessary imports
        javaClass.addImport(Entity.class);
        javaClass.addImport(IdentityManaged.class);
        javaClass.addImport(IdentityType.class);
        javaClass.addImport(Serializable.class);
        javaClass.addImport(Id.class);
        javaClass.addImport(Identifier.class);
        javaClass.addImport(IdentityClass.class);
        javaClass.addImport(Date.class);
        javaClass.addImport(Temporal.class);
        javaClass.addImport(TemporalType.class);

        return javaClass;
    }

    private JavaClass createIdentityAttributeEntity(String packageName) {
        String lineSeparator = System.getProperty("line.separator");

        JavaClass javaClass = javaSourceFactory.create(JavaClass.class)
                .setName("IdentityAttribute")
                .setPublic()
                .addInterface(Serializable.class);

        javaClass.setPackage(packageName);
        javaClass.addAnnotation(Entity.class);
        javaClass.addAnnotation(IdentityManaged.class).setClassValue(IdentityType.class);

        // Create the id property
        Field<JavaClass> f = javaClass.addField("private Long id = null;");
        f.addAnnotation(Id.class);
        f.addAnnotation(GeneratedValue.class);

        javaClass.addMethod("public Long getId() {" + lineSeparator +
                "  return id;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setId(Long id) {" + lineSeparator +
                "  this.id = id;" + lineSeparator + 
                "}");

        // Create the owner property
        f = javaClass.addField("private IdentityEntity owner;");
        f.addAnnotation(ManyToOne.class);
        f.addAnnotation(OwnerReference.class);

        javaClass.addMethod("public IdentityEntity getIdentity() {" + lineSeparator +
                "  return identity;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setIdentity(IdentityEntity identity) {" + lineSeparator +
                "  this.identity = identity;" + lineSeparator +
                "}");

        // Create the attributeClass property
        f = javaClass.addField("private String attributeClass;");
        f.addAnnotation(AttributeClass.class);

        javaClass.addMethod("public String getAttributeClass() {" + lineSeparator +
                "  return attributeClass;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setAttributeClass(String attributeClass) {" + lineSeparator +
                "  this.attributeClass = attributeClass;" + lineSeparator +
                "}");

        // Create the name property
        f = javaClass.addField("private String name;");
        f.addAnnotation(AttributeName.class);

        javaClass.addMethod("public String getName() {" + lineSeparator +
                "  return name;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setName(String name) {" + lineSeparator +
                "  this.name = name;" + lineSeparator +
                "}");

        // Create the value property
        f = javaClass.addField("private String value;");
        f.addAnnotation(AttributeValue.class);

        javaClass.addMethod("public String getValue() {" + lineSeparator +
                "  return value;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setValue(String value) {" + lineSeparator +
                "  this.value = value;" + lineSeparator +
                "}");

        javaClass.addImport(Entity.class);
        javaClass.addImport(IdentityManaged.class);
        javaClass.addImport(ManyToOne.class);
        javaClass.addImport(OwnerReference.class);
        javaClass.addImport(AttributeClass.class);
        javaClass.addImport(AttributeName.class);
        javaClass.addImport(AttributeValue.class);
        javaClass.addImport(Serializable.class);

        return javaClass;
    }

    private JavaClass createCredentialEntity(String packageName) {
        String lineSeparator = System.getProperty("line.separator");

        JavaClass javaClass = javaSourceFactory.create(JavaClass.class)
                .setName("PasswordCredential")
                .setPublic()
                .addInterface(Serializable.class);

        javaClass.setPackage(packageName);
        javaClass.addAnnotation(Entity.class);
        javaClass.addAnnotation(ManagedCredential.class).setClassValue(EncodedPasswordStorage.class);

        // Create the id property
        Field<JavaClass> f = javaClass.addField("private Long id = null;");
        f.addAnnotation(Id.class);
        f.addAnnotation(GeneratedValue.class);

        javaClass.addMethod("public Long getId() {" + lineSeparator +
                "  return id;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setId(Long id) {" + lineSeparator +
                "  this.id = id;" + lineSeparator + 
                "}");

        // Create the owner property
        f = javaClass.addField("private IdentityEntity owner;");
        f.addAnnotation(OwnerReference.class);
        f.addAnnotation(ManyToOne.class);

        javaClass.addMethod("public IdentityEntity getOwner() {" + lineSeparator +
                "  return owner;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setOwner(IdentityEntity owner) {" + lineSeparator +
                "  this.owner = owner;" + lineSeparator +
                "}");

        // Create the credentialClass property
        f = javaClass.addField("private String credentialClass;");
        f.addAnnotation(CredentialClass.class);

        javaClass.addMethod("public String getCredentialClass() {" + lineSeparator +
                "  return credentialClass;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setCredentialClass(String credentialClass) {" + lineSeparator +
                "  this.credentialClass = credentialClass;" + lineSeparator +
                "}");

        // Create the effectiveDate property
        f = javaClass.addField("private Date effectiveDate = null;");
        f.addAnnotation(EffectiveDate.class);
        f.addAnnotation(Temporal.class).setEnumValue(TemporalType.TIMESTAMP);

        javaClass.addMethod("public Date getEffectiveDate()  {" + lineSeparator +
                "  return effectiveDate;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setEffectiveDate(Date effectiveDate) {" + lineSeparator +
                "  this.effectiveDate = effectiveDate;" + lineSeparator +
                "}");

        // Create the expiryDate property
        f = javaClass.addField("private Date expiryDate = null;");
        f.addAnnotation(ExpiryDate.class);
        f.addAnnotation(Temporal.class).setEnumValue(TemporalType.TIMESTAMP);

        javaClass.addMethod("public Date getExpiryDate() {" + lineSeparator +
                "  return expiryDate;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setExpiryDate(Date expiryDate) {" + lineSeparator +
                "  this.expiryDate = expiryDate;" + lineSeparator +
                "}");

        // Create the encodedHash property
        f = javaClass.addField("private String encodedHash = null;");
        f.addAnnotation(CredentialProperty.class).setStringValue("name", "encodedHash");

        javaClass.addMethod("public String getEncodedHash() {" + lineSeparator +
                "  return encodedHash;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setEncodedHash(String encodedHash) {" + lineSeparator +
                "  this.encodedHash = encodedHash;" + lineSeparator +
                "}");

        javaClass.addImport(Entity.class);
        javaClass.addImport(CredentialClass.class);
        javaClass.addImport(ManagedCredential.class);
        javaClass.addImport(Date.class);
        javaClass.addImport(EffectiveDate.class);
        javaClass.addImport(ExpiryDate.class);
        javaClass.addImport(Temporal.class);
        javaClass.addImport(TemporalType.class);
        javaClass.addImport(Serializable.class);

        return javaClass;
    }

    private JavaClass createRelationshipEntity(String packageName) {
        String lineSeparator = System.getProperty("line.separator");

        JavaClass javaClass = javaSourceFactory.create(JavaClass.class)
                .setName("RelationshipEntity")
                .setPublic()
                .addInterface(Serializable.class);

        javaClass.setPackage(packageName);
        javaClass.addAnnotation(Entity.class);
        javaClass.addAnnotation(IdentityManaged.class).setClassValue(Relationship.class);

        // Create the id property
        Field<JavaClass> f = javaClass.addField("private Long id = null;");
        f.addAnnotation(Id.class);
        f.addAnnotation(GeneratedValue.class);

        javaClass.addMethod("public Long getId() {" + lineSeparator +
                "  return id;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setId(Long id) {" + lineSeparator +
                "  this.id = id;" + lineSeparator + 
                "}");

        // Create the relationshipClass property
        f = javaClass.addField("private String relationshipClass = null;");
        f.addAnnotation(RelationshipClass.class);

        javaClass.addMethod("public String getRelationshipClass() {"  + lineSeparator +
                "  return relationshipClass;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setRelationshipClass(String relationshipClass) {" + lineSeparator +
                "  this.relationshipClass = relationshipClass;" + lineSeparator +
                "}");

        javaClass.addImport(Entity.class);
        javaClass.addImport(Serializable.class);
        javaClass.addImport(IdentityManaged.class);
        javaClass.addImport(Relationship.class);
        javaClass.addImport(RelationshipClass.class);
        return javaClass;
    }

    private JavaClass createRelationshipIdentityEntity(String packageName) {
        String lineSeparator = System.getProperty("line.separator");

        JavaClass javaClass = javaSourceFactory.create(JavaClass.class)
                .setName("RelationshipIdentity")
                .setPublic()
                .addInterface(Serializable.class);

        javaClass.setPackage(packageName);
        javaClass.addAnnotation(Entity.class);
        javaClass.addAnnotation(IdentityManaged.class).setClassValue(Relationship.class);

        // Create the id property
        Field<JavaClass> f = javaClass.addField("private Long id = null;");
        f.addAnnotation(Id.class);
        f.addAnnotation(GeneratedValue.class);

        javaClass.addMethod("public Long getId() {" + lineSeparator +
                "  return id;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setId(Long id) {" + lineSeparator +
                "  this.id = id;" + lineSeparator + 
                "}");

        // Create the owner property
        f = javaClass.addField("private RelationshipEntity owner = null;");
        f.addAnnotation(OwnerReference.class);
        f.addAnnotation(ManyToOne.class);

        javaClass.addMethod("public RelationshipEntity getOwner() {" + lineSeparator +
                "  return owner;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setOwner(RelationshipEntity owner) {" + lineSeparator +
                "  this.owner = owner;" + lineSeparator +
                "}");

        // Create the descriptor property
        f = javaClass.addField("private String descriptor = null;");
        f.addAnnotation(RelationshipDescriptor.class);

        javaClass.addMethod("public String getDescriptor() {"  + lineSeparator +
                "  return descriptor;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setDescriptor(String descriptor) {" + lineSeparator +
                "  this.descriptor = descriptor;" + lineSeparator +
                "}");

        // Create the identity property
        f = javaClass.addField("private IdentityEntity identity = null;");
        f.addAnnotation(RelationshipMember.class);

        javaClass.addMethod("public IdentityEntity getIdentity() {" + lineSeparator +
                "  return identity;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setIdentity(IdentityEntity identity) {" + lineSeparator +
                "  this.identity = identity;" + lineSeparator +
                "}");

        javaClass.addImport(Entity.class);
        javaClass.addImport(OwnerReference.class);
        javaClass.addImport(ManyToOne.class);
        javaClass.addImport(Serializable.class);
        javaClass.addImport(IdentityManaged.class);
        javaClass.addImport(Relationship.class);
        javaClass.addImport(RelationshipDescriptor.class);
        javaClass.addImport(RelationshipMember.class);
        return javaClass;
    }
}
