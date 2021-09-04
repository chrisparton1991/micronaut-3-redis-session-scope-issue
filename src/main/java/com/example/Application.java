package com.example;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.http.scope.RequestScope;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}

/** Trivial auth provider that always creates a session, just hit the endpoint with any Authorization header. */
@Singleton
class AuthProvider implements AuthenticationProvider {
    @Override
    public Publisher<AuthenticationResponse> authenticate(HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
        return Mono.create(emitter -> {
            emitter.success(AuthenticationResponse.success("user"));
        });
    }
}

/** Minimal request-scoped bean, the hi() method throws an exception when Redis sessions are enabled. */
@RequestScope
class RequestContext {
    void hi() {
        System.out.println("Hi");
    }
}

@Controller("test")
@Secured("isAuthenticated()")
class TestController {

    private final RequestContext requestContext;

    public TestController(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Get
    public void hi() {
        // Throws java.lang.IllegalMonitorStateException: attempt to unlock read lock, not locked by current thread.
        requestContext.hi();
    }
}
