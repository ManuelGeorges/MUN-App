from typing import List

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from app.auth_deps import require
from app.db.database import get_db
from app.models.models import Team, User
from app.permissions import Permission
from app.routers.notifications import create_broadcast, query_notifications
from app.schemas import (
    BroadcastAudience,
    BroadcastCreate,
    NotificationCreate,
    NotificationResponse,
    TeamCreate,
    TeamResponse,
    TeamRole,
    TeamUpdate,
    UserResponse,
)

router = APIRouter(prefix="/teams", tags=["teams"])


def _to_response(team: Team) -> TeamResponse:
    return TeamResponse(
        id=team.id,
        name=team.name,
        capacity=team.capacity,
        current_size=team.current_size,
    )


@router.post("", response_model=TeamResponse, status_code=status.HTTP_201_CREATED)
def create_team(
    payload: TeamCreate,
    _: User = Depends(require(Permission.MANAGE_TEAMS)),
    db: Session = Depends(get_db),
) -> TeamResponse:
    team = Team(name=payload.name, capacity=payload.capacity)
    db.add(team)
    db.commit()
    db.refresh(team)
    return _to_response(team)


@router.get("", response_model=List[TeamResponse])
def list_teams(db: Session = Depends(get_db)) -> List[TeamResponse]:
    return [_to_response(team) for team in db.query(Team).all()]


@router.get("/{team_id}", response_model=TeamResponse)
def get_team(team_id: str, db: Session = Depends(get_db)) -> TeamResponse:
    team = db.query(Team).filter(Team.id == team_id).first()
    if not team:
        raise HTTPException(status_code=404, detail="Team not found")
    return _to_response(team)


@router.put("/{team_id}", response_model=TeamResponse)
def update_team(
    team_id: str,
    payload: TeamUpdate,
    _: User = Depends(require(Permission.MANAGE_TEAMS)),
    db: Session = Depends(get_db),
) -> TeamResponse:
    team = db.query(Team).filter(Team.id == team_id).first()
    if not team:
        raise HTTPException(status_code=404, detail="Team not found")

    if payload.name is not None:
        team.name = payload.name
    if payload.capacity is not None:
        if payload.capacity < team.current_size:
            raise HTTPException(
                status_code=400,
                detail=f"Team already has {team.current_size} members.",
            )
        team.capacity = payload.capacity

    db.commit()
    db.refresh(team)
    return _to_response(team)


@router.delete("/{team_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_team(
    team_id: str,
    _: User = Depends(require(Permission.MANAGE_TEAMS)),
    db: Session = Depends(get_db),
) -> None:
    team = db.query(Team).filter(Team.id == team_id).first()
    if not team:
        raise HTTPException(status_code=404, detail="Team not found")
    # Release members rather than cascading the delete onto them.
    for member in list(team.members):
        member.team_id = None
        member.team_role = None
    db.delete(team)
    db.commit()


@router.get("/{team_id}/members", response_model=List[UserResponse])
def list_members(team_id: str, db: Session = Depends(get_db)) -> List[UserResponse]:
    if not db.query(Team).filter(Team.id == team_id).first():
        raise HTTPException(status_code=404, detail="Team not found")
    return db.query(User).filter(User.team_id == team_id).all()


@router.post("/{team_id}/members/{user_id}", response_model=UserResponse)
def assign_member(
    team_id: str,
    user_id: str,
    role: TeamRole = Query(TeamRole.MEMBER),
    _: User = Depends(require(Permission.MANAGE_TEAMS)),
    db: Session = Depends(get_db),
) -> UserResponse:
    team = db.query(Team).filter(Team.id == team_id).first()
    if not team:
        raise HTTPException(status_code=404, detail="Team not found")

    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # Capacity only binds on an actual move; re-roling someone already on the team is always fine,
    # including when the team is full.
    if user.team_id != team_id and team.current_size >= team.capacity:
        raise HTTPException(status_code=400, detail="Team is at full capacity")

    user.team_id = team_id
    user.team_role = role.value
    db.commit()
    db.refresh(user)
    return user


@router.delete("/{team_id}/members/{user_id}", response_model=UserResponse)
def remove_member(
    team_id: str,
    user_id: str,
    _: User = Depends(require(Permission.MANAGE_TEAMS)),
    db: Session = Depends(get_db),
) -> UserResponse:
    user = db.query(User).filter(User.id == user_id, User.team_id == team_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User is not on this team")
    user.team_id = None
    user.team_role = None
    db.commit()
    db.refresh(user)
    return user


@router.post("/{team_id}/notifications", response_model=NotificationResponse)
def broadcast_to_team(
    team_id: str,
    notification: NotificationCreate,
    sender_id: str,
    db: Session = Depends(get_db),
) -> NotificationResponse:
    """Team-scoped broadcast, kept for the Android client shipped before targeting existed.

    Delegates to the general endpoint so there is exactly one implementation of the permission
    rules — new audiences should go to `POST /notifications` instead.
    """
    return create_broadcast(
        db,
        sender_id,
        BroadcastCreate(
            message=notification.message,
            audience=BroadcastAudience.TEAM,
            team_id=team_id,
        ),
    )


@router.get("/{team_id}/notifications", response_model=List[NotificationResponse])
def team_notifications(team_id: str, db: Session = Depends(get_db)) -> List[NotificationResponse]:
    return query_notifications(db, team_id=team_id)
