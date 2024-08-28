package com.ericsson.esc.bsf.main;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.Flow;

public class bsfmgr3 {
    public static void main(String argv[]) {
        System.out.println("Hello World from bsfmgr3!");

        

        // doRxJavaTrails();
        boolean first = true;
        for (long N=2; N<=100000; N++)
            if (didFindSolutionFor(N, false)) {
                if (first)
                    first = false;
                else
                    System.out.print(",");

                System.out.print(N);
            }
        System.out.println();


        /*
        Flowable<Long>    naturalNumbers = Flowable.rangeLong(1, 1);
        Flowable<Boolean> hasSolution    = naturalNumbers.subscribeOn(Schedulers.io());
		*/
    }

    private static void doRxJavaTrails() {
        Flowable.just("Hello world").subscribe(System.out::println);

        Flowable<Integer> flow = Flowable.range(1, 5)
                .map(v -> v* v)
                .filter(v -> v % 3 == 0)
                ;

        flow.subscribe(System.out::println);

        Observable.create(emitter -> {
            while (!emitter.isDisposed()) {
                long time = System.currentTimeMillis();
                emitter.onNext(time);
                if (time % 2 != 0) {
                    emitter.onError(new IllegalStateException("Odd millisecond!"));
                    break;
                }
            }
        }).subscribe(System.out::println, Throwable::printStackTrace);
    }

    // some long runner to simulate a heavy background tasks
    //
    // In case you're intereseted what it does:
    // Find 2 numbers out of the first N naturals whose product equals to the sum of
    // the remaining numbers. The algorithm is intenteionally no optimized.
    private static boolean didFindSolutionFor(long N, boolean verbose) {
        boolean foundSolution = false;
        long    sum           = N * (N+1) / 2;

        if (verbose)
            System.out.print("1.." + N + ", sum=" + sum);

        for (long y=1; y <= N; y++)
            for (long x=1; x != y && x<=N; x++)
                if (x * y == sum - x - y) {
                    if (verbose) {
                        if (!foundSolution)
                            System.out.println();


                        System.out.println("    x=" + x + ", y=" + y + ":");
                        System.out.print("    ");

                        boolean first = true;
                        for (long i=1; i<=N; i++) {
                            if (i != x && i != y) {
                                if (first)
                                    first = false;
                                else
                                    System.out.print("+");

                                System.out.print(i);
                            }
                        }
                        System.out.println(" = " + (sum - x - y) + " = x * y");
                    }

                    foundSolution = true;
                }

        if (verbose && !foundSolution)
            System.out.println(" ==> no solution.");

        return foundSolution;
    }
}
