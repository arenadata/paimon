/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.paimon.hive;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests for {@link HiveCatalog#qualifyWarehousePath}. */
public class HiveCatalogWarehousePathTest {

    @Test
    public void testQualifyWarehousePathAgainstDefaultFsWithoutTrailingSlash() {
        assertThat(
                        HiveCatalog.qualifyWarehousePath(
                                        "/apps/paimon/warehouse", URI.create("ofs://omservice"))
                                .toString())
                .isEqualTo("ofs://omservice/apps/paimon/warehouse");

        assertThat(
                        HiveCatalog.qualifyWarehousePath(
                                        "apps/paimon/warehouse", URI.create("hdfs://nn:8020"))
                                .toString())
                .isEqualTo("hdfs://nn:8020/apps/paimon/warehouse");
    }

    @Test
    public void testQualifyWarehousePathAgainstDefaultFsWithTrailingSlash() {
        assertThat(
                        HiveCatalog.qualifyWarehousePath(
                                        "/apps/paimon/warehouse", URI.create("ofs://omservice/"))
                                .toString())
                .isEqualTo("ofs://omservice/apps/paimon/warehouse");
    }

    @Test
    public void testQualifyWarehousePathLeavesSchemedLocationsUnchanged() {
        URI ozone = URI.create("ofs://omservice");
        java.net.URI kept =
                HiveCatalog.qualifyWarehousePath("hdfs:///apps/paimon/warehouse", ozone).toUri();
        assertThat(kept.getScheme()).isEqualTo("hdfs");
        assertThat(kept.getPath()).isEqualTo("/apps/paimon/warehouse");
        assertThat(
                        HiveCatalog.qualifyWarehousePath(
                                        "ofs://omservice/apps/paimon/warehouse", ozone)
                                .toString())
                .isEqualTo("ofs://omservice/apps/paimon/warehouse");
    }
}
