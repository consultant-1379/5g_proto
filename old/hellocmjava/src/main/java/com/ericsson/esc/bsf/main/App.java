package com.ericsson.esc.bsf.main;

import com.ericsson.esc.bsf.services.cm.CMProvider;
import com.ericsson.esc.bsf.services.cm.CMProviderFactory;
import rx.Completable;

public class App {
    public static void main(String[] args) {
        CMProvider cmProvider = CMProviderFactory.getCMProvider();

        cmProvider.startService().subscribe(() -> {
           System.out.println("CM Service available.");
        }, throwable -> {
            System.out.println("CM Service failed.");
        });
    }
}
