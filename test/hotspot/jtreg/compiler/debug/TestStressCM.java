/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

package compiler.debug;

import jdk.test.lib.process.OutputAnalyzer;
import jdk.test.lib.process.ProcessTools;
import jdk.test.lib.Asserts;

/*
 * @test
 * @bug 8253765
 * @requires vm.debug == true & vm.compiler2.enabled
 * @summary Tests that, when compiling with StressLCM or StressGCM, using the
 *          same seed results in the same compilation, and using different seeds
 *          results in different compilations (the latter does not necessarily
 *          hold for all pairs of seeds). The output of PrintOptoStatistics is
 *          used to compare among compilations, instead of the more intuitive
 *          TraceOptoPipelining which prints non-deterministic memory addresses.
 * @library /test/lib /
 * @run driver compiler.debug.TestStressCM StressLCM
 * @run driver compiler.debug.TestStressCM StressGCM
 */

public class TestStressCM {

    static String optoStats(String stressOpt, int stressSeed) throws Exception {
        String className = TestStressCM.class.getName();
        String[] procArgs = {
            "-Xcomp", "-XX:-TieredCompilation",
            "-XX:CompileOnly=" + className + "::sum",
            "-XX:+PrintOptoStatistics", "-XX:+" + stressOpt,
            "-XX:StressSeed=" + stressSeed, className, "10"};
        ProcessBuilder pb  = ProcessTools.createJavaProcessBuilder(procArgs);
        OutputAnalyzer out = new OutputAnalyzer(pb.start());
        return out.getStdout();
    }

    static void sum(int n) {
        int acc = 0;
        for (int i = 0; i < n; i++) acc += i;
        System.out.println(acc);
    }

    public static void main(String[] args) throws Exception {
        if (args[0].startsWith("Stress")) {
            String stressOpt = args[0];
            Asserts.assertEQ(optoStats(stressOpt, 10), optoStats(stressOpt, 10),
                "got different optimization stats for the same seed");
            Asserts.assertNE(optoStats(stressOpt, 10), optoStats(stressOpt, 20),
                "got the same optimization stats for different seeds");
        } else if (args.length > 0) {
            sum(Integer.parseInt(args[0]));
        }
    }
}
