/**
 * <h1>Cross-Origin Resource Sharing (CORS) Filter</h1>
 *
 * <p>This package provides implementation for the
 * <a href="http://www.w3.org/TR/cors/">Cross-Origin Resource Sharing
 * (CORS)</a> mechanism for making cross-site HTTP requests from web browsers.
 * The CORS W3C working draft stabilized in 2009 and as of 2010 CORS is
 * supported by all major browsers such as Firefox, Safari, Chrome and IE.
 *
 * <p>The CORSRequestsPathAuthorizer can be configured to tag the allowed CORS HTTP requests
 * with {@code HttpServletRequest.addAttribute} to provide the following
 * information to downstream handlers:
 *
 * <ul>
 *     <li>{@code cors.isCorsRequest} {Boolean} Indicates if the HTTP request
 *         is CORS.
 *     <li>{@code cors.origin} {String} the value of the "Origin" header,
 *         {@code null} if undefined.
 *     <li>{@code cors.requestType} {String} If the request is CORS, indicates
 *         its type - "actual" for simple / actual or "preflight".
 *     <li>{@code cors.requestHeaders} {String} if the request is CORS
 *         preflight, the value of the "Access-Control-Request-Headers" header,
 *         {@code null} if undefined.
 * </ul>
 *
 * <p>This package provides CORS implementation for the W3C
 * <a href="http://www.w3.org/TR/2014/REC-cors-20140116/">recommendation</a>
 * from 16 January 2014.</p>
 *
 * <p>Supported CORS request types:</p>
 *
 * <ul>
 *         <li><a href="http://www.w3.org/TR/2014/REC-cors-20140116/#resource-requests">Simple / actual requests</a></li>
 *         <li><a href="http://www.w3.org/TR/2014/REC-cors-20140116/#resource-preflight-requests">Preflight requests</a></li>
 *
 * </ul>
 *
 * <p>Supported CORS headers:</p>
 *
 * <ul>
 *     <li>Request headers:
 *         <ul>
 *             <li><a href="http://www.w3.org/TR/2014/REC-cors-20140116/#origin-request-header">Origin</a></li>
 *             <li><a href="http://www.w3.org/TR/2014/REC-cors-20140116/#access-control-request-method-request-header">Access-Control-Request-Method</a></li>
 *             <li><a href="http://www.w3.org/TR/2014/REC-cors-20140116/#access-control-request-headers-request-header">Access-Control-Request-Headers</a></li>
 *         </ul>
 *     </li>
 *
 *     <li>Response headers:
 *         <ul>
 *             <li><a href="http://www.w3.org/TR/2014/REC-cors-20140116/#access-control-allow-origin-response-header">Access-Control-Allow-Origin</a></li>
 *             <li><a href="http://www.w3.org/TR/2014/REC-cors-20140116/#access-control-allow-credentials-response-header">Access-Control-Allow-Credentials</a></li>
 *             <li><a href="http://www.w3.org/TR/2014/REC-cors-20140116/#access-control-allow-methods-response-header">Access-Control-Allow-Methods</a></li>
 *             <li><a href="http://www.w3.org/TR/2014/REC-cors-20140116/#access-control-allow-headers-response-header">Access-Control-Allow-Headers</a></li>
 *
 *             <li><a href="http://www.w3.org/TR/2014/REC-cors-20140116/#access-control-expose-headers-response-header">Access-Control-Expose-Headers</a></li>
 *             <li><a href="http://www.w3.org/TR/2014/REC-cors-20140116/#access-control-max-age-response-header">Access-Control-Max-Age</a></li>
 *         </ul>
 *     </li>
 * </ul>
 */
package org.picketlink.http.internal.cors;

