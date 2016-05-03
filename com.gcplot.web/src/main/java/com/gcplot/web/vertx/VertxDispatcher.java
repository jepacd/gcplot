package com.gcplot.web.vertx;

import com.gcplot.accounts.AccountRepository;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.commons.serialization.JsonSerializer;
import com.gcplot.web.Dispatcher;
import com.gcplot.web.HttpMethod;
import com.gcplot.web.RequestContext;
import com.google.common.base.Strings;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class VertxDispatcher implements Dispatcher<String> {

    public void init() {
        LOG.info("Starting Vert.x Dispatcher at [{}:{}]", host, port);
        httpServer = vertx.createHttpServer();
        router = Router.router(vertx);
        httpServer.requestHandler(router::accept).listen(port, host);
        router.route().handler(bodyHandler.setBodyLimit(maxUploadSize));
    }

    @Override
    public void close() {
        LOG.info("Shutting down Vert.x Dispatcher.");
        httpServer.close();
    }

    @Override
    public Dispatcher<String> preHandle(Consumer<RequestContext> handler) {
        this.preHandler = handler;
        return this;
    }

    @Override
    public Dispatcher<String> postHandle(Consumer<RequestContext> handler) {
        this.postHandler = handler;
        return this;
    }

    @Override
    public Dispatcher<String> get(String s, Consumer<RequestContext> handler) {
        return handler(s, HttpMethod.GET, (rc, c) -> handler.accept(c));
    }

    @Override
    public Dispatcher<String> head(String s, Consumer<RequestContext> handler) {
        return handler(s, HttpMethod.HEAD, (rc, c) -> handler.accept(c));
    }

    @Override
    public Dispatcher<String> delete(String s, Consumer<RequestContext> handler) {
        return handler(s, HttpMethod.DELETE, (rc, c) -> handler.accept(c));
    }

    @Override
    public <Payload> Dispatcher<String> post(String s, Class<? extends Payload> payloadType, BiConsumer<Payload, RequestContext> handler) {
        return handler(s, HttpMethod.POST, (rc, c) -> {
            Payload payload = JsonSerializer.deserialize(rc.getBodyAsString(), payloadType);
            handler.accept(payload, c);
        });
    }

    @Override
    public Dispatcher<String> postJson(String s, BiConsumer<JsonObject, RequestContext> handler) {
        return handler(s, HttpMethod.POST, (rc, c) -> handler.accept(rc.getBodyAsJson(), c));
    }

    @Override
    public Dispatcher<String> post(String s, Consumer<RequestContext> handler) {
        return handler(s, HttpMethod.POST, (rc, c) -> handler.accept(c));
    }

    @Override
    public Dispatcher<String> post(String s, BiConsumer<byte[], RequestContext> handler) {
        return handler(s, HttpMethod.POST, (rc, c) -> handler.accept(rc.getBody().getBytes(), c));
    }

    @Override
    public Dispatcher<String> put(String s, Consumer<RequestContext> handler) {
        return handler(s, HttpMethod.PUT, (rc, c) -> handler.accept(c));
    }

    @Override
    public <Payload> Dispatcher<String> put(String s, Class<? extends Payload> payloadType, BiConsumer<Payload, RequestContext> handler) {
        return handler(s, HttpMethod.PUT, (rc, c) -> {
            Payload payload = JsonSerializer.deserialize(rc.getBodyAsString(), payloadType);
            handler.accept(payload, c);
        });
    }

    @Override
    public Dispatcher<String> putJson(String s, BiConsumer<JsonObject, RequestContext> handler) {
        return handler(s, HttpMethod.PUT, (rc, c) -> handler.accept(rc.getBodyAsJson(), c));
    }

    @Override
    public Dispatcher<String> put(String s, BiConsumer<byte[], RequestContext> handler) {
        return handler(s, HttpMethod.PUT, (rc, c) -> handler.accept(rc.getBody().getBytes(), c));
    }

    @Override
    public Dispatcher<String> blocking() {
        this.blocking = true;
        return this;
    }

    @Override
    public Dispatcher<String> noAuth() {
        this.requireAuth = false;
        return this;
    }

    @Override
    public Dispatcher<String> requireAuth() {
        this.requireAuth = true;
        return this;
    }

    @Override
    public Dispatcher<String> requireConfirmed() {
        this.requireConfirmed = true;
        this.requireAuth = true;
        return this;
    }

    @Override
    public Dispatcher<String> filter(Predicate<RequestContext> filter, String message) {
        this.filter = filter;
        this.filterMessage = message;
        return this;
    }

    @Override
    public Dispatcher<String> mimeTypes(String... mimeTypes) {
        this.mimeTypes = mimeTypes;
        return this;
    }

    @Override
    public Dispatcher<String> exceptionHandler(BiConsumer<Throwable, RequestContext> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    protected Dispatcher<String> handler(String s, HttpMethod method,
                                         BiConsumer<RoutingContext, RequestContext> handler) {
        try {
            LOG.info("Mapping [{}] on {}. Require auth: {}.", s, method, requireAuth);
            Route route = null;
            switch (method) {
                case GET:
                    route = router.get(s);
                    break;
                case PUT:
                    route = router.put(s);
                    break;
                case POST:
                    route = router.post(s);
                    break;
                case DELETE:
                    route = router.delete(s);
                    break;
                case HEAD:
                    route = router.head(s);
                    break;
                case PATCH:
                    route = router.patch(s);
                    break;
            }
            boolean bodyAllowed = method == HttpMethod.PUT || method == HttpMethod.POST;
            if (bodyAllowed && mimeTypes != null) {
                for (String mimeType : mimeTypes) {
                    route = route.consumes(mimeType);
                }
            }
            final boolean auth = requireAuth;
            final boolean confirmation = requireConfirmed;
            final Predicate<RequestContext> filter = this.filter;
            final Handler<RoutingContext> r = rc -> {
                VertxRequestContext context = contexts.get().reset(rc);
                try {
                    if (preHandler != null) {
                        preHandler.accept(context);
                    }
                    if (context.loginInfo().isPresent() && context.loginInfo().get().getAccount().isBlocked()) {
                        rc.response().end(ErrorMessages.buildJson(ErrorMessages.USER_IS_BLOCKED));
                    } else {
                        if (auth && !context.loginInfo().isPresent()) {
                            rc.response().end(ErrorMessages.buildJson(ErrorMessages.NOT_AUTHORISED));
                        } else {
                            if (confirmation && !context.loginInfo().get().getAccount().isConfirmed()) {
                                rc.response().end(ErrorMessages.buildJson(ErrorMessages.ACCOUNT_NOT_CONFIRMED));
                            }
                            if (filter == null || filter.test(context)) {
                                handler.accept(rc, context);
                            } else if (!rc.response().ended()) {
                                rc.response().end(ErrorMessages.buildJson(ErrorMessages.REQUEST_FILTERED,
                                        Strings.nullToEmpty(filterMessage)));
                            }
                        }
                    }
                } catch (Throwable t) {
                    if (exceptionHandler != null) {
                        exceptionHandler.accept(t, context);
                    } else {
                        LOG.error("DISPATCH: ", t);
                    }
                } finally {
                    if (postHandler != null) {
                        try {
                            postHandler.accept(context);
                        } catch (Throwable t) {
                            LOG.error("DISPATCH POST HANDLE: ", t);
                        }
                    }
                    if (!rc.response().ended()) {
                        rc.response().end();
                    }
                }
            };
            if (blocking) {
                route.blockingHandler(r, false);
            } else {
                route.handler(r);
            }
            return this;
        } finally {
            reset();
        }
    }

    protected void reset() {
        blocking = false;
        requireAuth = true;
        requireConfirmed = false;
        filter = null;
        filterMessage = null;
        mimeTypes = null;
    }

    protected final ThreadLocal<VertxRequestContext> contexts = new ThreadLocal<VertxRequestContext>() {
        @Override
        protected VertxRequestContext initialValue() {
            return new VertxRequestContext().setAccountRepository(getAccountRepository());
        }
    };

    protected HttpServer httpServer;
    protected Router router;
    protected BodyHandler bodyHandler = BodyHandler.create();

    protected boolean blocking = false;
    protected boolean requireAuth = true;
    protected boolean requireConfirmed = false;
    protected String[] mimeTypes;
    protected BiConsumer<Throwable, RequestContext> exceptionHandler;
    protected Consumer<RequestContext> preHandler;
    protected Consumer<RequestContext> postHandler;
    protected Predicate<RequestContext> filter;
    protected String filterMessage;

    protected static final Logger LOG = LoggerFactory.getLogger(VertxDispatcher.class);

    private Vertx vertx;
    public Vertx getVertx() {
        return vertx;
    }
    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    private String host;
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }

    private int port;
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    private AccountRepository accountRepository;
    public AccountRepository getAccountRepository() {
        return accountRepository;
    }
    public void setAccountRepository(AccountRepository userService) {
        this.accountRepository = userService;
    }

    private int maxUploadSize;
    public int getMaxUploadSize() {
        return maxUploadSize;
    }
    public void setMaxUploadSize(int maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }
}