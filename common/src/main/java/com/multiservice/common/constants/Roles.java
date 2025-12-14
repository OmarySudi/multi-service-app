package com.multiservice.common.constants;

import java.util.*;

/**
 * Role definitions where each role is a group of permissions.
 * Roles represent user types, and permissions define what they can do.
 */
public final class Roles {

    private Roles() {
        // Prevent instantiation
    }

    // Role Names
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final String MANAGER = "MANAGER";
    public static final String OPERATOR = "OPERATOR";
    public static final String VIEWER = "VIEWER";

    // Role to Permissions Mapping
    private static final Map<String, List<String>> ROLE_PERMISSIONS = new HashMap<>();

    static {
        // ADMIN has all permissions
        ROLE_PERMISSIONS.put(ADMIN, Arrays.asList(
            Permissions.ADMIN_ALL,
            Permissions.ADMIN_SYSTEM_CONFIG,
            Permissions.ADMIN_VIEW_LOGS,
            Permissions.DASHBOARD_VIEW,
            Permissions.DASHBOARD_STATS,
            Permissions.DASHBOARD_PROFILE,
            Permissions.DASHBOARD_ADMIN,
            Permissions.USER_CREATE,
            Permissions.USER_READ,
            Permissions.USER_UPDATE,
            Permissions.USER_DELETE,
            Permissions.USER_LIST,
            Permissions.TIPS_CREATE,
            Permissions.TIPS_READ,
            Permissions.TIPS_UPDATE,
            Permissions.TIPS_DELETE,
            Permissions.TIPS_APPROVE,
            Permissions.TIPS_REPORT,
            Permissions.USSD_SEND,
            Permissions.USSD_VIEW,
            Permissions.USSD_HISTORY,
            Permissions.USSD_CONFIG,
            Permissions.PAYMENT_INITIATE,
            Permissions.PAYMENT_VIEW,
            Permissions.PAYMENT_REFUND,
            Permissions.PAYMENT_REPORT,
            Permissions.PAYMENT_RECONCILE
        ));

        // MANAGER can manage users, tips, and view reports
        ROLE_PERMISSIONS.put(MANAGER, Arrays.asList(
            Permissions.DASHBOARD_VIEW,
            Permissions.DASHBOARD_STATS,
            Permissions.DASHBOARD_PROFILE,
            Permissions.USER_READ,
            Permissions.USER_UPDATE,
            Permissions.USER_LIST,
            Permissions.TIPS_CREATE,
            Permissions.TIPS_READ,
            Permissions.TIPS_UPDATE,
            Permissions.TIPS_APPROVE,
            Permissions.TIPS_REPORT,
            Permissions.USSD_VIEW,
            Permissions.USSD_HISTORY,
            Permissions.PAYMENT_VIEW,
            Permissions.PAYMENT_REPORT
        ));

        // OPERATOR can perform operations but limited admin access
        ROLE_PERMISSIONS.put(OPERATOR, Arrays.asList(
            Permissions.DASHBOARD_VIEW,
            Permissions.DASHBOARD_STATS,
            Permissions.TIPS_CREATE,
            Permissions.TIPS_READ,
            Permissions.TIPS_UPDATE,
            Permissions.USSD_SEND,
            Permissions.USSD_VIEW,
            Permissions.PAYMENT_INITIATE,
            Permissions.PAYMENT_VIEW
        ));

        // USER has basic access
        ROLE_PERMISSIONS.put(USER, Arrays.asList(
            Permissions.DASHBOARD_VIEW,
            Permissions.DASHBOARD_PROFILE,
            Permissions.TIPS_READ,
            Permissions.PAYMENT_VIEW
        ));

        // VIEWER can only view
        ROLE_PERMISSIONS.put(VIEWER, Arrays.asList(
            Permissions.DASHBOARD_VIEW,
            Permissions.TIPS_READ,
            Permissions.USSD_VIEW,
            Permissions.PAYMENT_VIEW
        ));
    }

    /**
     * Get all permissions for a given role.
     *
     * @param role the role name
     * @return list of permissions for the role
     */
    public static List<String> getPermissions(String role) {
        return ROLE_PERMISSIONS.getOrDefault(role, Collections.emptyList());
    }

    /**
     * Get permissions for multiple roles.
     *
     * @param roles list of role names
     * @return combined list of unique permissions
     */
    public static List<String> getPermissions(List<String> roles) {
        Set<String> permissions = new HashSet<>();
        for (String role : roles) {
            permissions.addAll(getPermissions(role));
        }
        return new ArrayList<>(permissions);
    }

    /**
     * Check if a role has a specific permission.
     *
     * @param role the role name
     * @param permission the permission to check
     * @return true if role has the permission
     */
    public static boolean hasPermission(String role, String permission) {
        return getPermissions(role).contains(permission);
    }
}
