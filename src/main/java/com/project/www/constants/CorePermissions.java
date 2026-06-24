package com.project.www.constants;

/**
 * Core system-level permission keys — single source of truth for @PreAuthorize.
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * RULES:
 * 1. Only backend-owned, infrastructure-level permissions belong here.
 * 2. These keys MUST exactly match the permissionKey values seeded by:
 * - DatabaseSeeder (System/SUPER_ADMIN tenant at startup)
 * - TenantServiceImpl (new tenant via SUPER_ADMIN API)
 * - AuthServiceImpl (new tenant via self-registration)
 * 3. NEVER add dynamic business permissions here.
 * Business module permissions (LEAVE_APPROVE, PAYROLL_VERIFY, etc.)
 * live ONLY in the DB and are checked via PermissionEvaluatorService.
 * 4. Golden rule: add a constant here → update all 3 seeders in same commit.
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * FUTURE MODULE PERMISSIONS (examples — move to DB when modules are built):
 * ─────────────────────────────────────────────────────────────────────────
 * HRMS : HRMS_VIEW_PAYSLIP, HRMS_APPLY_LEAVE, HRMS_APPROVE_LEAVE
 * LMS : LMS_CREATE_COURSE, LMS_VIEW_COURSE, LMS_GRADE_QUIZ
 * CRM : CRM_VIEW_LEADS, CRM_CREATE_LEAD, CRM_CONVERT_LEAD
 * Payroll : PAYROLL_PROCESS, PAYROLL_VIEW
 * These are tenant-defined and checked via:
 * @PreAuthorize("@permissionEvaluator.hasPermission('HRMS_APPLY_LEAVE')")
 */
public final class CorePermissions {

    private CorePermissions() {
    }

    // ── Tenant management ────────────────────────────────────────────────────
    public static final String TENANT_CREATE = "TENANT_CREATE";
    public static final String TENANT_VIEW = "TENANT_VIEW";
    public static final String TENANT_UPDATE = "TENANT_UPDATE";
    public static final String TENANT_ENABLE = "TENANT_ENABLE";
    public static final String TENANT_DISABLE = "TENANT_DISABLE";

    // ── User management ──────────────────────────────────────────────────────
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_VIEW = "USER_VIEW";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";

    // ── Role management ──────────────────────────────────────────────────────
    public static final String ROLE_CREATE = "ROLE_CREATE";
    public static final String ROLE_UPDATE = "ROLE_UPDATE";
    public static final String ROLE_ENABLE = "ROLE_ENABLE";
    public static final String ROLE_DISABLE = "ROLE_DISABLE";

    // ── Permission management ─────────────────────────────────────────────────
    public static final String PERMISSION_CREATE = "PERMISSION_CREATE";
    public static final String PERMISSION_ENABLE = "PERMISSION_ENABLE";
    public static final String PERMISSION_DISABLE = "PERMISSION_DISABLE";

    // ── Settings management ───────────────────────────────────────────────────
    public static final String COMPANY_PROFILE_VIEW = "COMPANY_PROFILE_VIEW";
    public static final String COMPANY_PROFILE_UPDATE = "COMPANY_PROFILE_UPDATE";
    public static final String SETTINGS_MANAGE_TEMPLATES = "SETTINGS_MANAGE_TEMPLATES";
    public static final String SETTINGS_MANAGE_ONBOARDING = "SETTINGS_MANAGE_ONBOARDING";
    public static final String SUBSCRIPTION_MANAGE = "SUBSCRIPTION_MANAGE";

    // ── Marketing management ──────────────────────────────────────────────────
    public static final String MARKETING_AJAY_SUMMARY = "MARKETING_AJAY_SUMMARY";
}
