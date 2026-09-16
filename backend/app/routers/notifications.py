from datetime import datetime
from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import or_
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.models.models import Notification, Team, User
from app.permissions import Permission, has_permission
from app.schemas import (
    BroadcastAudience,
    BroadcastCreate,
    NotificationResponse,
    TeamRole,
    UserRole,
)

router = APIRouter(prefix="/notifications", tags=["notifications"])


# Staff run the event; everyone else attends it. "All participants" means the latter — delegates,
# team leaders and team members alike — so an event-wide notice doesn't also spam the organizers
# who are usually the ones sending it.
_STAFF_ROLES = {
    UserRole.ADMIN.value,
    UserRole.CHIEF_ORGANIZER.value,
    UserRole.ORGANIZER.value,
}


def _resolve_audience(db: Session, payload: BroadcastCreate) -> List[User]:
    """The users a broadcast actually lands on."""
    query = db.query(User).filter(User.is_active.is_(True))

    if payload.audience is BroadcastAudience.ALL:
        return query.filter(User.role.notin_(_STAFF_ROLES)).all()
    if payload.audience is BroadcastAudience.TEAM:
        return query.filter(User.team_id == payload.team_id).all()
    return query.filter(User.id == payload.recipient_id).all()


def _authorize(db: Session, sender: User, payload: BroadcastCreate) -> None:
    """Raises 403 unless `sender` may address this audience.

    Wide reach (everyone, or an arbitrary team) is a BROADCAST_WIDE permission. Everyone else with
    BROADCAST_OWN_TEAM is confined to the team they lead: the team as a whole, or one member of it.
    """
    if has_permission(sender.role, Permission.BROADCAST_WIDE):
        return

    if not has_permission(sender.role, Permission.BROADCAST_OWN_TEAM):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="This role cannot send broadcasts.",
        )

    # Holding the role is not enough — you must actually lead the team you are addressing.
    if sender.team_role != TeamRole.LEADER.value:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only the leader of a team can message it.",
        )

    if not sender.team_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You lead no team, so there is nobody to broadcast to.",
        )

    if payload.audience is BroadcastAudience.ALL:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Team leaders can only message their own team. "
            "Ask a chief organizer to reach everyone.",
        )

    if payload.audience is BroadcastAudience.TEAM and payload.team_id != sender.team_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Team leaders can only message their own team.",
        )

    if payload.audience is BroadcastAudience.PARTICIPANT:
        target = db.query(User).filter(User.id == payload.recipient_id).first()
        if not target:
            raise HTTPException(status_code=404, detail="Recipient not found")
        if target.team_id != sender.team_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="That participant is not on your team.",
            )


def _to_response(db: Session, notif: Notification) -> NotificationResponse:
    sender = db.query(User).filter(User.id == notif.sender_id).first()
    team = db.query(Team).filter(Team.id == notif.team_id).first() if notif.team_id else None
    recipient = (
        db.query(User).filter(User.id == notif.recipient_id).first() if notif.recipient_id else None
    )
    return NotificationResponse(
        id=notif.id,
        sender_id=notif.sender_id,
        sender_name=sender.name if sender else None,
        audience=BroadcastAudience(notif.audience),
        team_id=notif.team_id,
        team_name=team.name if team else None,
        recipient_id=notif.recipient_id,
        recipient_name=recipient.name if recipient else None,
        message=notif.message,
        timestamp=notif.timestamp,
        recipient_count=notif.recipient_count or 0,
    )


def create_broadcast(db: Session, sender_id: str, payload: BroadcastCreate) -> NotificationResponse:
    """Send a broadcast.

    A plain function, not the route handler: other routers delegate here, and calling a FastAPI
    handler directly hands it the `Query(...)` default objects instead of real values.
    """
    sender = db.query(User).filter(User.id == sender_id).first()
    if not sender:
        raise HTTPException(status_code=404, detail="Sender not found")

    if payload.audience is BroadcastAudience.TEAM:
        if not db.query(Team).filter(Team.id == payload.team_id).first():
            raise HTTPException(status_code=404, detail="Team not found")

    _authorize(db, sender, payload)

    recipients = _resolve_audience(db, payload)
    if not recipients:
        # A send that reaches nobody is far more likely a mis-targeted message than an intentional
        # one, so it fails loudly instead of being recorded as a delivered broadcast.
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="That audience has no active members.",
        )

    notif = Notification(
        sender_id=sender_id,
        audience=payload.audience.value,
        team_id=payload.team_id,
        recipient_id=payload.recipient_id,
        message=payload.message,
        recipient_count=len(recipients),
        timestamp=datetime.utcnow(),
    )
    db.add(notif)
    db.commit()
    db.refresh(notif)
    return _to_response(db, notif)


def query_notifications(
    db: Session,
    team_id: Optional[str] = None,
    sender_id: Optional[str] = None,
    limit: int = 50,
) -> List[NotificationResponse]:
    """Sent-message log, newest first. For the sender's own history and the admin overview."""
    query = db.query(Notification)
    if team_id:
        query = query.filter(Notification.team_id == team_id)
    if sender_id:
        query = query.filter(Notification.sender_id == sender_id)
    rows = query.order_by(Notification.timestamp.desc()).limit(limit).all()
    return [_to_response(db, row) for row in rows]


@router.post("", response_model=NotificationResponse, status_code=status.HTTP_201_CREATED)
def broadcast_route(
    payload: BroadcastCreate,
    sender_id: str = Query(..., description="Id of the user sending the broadcast"),
    db: Session = Depends(get_db),
) -> NotificationResponse:
    return create_broadcast(db, sender_id, payload)


@router.get("", response_model=List[NotificationResponse])
def list_notifications_route(
    team_id: Optional[str] = None,
    sender_id: Optional[str] = None,
    limit: int = Query(50, ge=1, le=200),
    db: Session = Depends(get_db),
) -> List[NotificationResponse]:
    return query_notifications(db, team_id=team_id, sender_id=sender_id, limit=limit)


@router.get("/inbox/{user_id}", response_model=List[NotificationResponse])
def inbox(
    user_id: str,
    limit: int = Query(50, ge=1, le=200),
    db: Session = Depends(get_db),
) -> List[NotificationResponse]:
    """What one user should see: event-wide messages, their team's, and anything addressed to them."""
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    clauses = [
        Notification.audience == BroadcastAudience.ALL.value,
        Notification.recipient_id == user_id,
    ]
    if user.team_id:
        clauses.append(
            (Notification.audience == BroadcastAudience.TEAM.value)
            & (Notification.team_id == user.team_id)
        )

    rows = (
        db.query(Notification)
        .filter(or_(*clauses))
        .order_by(Notification.timestamp.desc())
        .limit(limit)
        .all()
    )
    return [_to_response(db, row) for row in rows]
