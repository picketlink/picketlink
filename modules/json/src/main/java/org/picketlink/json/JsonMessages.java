/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.json;

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;
import org.picketlink.json.jose.crypto.Algorithm;

/**
 * An instance of {@link MessageBundle} from JBoss Logging
 *
 * @author Stefan Guilhen
 * @since Jul 10, 2012
 */
@MessageBundle(projectCode = "PLJSON")
public interface JsonMessages {

    JsonMessages MESSAGES = Messages.getBundle(JsonMessages.class);

    // common messages: 1-19
    @Message(id = 1, value = "The argument %s cannot be null")
    JsonException invalidNullArgument(String argName);

    @Message(id = 2, value = "Could not encode token.")
    JsonException failEncodeToken(@Cause Throwable throwable);

    @Message(id = 3, value = "The given string does not represent a valid JWT token [%s].")
    JsonException invalidFormat(String json);

    @Message(id = 4, value = "Missing header [%s].")
    JsonException missingHeader(String header);

    // crypto messages: 20-39
    @Message(id = 20, value = "No such algorithm [%s].")
    JsonException cryptoNoSuchAlgorithm(String name, @Cause Throwable throwable);

    @Message(id = 21, value = "Could not verify signature using algorithm [%s].")
    JsonException cryptoSignatureValidationFailed(Algorithm algorithm, @Cause Throwable throwable);

    @Message(id = 22, value = "Could not create signature using algorithm [%s].")
    JsonException cryptoSignatureFailed(Algorithm algorithm, @Cause Throwable throwable);

    @Message(id = 23, value = "Error creating token instance from type [%s].")
    JsonException couldNotCreateToken(Class<?> type, @Cause Throwable t);

    @Message(id = 24, value = "Invalid signature for JSON [%s].")
    JsonException cryptoInvalidSignature(String json);

    @Message(id = 25, value = "Signature not present: [%s].")
    JsonException cryptoSignatureNotPresent(String json);

    @Message(id = 26, value = "Could not parse key: [%s].")
    JsonException cryptoCouldNotParseKey(String json, @Cause Throwable t);

    @Message(id = 28, value = "Unsupported key type: [%s].")
    JsonException cryptoUnsupportedKey(String keyType);

    //FIXME: need to review JWE and support JSR-353
//    @Message(id = 1, value = "keydatalen should be a multiple of 8")
//    IllegalArgumentException keyDataLenError();
//
//    @Message(id = 2, value = "keydatalen is larger than Maximum Value allowed by Unsigned Integer data type.")
//    IllegalArgumentException keyDataLenLarge();
//
//    @Message(id = 4, value = "Hash Length is too large")
//    RuntimeException hashLengthTooLarge();
//
//    @Message(id = 8, value = "JSON Serialization Failed.")
//    RuntimeException jsonSerializationFailed(@Cause Throwable e);
//
//    @Message(id = 9, value = "JSON Encryption Header Missing.")
//    IllegalStateException jsonEncryptionHeaderMissing();
//
//    @Message(id = 10, value = "Invalid Base64 character found: %s")
//    RuntimeException invalidBase64CharacterMessage(byte character);
//
//    @Message(id = 11, value = "Error reading Base64 stream: nothing to read")
//    IOException errorReadingBase64Stream();
//
//    @Message(id = 12, value = "Error decoding from file %s")
//    IllegalStateException errorDecodingFromFile(String fileName, @Cause Throwable throwable);
//
//    @Message(id = 13, value = "Error decoding from file %s: file is too big (%s bytes)")
//    IllegalStateException errorDecodingFromBigInputFile(String fileName, long fileSize);
//
//    @Message(id = 14, value = "JSON Web Keys Missing.")
//    RuntimeException jsonWebKeysMissing();
//
//    @Message(id = 15, value = "Wrong Type of JSON Key.")
//    RuntimeException wrongJsonKey();
//
//    @Message(id = 16, value = "Error encoding from file %s")
//    IllegalStateException errorEncodingFromFile(String fileName, @Cause Throwable throwable);
//
//    @Message(id = 17, value = "Base64 input not properly padded")
//    IOException invalidBase64Padding();
//
//    @Message(id = 18, value = "Invalid Number of tokens: %s")
//    IllegalArgumentException invalidNumberOfTokens(int num);
//
//    @Message(id = 19, value = "Does not match: %s")
//    RuntimeException doesNotMatch(String str);
//
//    @Message(id = 21, value = "Error that can be ignored.")
//    RuntimeException ignorableError(@Cause Throwable e);
//
//    @Message(id = 2, value = "Processing Exception.")
//    ProcessingException processingException(@Cause Throwable throwable);
}
