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

import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests for {@link RetryingMetaStoreClientFactory}. */
class RetryingMetaStoreClientFactoryTest {

    @Test
    void newSynchronizedClientUsesFactoryClassWhenPresent() {
        IMetaStoreClient client = recordingClient(new ArrayList<>());

        IMetaStoreClient wrapped =
                RetryingMetaStoreClientFactory.newSynchronizedClient(
                        client, FakeSynchronizedClientFactory.class.getName());

        assertThat(wrapped).isSameAs(FakeSynchronizedClientFactory.RESULT);
        assertThat(FakeSynchronizedClientFactory.lastDelegate).isSameAs(client);
    }

    @Test
    void newSynchronizedClientFallsBackWhenFactoryClassMissing() throws Exception {
        List<String> calls = new ArrayList<>();
        IMetaStoreClient client = recordingClient(calls);

        IMetaStoreClient wrapped =
                RetryingMetaStoreClientFactory.newSynchronizedClient(
                        client, "no.such.SynchronizedMetaStoreClient");

        assertThat(Proxy.isProxyClass(wrapped.getClass())).isTrue();
        wrapped.close();
        assertThat(calls).containsExactly("close");
    }

    private static IMetaStoreClient recordingClient(List<String> calls) {
        return (IMetaStoreClient)
                Proxy.newProxyInstance(
                        RetryingMetaStoreClientFactoryTest.class.getClassLoader(),
                        new Class<?>[] {IMetaStoreClient.class},
                        (proxy, method, args) -> {
                            calls.add(method.getName());
                            return null;
                        });
    }

    /** Mimics the Hive 4 {@code SynchronizedMetaStoreClient} factory. */
    public static class FakeSynchronizedClientFactory {

        static final IMetaStoreClient RESULT = recordingClient(new ArrayList<>());
        static IMetaStoreClient lastDelegate;

        public static IMetaStoreClient newSynchronizedClient(IMetaStoreClient delegate) {
            lastDelegate = delegate;
            return RESULT;
        }
    }
}
