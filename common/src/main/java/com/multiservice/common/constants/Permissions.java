package com.multiservice.common.constants;

/**
 * Centralized permissions constants for the application.
 * These permissions are checked by @PreAuthorize guards on endpoints.
 */
public final class Permissions {

    private Permissions() {
        // Prevent instantiation
    }

    // Dashboard Permissions
    public static final String DASHBOARD_VIEW = "dashboard:view";
    public static final String DASHBOARD_STATS = "dashboard:stats";
    public static final String DASHBOARD_PROFILE = "dashboard:profile";
    public static final String DASHBOARD_ADMIN = "dashboard:admin";

    // User Management Permissions
    public static final String USER_CREATE = "user:create";
    public static final String USER_READ = "user:read";
    public static final String USER_UPDATE = "user:update";
    public static final String USER_DELETE = "user:delete";
    public static final String USER_LIST = "user:list";

    // Tips Service Permissions
    public static final String TIPS_CREATE = "tips:create";
    public static final String TIPS_READ = "tips:read";
    public static final String TIPS_UPDATE = "tips:update";
    public static final String TIPS_DELETE = "tips:delete";
    public static final String TIPS_APPROVE = "tips:approve";
    public static final String TIPS_REPORT = "tips:report";

    // USSD Push Permissions
    public static final String USSD_SEND = "ussd:send";
    public static final String USSD_VIEW = "ussd:view";
    public static final String USSD_HISTORY = "ussd:history";
    public static final String USSD_CONFIG = "ussd:config";

    // Payment Permissions
    public static final String PAYMENT_INITIATE = "payment:initiate";
    public static final String PAYMENT_VIEW = "payment:view";
    public static final String PAYMENT_REFUND = "payment:refund";
    public static final String PAYMENT_REPORT = "payment:report";
    public static final String PAYMENT_RECONCILE = "payment:reconcile";

    // Admin Permissions
    public static final String ADMIN_ALL = "admin:all";
    public static final String ADMIN_SYSTEM_CONFIG = "admin:system_config";
    public static final String ADMIN_VIEW_LOGS = "admin:view_logs";
}
