"""The single definition of what each role may do.

Routers check permissions, never roles. When the role model shifts again, this table changes and the
call sites don't — and there is one place to read to answer "who can do X?".
"""

from enum import Enum

from app.schemas import UserRole


class Permission(str, Enum):
    MANAGE_USERS = "manage_users"
    MANAGE_TEAMS = "manage_teams"
    MANAGE_HALLS = "manage_halls"
    MANAGE_SETTINGS = "manage_settings"

    # The floor operations an organizer exists to perform.
    SCAN_ACCESS = "scan_access"
    CHARGE_TOKENS = "charge_tokens"  # take payment from a wallet (deduction)
    CHARGE_MEALS = "charge_meals"

    # Adding funds is separated from spending them: an organizer at the till takes payment but must
    # not be able to credit a wallet. Topping up is an admin-desk action.
    TOP_UP_WALLET = "top_up_wallet"  # add funds to a wallet (recharge)

    # Reaching the whole event or an arbitrary team, versus only the team you lead.
    BROADCAST_WIDE = "broadcast_wide"
    BROADCAST_OWN_TEAM = "broadcast_own_team"

    VIEW_HALL_AVAILABILITY = "view_hall_availability"
    VIEW_REPORTS = "view_reports"
    VIEW_INBOX = "view_inbox"


_ALL = set(Permission)

# Everyone signed in can read their own messages and see where there is room to work.
_BASELINE = {Permission.VIEW_INBOX, Permission.VIEW_HALL_AVAILABILITY}

ROLE_PERMISSIONS: dict[UserRole, set[Permission]] = {
    UserRole.ADMIN: _ALL,
    # Runs the organizer corps: same floor operations, plus the coordination powers an organizer
    # lacks. Kept out of MANAGE_SETTINGS so event-wide configuration stays with admin.
    UserRole.CHIEF_ORGANIZER: _ALL - {Permission.MANAGE_SETTINGS},
    # Dedicated scanning operations only: meal scanning and hall door scanning.
    # No venue location oversight or hall availability access.
    UserRole.ORGANIZER: {
        Permission.VIEW_INBOX,
        Permission.SCAN_ACCESS,
        Permission.CHARGE_TOKENS,
        Permission.CHARGE_MEALS,
    },
    UserRole.TEAM_LEADER: _BASELINE | {Permission.BROADCAST_OWN_TEAM},
    # Press and similar: they need to know where there is space, and to hear from their leader.
    UserRole.TEAM_MEMBER: _BASELINE,
    UserRole.USER: {Permission.VIEW_INBOX},
}


def permissions_for(role: str | UserRole) -> set[Permission]:
    try:
        return ROLE_PERMISSIONS.get(UserRole(role), set())
    except ValueError:
        # An unrecognised role string (an old row, a hand-edited database) gets nothing rather than
        # falling back to something permissive.
        return set()


def has_permission(role: str | UserRole, permission: Permission) -> bool:
    return permission in permissions_for(role)
