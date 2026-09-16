package com.mianu.paymentapp.ui

/**
 * Navigation-facing role. Adds NONE for the signed-out state to the API's roles.
 *
 * The app maps the API's six roles onto these: the two organizer tiers share the operations
 * surface, and the two team tiers (leader, member) each get their own. See `ApiUserRole.toUiRole`.
 */
enum class UserRole {
    NONE,
    ORGANIZER,
    CHIEF_ORGANIZER,
    TEAM_LEADER,
    TEAM_MEMBER,
    ADMIN
}
