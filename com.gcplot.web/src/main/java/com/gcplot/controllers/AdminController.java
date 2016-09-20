package com.gcplot.controllers;

import com.gcplot.Identifier;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.messages.AccountResponse;
import com.gcplot.model.account.Account;
import com.gcplot.model.role.Restriction;
import com.gcplot.model.role.RestrictionType;
import com.gcplot.repository.AccountRepository;
import com.gcplot.web.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/21/16
 */
public class AdminController extends Controller {
    public static final String MESSAGE = "You are not allowed to perform such action.";

    @PostConstruct
    public void init() {
        dispatcher.requireAuth()
                .filter(this::hasNonRestrictiveRole, MESSAGE)
                .filter(c -> c.hasParam("role_manager"), "Param 'role_manager' is missing.")
                .filter(c -> c.hasParam("account_id"), "Param 'account_id' is missing.")
                .get("/admin/account/roles_management", this::userRoleManagement);
        dispatcher.requireAuth()
                .filter(this::hasNonRestrictiveRole, MESSAGE)
                .filter(c -> c.hasParam("account_id"), "Param 'account_id' is missing.")
                .get("/admin/account/info", this::getAccountInfo);
        dispatcher.requireAuth()
                .get("/admin/account/id", this::getCurrentAccountId);
    }

    /**
     * GET /admin/account/id
     * Require Auth (token)
     */
    public void getCurrentAccountId(RequestContext ctx) {
        ctx.response(ctx.loginInfo().get().getAccount().id().toString());
    }

    /**
     * GET /admin/account/info
     * Require Auth (token)
     * Require >= 1 non-restrictive roles
     * Params:
     *  - account_id (string)
     * Returns: Account in JSON except pass hash
     */
    public void getAccountInfo(RequestContext ctx) {
        String accountId = ctx.param("account_id");

        Optional<Account> account = accountRepository.account(Identifier.fromStr(accountId));
        if (account.isPresent()) {
            ctx.response(AccountResponse.from(account.get()));
        } else {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                    String.format("Account with [id=%s] wasn't found", accountId)));
        }
    }

    /**
     * GET /admin/account/roles_management
     * Require Auth (token)
     * Require >= 1 non-restrictive roles
     * Params:
     *  - role_manager (boolean)
     *  - account_id (string)
     */
    public void userRoleManagement(RequestContext ctx) {
        boolean isRoleManager = Boolean.parseBoolean(ctx.param("role_manager"));
        String accountId = ctx.param("account_id");

        Optional<Account> account = accountRepository.account(Identifier.fromStr(accountId));
        if (account.isPresent()) {
            accountRepository.roleManagement(account.get().id(), isRoleManager);
            ctx.response(SUCCESS);
        } else {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                    String.format("Account with [id=%s] wasn't found", accountId)));
        }
    }

    private boolean hasNonRestrictiveRole(RequestContext ctx) {
        if (!ctx.loginInfo().isPresent()) {
            return false;
        }
        List<Restriction> rs = ctx.loginInfo().get().getAccount().roles().stream()
                .flatMap(r -> r.restrictions().stream())
                .filter(r -> r.type() == RestrictionType.TOGGLE)
                .filter(r -> r.action().equals(ctx.path())).collect(Collectors.toList());
        return rs.size() > 0 && rs.stream().allMatch(r -> !r.restricted());
    }

    protected AccountRepository accountRepository;
    public AccountRepository getAccountRepository() {
        return accountRepository;
    }
    @Autowired
    public void setAccountRepository(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

}
