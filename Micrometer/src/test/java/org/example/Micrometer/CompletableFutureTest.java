package org.example.Micrometer;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureTest {

    @Test
    void handlerByFirstRef() {
        CompletableFuture<Void> cf = new CompletableFuture<>();

        cf.whenComplete((unused, throwable) ->
                System.out.println("Hello world by stacking handlers")
        );

        cf.complete(null);

        cf.join();
    }

    @Test
    void handlerBySecondRef() {
        CompletableFuture<Void> cf = new CompletableFuture<>();

        CompletableFuture<Void> cfWrapped = cf.whenComplete((unused, throwable) ->
                System.out.println("Hello world by stacked futures")
        );

        cf.complete(null);

        cfWrapped.join();
    }

    @Test
    void hanlderByWrapped() {
        CompletableFuture<Void> cf = new CompletableFuture<Void>()
                .whenComplete((unused, throwable) ->
                        System.out.println("Ya - pidoras")
                );

        cf.complete(null);

        cf.join();
    }

    @Test
    void hanlderByWrappedV2() {
        CompletableFuture<Void> cf = new CompletableFuture<>();

        CompletableFuture<Void> cf2 = cf
                .whenComplete((unused, throwable) ->
                        System.out.println("Ya - pidoras")
                );

        CompletableFuture<Void> cf3 = cf2
                .whenComplete((unused, throwable) ->
                        System.out.println("Ultragay")
                );

        cf2.complete(null);

        cf3.join();
    }
}
