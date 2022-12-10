﻿#region License

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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#endregion

using System.Collections.Generic;
using Gremlin.Net.Process.Traversal;
using Gremlin.Net.Process.Traversal.Strategy.Decoration;
using Gremlin.Net.Structure.IO.GraphSON;
using Xunit;

namespace Gremlin.Net.UnitTest.Structure.IO.GraphSON
{
    public class StrategyWriterTests
    {
        /// <summary>
        /// Parameters for each test supporting multiple versions of GraphSON
        /// </summary>
        public static IEnumerable<object[]> Versions => new []
        {
            new object[] { 2 },
            new object[] { 3 }
        };

        private GraphSONWriter CreateGraphSONWriter(int version)
        {
            if (version == 3)
            {
                return new GraphSON3Writer();
            }
            return new GraphSON2Writer();
        }

        [Theory, MemberData(nameof(Versions))]
        public void ShouldSerializeSubgraphStrategyWithoutValues(int version)
        {
            var subgraphStrategy = new SubgraphStrategy();
            var writer = CreateGraphSONWriter(version);

            var graphSon = writer.WriteObject(subgraphStrategy);

            const string expected = "{\"@type\":\"g:SubgraphStrategy\",\"@value\":{}}";
            Assert.Equal(expected, graphSon);
        }

        [Theory, MemberData(nameof(Versions))]
        public void ShouldDeserializeSubgraphStrategyWithVertexCriterion(int version)
        {
            var vertexCriterionBytecode = new Bytecode();
            vertexCriterionBytecode.AddStep("has", "name", "marko");
            var vertexCriterion = new TestTraversal(vertexCriterionBytecode);
            var subgraphStrategy = new SubgraphStrategy(vertexCriterion);
            var writer = CreateGraphSONWriter(version);

            var graphSon = writer.WriteObject(subgraphStrategy);

            const string expected =
                "{\"@type\":\"g:SubgraphStrategy\",\"@value\":{\"vertices\":{\"@type\":\"g:Bytecode\",\"@value\":{\"step\":[[\"has\",\"name\",\"marko\"]]}}}}";
            Assert.Equal(expected, graphSon);
        }
    }
}