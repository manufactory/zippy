/*
 * Copyright (c) 2012, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.graal.hotspot.test;

import static com.oracle.graal.api.meta.MetaUtil.*;
import static java.lang.reflect.Modifier.*;

import java.lang.reflect.*;

import org.junit.*;

import com.oracle.graal.api.code.*;
import com.oracle.graal.api.meta.*;
import com.oracle.graal.api.runtime.*;
import com.oracle.graal.compiler.test.*;
import com.oracle.graal.hotspot.meta.*;
import com.oracle.graal.nodes.*;

public class InstalledCodeExecuteHelperTest extends GraalCompilerTest {

    private static final int ITERATIONS = 10000;
    private final MetaAccessProvider metaAccessProvider;
    Object[] argsToBind;

    public static void main(String[] args) throws NoSuchMethodException, SecurityException, InvalidInstalledCodeException {
        InstalledCodeExecuteHelperTest main = new InstalledCodeExecuteHelperTest();
        main.testWithTime();
    }

    public InstalledCodeExecuteHelperTest() {
        this.metaAccessProvider = Graal.getRequiredCapability(MetaAccessProvider.class);
    }

    @Ignore
    @Test
    public void test1() throws NoSuchMethodException, SecurityException, InvalidInstalledCodeException {
        final Method fooMethod = InstalledCodeExecuteHelperTest.class.getMethod("foo", Object.class, Object.class, Object.class);
        final HotSpotResolvedJavaMethod fooJavaMethod = (HotSpotResolvedJavaMethod) metaAccessProvider.lookupJavaMethod(fooMethod);
        final HotSpotInstalledCode fooCode = (HotSpotInstalledCode) getCode(fooJavaMethod, parse(fooMethod));

        argsToBind = new Object[]{fooCode};

        final Method benchmarkMethod = InstalledCodeExecuteHelperTest.class.getMethod("benchmark", HotSpotInstalledCode.class);
        final ResolvedJavaMethod benchmarkJavaMethod = metaAccessProvider.lookupJavaMethod(benchmarkMethod);
        final HotSpotInstalledCode installedBenchmarkCode = (HotSpotInstalledCode) getCode(benchmarkJavaMethod, parse(benchmarkMethod));

        Assert.assertEquals(Integer.valueOf(42), benchmark(fooCode));

        Assert.assertEquals(Integer.valueOf(42), installedBenchmarkCode.executeVarargs(argsToBind[0]));

    }

    public void testWithTime() throws NoSuchMethodException, SecurityException, InvalidInstalledCodeException {
        final Method fooMethod = InstalledCodeExecuteHelperTest.class.getMethod("foo", Object.class, Object.class, Object.class);
        final HotSpotResolvedJavaMethod fooJavaMethod = (HotSpotResolvedJavaMethod) metaAccessProvider.lookupJavaMethod(fooMethod);
        final HotSpotInstalledCode fooCode = (HotSpotInstalledCode) getCode(fooJavaMethod, parse(fooMethod));

        argsToBind = new Object[]{fooCode};

        final Method benchmarkMethod = InstalledCodeExecuteHelperTest.class.getMethod("benchmark", HotSpotInstalledCode.class);
        final ResolvedJavaMethod benchmarkJavaMethod = metaAccessProvider.lookupJavaMethod(benchmarkMethod);
        final HotSpotInstalledCode installedBenchmarkCodeotInstalledCode = (HotSpotInstalledCode) getCode(benchmarkJavaMethod, parse(benchmarkMethod));

        long start = System.currentTimeMillis();
        benchmark(fooCode);
        long end = System.currentTimeMillis();
        System.out.println((end - start));

        start = System.currentTimeMillis();
        installedBenchmarkCodeotInstalledCode.executeVarargs(argsToBind[0]);
        end = System.currentTimeMillis();
        System.out.println((end - start));
    }

    public static Integer benchmark(HotSpotInstalledCode code) throws InvalidInstalledCodeException {
        int val = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            val = (Integer) code.execute(null, null, null);
        }
        return val;
    }

    public static Integer foo(@SuppressWarnings("unused") Object a1, @SuppressWarnings("unused") Object a2, @SuppressWarnings("unused") Object a3) {
        return 42;
    }

    @Override
    protected StructuredGraph parse(Method m) {
        StructuredGraph graph = super.parse(m);
        if (argsToBind != null) {
            Object receiver = isStatic(m.getModifiers()) ? null : this;
            Object[] args = argsWithReceiver(receiver, argsToBind);
            JavaType[] parameterTypes = signatureToTypes(runtime.lookupJavaMethod(m));
            assert parameterTypes.length == args.length;
            for (int i = 0; i < argsToBind.length; i++) {
                LocalNode local = graph.getLocal(i);
                Constant c = Constant.forBoxed(parameterTypes[i].getKind(), argsToBind[i]);
                ConstantNode replacement = ConstantNode.forConstant(c, runtime, graph);
                local.replaceAtUsages(replacement);
            }
        }
        return graph;
    }
}
