import uuid
from typing import List
from datetime import datetime

from fastapi import APIRouter, HTTPException, Depends
from sqlalchemy.orm import Session

from app.auth_deps import require
from app.db.database import get_db
from app.models.models import Card, Hall, AccessLog, User
from app.permissions import Permission
from app.schemas import (
    HallCreate,
    HallUpdate,
    HallResponse,
    HallAvailability,
    HallState,
    AccessScanRequest,
    AccessLogResponse,
    UserRole,
    AccessAction,
)

router = APIRouter(prefix="/access", tags=["access"])


def _parse_roles(role_str: str) -> List[UserRole]:
    if not role_str:
        return [UserRole.USER, UserRole.ORGANIZER, UserRole.ADMIN]
    roles = []
    for r in role_str.split(","):
        r = r.strip()
        try:
            roles.append(UserRole(r))
        except ValueError:
            pass
    return roles or [UserRole.USER, UserRole.ORGANIZER, UserRole.ADMIN]


def _to_hall_response(hall: Hall) -> HallResponse:
    return HallResponse(
        id=hall.id,
        name=hall.name,
        capacity_threshold=hall.capacity_threshold,
        allowed_roles=_parse_roles(hall.allowed_roles),
        current_occupancy=hall.current_occupancy or 0,
    )


@router.post("/halls", response_model=HallResponse)
def create_hall(
    hall_create: HallCreate,
    _: User = Depends(require(Permission.MANAGE_HALLS)),
    db: Session = Depends(get_db),
) -> HallResponse:
    role_str = ",".join(r.value for r in hall_create.allowed_roles)
    hall = Hall(
        name=hall_create.name,
        capacity_threshold=hall_create.capacity_threshold,
        allowed_roles=role_str,
        current_occupancy=0,
    )
    db.add(hall)
    db.commit()
    db.refresh(hall)
    return _to_hall_response(hall)


@router.get("/halls", response_model=List[HallResponse])
def list_halls(db: Session = Depends(get_db)) -> List[HallResponse]:
    halls = db.query(Hall).all()
    return [_to_hall_response(h) for h in halls]


@router.put("/halls/{hall_id}", response_model=HallResponse)
def update_hall(
    hall_id: str,
    hall_update: HallUpdate,
    _: User = Depends(require(Permission.MANAGE_HALLS)),
    db: Session = Depends(get_db),
) -> HallResponse:
    hall = db.query(Hall).filter(Hall.id == hall_id).first()
    if not hall:
        raise HTTPException(status_code=404, detail="Hall not found")

    if hall_update.name is not None:
        hall.name = hall_update.name
    if hall_update.capacity_threshold is not None:
        hall.capacity_threshold = hall_update.capacity_threshold
    if hall_update.allowed_roles is not None:
        hall.allowed_roles = ",".join(r.value for r in hall_update.allowed_roles)

    db.commit()
    db.refresh(hall)
    return _to_hall_response(hall)


@router.get("/halls/{hall_id}/presence", response_model=List[str])
def get_hall_presence(hall_id: str, db: Session = Depends(get_db)) -> List[str]:
    hall = db.query(Hall).filter(Hall.id == hall_id).first()
    if not hall:
        raise HTTPException(status_code=404, detail="Hall not found")
    recent_logs = (
        db.query(AccessLog)
        .filter(AccessLog.hall_id == hall_id, AccessLog.allowed.is_(True))
        .order_by(AccessLog.timestamp.desc())
        .limit(100)
        .all()
    )
    inside = set()
    for log in reversed(recent_logs):
        if log.action == "entry":
            inside.add(log.user_id)
        elif log.action == "exit":
            inside.discard(log.user_id)
    return list(inside)


@router.get("/halls/availability", response_model=List[HallAvailability])
def hall_availability(db: Session = Depends(get_db)) -> List[HallAvailability]:
    """Where there is room right now."""
    halls = db.query(Hall).all()
    result = []
    for hall in halls:
        threshold = hall.capacity_threshold or 0
        occupancy = hall.current_occupancy or 0
        ratio = (occupancy / threshold) if threshold else 0.0

        if occupancy == 0:
            state = HallState.EMPTY
        elif ratio >= 1.0:
            state = HallState.FULL
        elif ratio >= 0.8:
            state = HallState.FILLING
        else:
            state = HallState.AVAILABLE

        result.append(
            HallAvailability(
                id=hall.id,
                name=hall.name,
                capacity=threshold,
                occupancy=occupancy,
                seats_free=max(0, threshold - occupancy),
                state=state,
            )
        )
    return result


@router.post("/scan", response_model=AccessLogResponse)
def scan_access(scan_request: AccessScanRequest, db: Session = Depends(get_db)) -> AccessLogResponse:
    card = db.query(Card).filter(Card.uid == scan_request.card_uid).first()
    if not card or not card.user:
        raise HTTPException(status_code=404, detail="User or Card not found")

    hall = db.query(Hall).filter(Hall.id == scan_request.hall_id).first()
    if not hall:
        raise HTTPException(status_code=404, detail="Hall not found")

    user = card.user
    allowed_roles = _parse_roles(hall.allowed_roles)
    user_role = UserRole(user.role)

    allowed = True
    reason = None

    if user_role not in allowed_roles:
        allowed = False
        reason = f"Role '{user_role.value}' not authorized for {hall.name}"

    if allowed and scan_request.action == AccessAction.entry:
        if (hall.current_occupancy or 0) >= (hall.capacity_threshold or 0):
            allowed = False
            reason = "Hall at full capacity"

    if allowed:
        if scan_request.action == AccessAction.entry:
            hall.current_occupancy = (hall.current_occupancy or 0) + 1
        elif scan_request.action == AccessAction.exit:
            hall.current_occupancy = max(0, (hall.current_occupancy or 0) - 1)

    log = AccessLog(
        id=str(uuid.uuid4()),
        user_id=user.id,
        hall_id=hall.id,
        action=scan_request.action.value,
        timestamp=datetime.utcnow(),
        allowed=allowed,
        reason=reason,
    )
    db.add(log)
    db.commit()

    return AccessLogResponse(
        id=log.id,
        user_id=log.user_id,
        hall_id=log.hall_id,
        action=scan_request.action,
        timestamp=log.timestamp,
        allowed=log.allowed,
        reason=log.reason,
    )
