from fastapi import APIRouter, status, Depends, HTTPException
from sqlalchemy.orm import Session
from app.schemas import LoginRequest, RegisterRequest, TeamRole, TokenPair, UserRole
from app.db.database import get_db
from app.models.models import User
from app.permissions import permissions_for

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/login", response_model=TokenPair, status_code=status.HTTP_200_OK)
def login(payload: LoginRequest, db: Session = Depends(get_db)) -> TokenPair:
    # In a real app, verify password hash
    user = db.query(User).filter(User.name == payload.username).first()
    if not user:
        # Auto-register for demo purposes if user doesn't exist
        # This is strictly for the prototype phase
        # Prototype convenience: infer a role from the name so demo accounts can be conjured by
        # signing in. Order matters — "chief" is checked before "organizer" since it contains it.
        name = payload.username.lower()
        if name == "admin":
            role = UserRole.ADMIN
        elif "chief" in name:
            role = UserRole.CHIEF_ORGANIZER
        elif "lead" in name:
            role = UserRole.TEAM_LEADER
        elif "organizer" in name:
            role = UserRole.ORGANIZER
        elif "press" in name or "member" in name:
            role = UserRole.TEAM_MEMBER
        else:
            role = UserRole.USER

        user = User(name=payload.username, role=role.value)
        db.add(user)
        db.commit()
        db.refresh(user)

    return _token_for(user)


@router.post("/register", response_model=TokenPair, status_code=status.HTTP_201_CREATED)
def register(payload: RegisterRequest, db: Session = Depends(get_db)) -> TokenPair:
    existing = db.query(User).filter(User.name == payload.username).first()
    if existing:
        raise HTTPException(status_code=400, detail="User already exists")
        
    user = User(
        name=payload.username,
        email=payload.email,
        role=payload.role.value
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    
    return _token_for(user)


def _token_for(user: User) -> TokenPair:
    """Prototype token: an opaque string, no signature, no expiry enforcement."""
    return TokenPair(
        access_token=f"access-{user.name}",
        refresh_token="refresh",
        expires_in=3600,
        role=UserRole(user.role),
        user_id=user.id,
        name=user.name,
        team_id=user.team_id,
        team_role=TeamRole(user.team_role) if user.team_role else None,
        permissions=sorted(p.value for p in permissions_for(user.role)),
    )
