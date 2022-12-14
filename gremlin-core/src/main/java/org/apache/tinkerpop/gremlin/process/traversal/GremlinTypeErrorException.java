/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.process.traversal;

/**
 * We use this exception type to signify ERROR in Ternary Boolean Logics. This exception will never propagate to the
 * user for boolean value expressions, it will always be ultimately reduced to FALSE.
 */
public class GremlinTypeErrorException extends RuntimeException {
    public GremlinTypeErrorException() {
    }

    public GremlinTypeErrorException(final String message) {
        super(message);
    }

    public GremlinTypeErrorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public GremlinTypeErrorException(final Throwable cause) {
        super(cause);
    }
}
