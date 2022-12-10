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
package org.apache.tinkerpop.gremlin.process.traversal.strategy.verification;

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.ComparatorHolder;
import org.apache.tinkerpop.gremlin.process.traversal.step.LambdaHolder;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;

/**
 * {@code LambdaRestrictionStrategy} does not allow lambdas to be used in a {@link Traversal}. The contents of a lambda
 * cannot be analyzed/optimized and thus, reduces the ability of other {@link TraversalStrategy} instances to reason
 * about the traversal. This strategy is not activated by default. However, graph system providers may choose to make
 * this a default strategy in order to ensure their respective strategies are better able to operate.
 * <p/>
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @example <pre>
 * __.out().map(v -&gt; v.get().value("name"))           // throws an IllegalStateException
 * __.out().filter(v -&gt; v.bulk() &gt; 2)              // throws an IllegalStateException
 * __.choose(v -&gt; v.sack() == 1,out(),in())           // throws an IllegalStateException
 * __.select().by(v -&gt; v.get().id())                  // throws an IllegalStateException
 * __.order().by(a,b -&gt; a &gt; b)                        // throws an IllegalStateException
 * </pre>
 */
public final class LambdaRestrictionStrategy extends AbstractTraversalStrategy<TraversalStrategy.VerificationStrategy> implements TraversalStrategy.VerificationStrategy {

    private static final LambdaRestrictionStrategy INSTANCE = new LambdaRestrictionStrategy();

    private LambdaRestrictionStrategy() {
    }

    @Override
    public void apply(final Traversal.Admin<?, ?> traversal) {
        for (final Step<?, ?> step : traversal.getSteps()) {
            if ((step instanceof LambdaHolder || step instanceof ComparatorHolder) && step.toString().contains("lambda"))
                throw new VerificationException("The provided traversal contains a lambda step: " + step, traversal);
        }
    }

    public static LambdaRestrictionStrategy instance() {
        return INSTANCE;
    }
}
