/*
 * Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.dropwizard.revolver.http;

/**
 * @author phaneesh
 */
public class RevolversHttpHeaders {

    public static final String TXN_ID_HEADER = "X-TRANSACTION-ID";
    public static final String REQUEST_ID_HEADER = "X-REQUEST-ID";
    public static final String PARENT_REQUEST_ID_HEADER = "X-PARENT-REQUEST-ID";
    public static final String TIMESTAMP_HEADER = "X-REQUEST-TIMESTAMP";
    public static final String CLIENT_HEADER = "X-CLIENT-ID";
    public static final String CALL_MODE_HEADER = "X-CALL-MODE";
    public static final String MAILBOX_ID_HEADER = "X-MAILBOX-ID";

}
