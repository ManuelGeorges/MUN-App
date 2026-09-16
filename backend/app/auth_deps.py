"""Request-time identity and permission guards.

The permission table in `app.permissions` decides *what* each role may do; these dependencies decide
*who is asking* and turn a lack of permission into a 403. Management routers depend on `require(...)`
so the check happens on the server, not just in a client's navigation.

Prototype auth: the bearer token is the opaque string `access-{name}` minted at login. There is no
signature and no expiry — this resolves identity, it does not prove it. Real auth would verify a
signed token here and nothing else in the app would have to change.
"""

from fastapi import Depends, Header, HTTPException, status
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.models.models import User
from app.permissions import Permission, has_permission

_TOKEN_PREFIX = "access-"


def current_user(
    authorization: str | None = Header(default=None),
    db: Session = Depends(get_db),
) -> User:
    if not authorization or not authorization.lower().startswith("bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Sign in to continue.",
        )

    token = authorization[len("bearer "):].strip()
    if not token.startswith(_TOKEN_PREFIX):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid session token.",
        )

    # Only the first prefix is stripped, so names that themselves contain a hyphen
    # (e.g. "hospitality-lead") resolve intact.
    name = token[len(_TOKEN_PREFIX):]
    user = db.query(User).filter(User.name == name).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Your session is no longer valid. Sign in again.",
        )
    return user


def require(permission: Permission):
    """Dependency factory: allows the request only if the caller holds `permission`."""

    def guard(user: User = Depends(current_user)) -> User:
        if not has_permission(user.role, permission):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"Your role can't {permission.value.replace('_', ' ')}.",
            )
        return user

    return guard
