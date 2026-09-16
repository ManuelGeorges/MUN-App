from datetime import datetime
from enum import Enum
from typing import Optional, List

from pydantic import BaseModel, Field, model_validator


class MealType(str, Enum):
    breakfast = "breakfast"
    lunch = "lunch"


class TransactionType(str, Enum):
    deduction = "deduction"
    recharge = "recharge"


class UserRole(str, Enum):
    """Who someone is in the event.

    Two ladders, not one. Staff run the event (admin → chief organizer → organizer); teams are the
    delegations working inside it (team leader → team member). A team leader is not a junior
    organizer and an organizer is not a senior team member — the two ladders never meet, which is
    why permissions come from `app.permissions` rather than from any ordering of this enum.
    """

    ADMIN = "admin"
    CHIEF_ORGANIZER = "chief_organizer"
    ORGANIZER = "organizer"
    TEAM_LEADER = "team_leader"
    TEAM_MEMBER = "team_member"
    USER = "user"


class TeamRole(str, Enum):
    """Who leads which team. Orthogonal to UserRole: TEAM_LEADER is what someone *is*, LEADER is
    which team they run. A user must be both to broadcast."""

    LEADER = "leader"
    MEMBER = "member"


class BroadcastAudience(str, Enum):
    ALL = "all"
    TEAM = "team"
    PARTICIPANT = "participant"


class TokenPair(BaseModel):
    access_token: str
    refresh_token: str
    expires_in: int = Field(default=3600, ge=1)
    role: UserRole
    # Identity travels with the token. Without it a client has to re-fetch the user list and match
    # on username just to learn its own id, which is what the Android SessionManager was doing.
    user_id: Optional[str] = None
    name: Optional[str] = None
    team_id: Optional[str] = None
    team_role: Optional[TeamRole] = None
    # What this account may do, resolved from the role table. Clients gate their UI on these rather
    # than re-deriving a role→feature map that would drift from the server's.
    permissions: List[str] = Field(default_factory=list)


class LoginRequest(BaseModel):
    username: str
    password: str


class RegisterRequest(BaseModel):
    username: str
    email: str
    password: str
    role: UserRole = UserRole.USER


class UserCreate(BaseModel):
    name: str
    email: Optional[str] = None
    phone: Optional[str] = None
    token_balance: float = Field(default=0, ge=0)
    role: UserRole = UserRole.USER
    team_id: Optional[str] = None
    team_role: Optional[TeamRole] = None


class UserUpdate(BaseModel):
    name: Optional[str] = None
    email: Optional[str] = None
    phone: Optional[str] = None
    is_active: Optional[bool] = None
    team_id: Optional[str] = None
    team_role: Optional[TeamRole] = None


class UserResponse(BaseModel):
    id: str
    name: str
    email: Optional[str] = None
    phone: Optional[str] = None
    token_balance: float
    is_active: bool
    role: str
    team_id: Optional[str] = None
    team_role: Optional[TeamRole] = None

    class Config:
        from_attributes = True


class CardLinkRequest(BaseModel):
    user_id: str
    card_uid: str


class CardResponse(BaseModel):
    card_uid: str
    user: UserResponse

    class Config:
        from_attributes = True


class MealSwipeRequest(BaseModel):
    card_uid: str
    meal_type: MealType
    timestamp: Optional[datetime] = None


class MealSwipeResponse(BaseModel):
    id: str
    user_id: str
    meal_type: MealType
    swipe_timestamp: datetime

    class Config:
        from_attributes = True


class TransactionRequest(BaseModel):
    card_uid: str
    amount: float = Field(gt=0)
    description: Optional[str] = None
    transaction_type: TransactionType


class TransactionResponse(BaseModel):
    id: str
    user_id: str
    amount: float
    transaction_type: TransactionType
    description: Optional[str] = None
    created_at: datetime

    class Config:
        from_attributes = True


class AnalyticsSummary(BaseModel):
    metric: str
    total: int
    generated_at: datetime


class DashboardOverview(BaseModel):
    total_meals_today: int
    total_transactions_today: int
    active_users: int
    total_token_balance: float


class MealWindowRequest(BaseModel):
    breakfast_start: str
    breakfast_end: str
    lunch_start: str
    lunch_end: str


class ReportSummary(BaseModel):
    total_records: int
    generated_at: datetime


class AccessAction(str, Enum):
    entry = "entry"
    exit = "exit"


class HallCreate(BaseModel):
    name: str
    capacity_threshold: int = Field(gt=0)
    allowed_roles: List[UserRole] = Field(default_factory=lambda: [UserRole.USER, UserRole.ORGANIZER, UserRole.ADMIN])


class HallUpdate(BaseModel):
    name: Optional[str] = None
    capacity_threshold: Optional[int] = Field(None, gt=0)
    allowed_roles: Optional[List[UserRole]] = None


class HallResponse(BaseModel):
    id: str
    name: str
    capacity_threshold: int
    allowed_roles: List[UserRole]
    current_occupancy: int

    class Config:
        from_attributes = True


class HallState(str, Enum):
    EMPTY = "empty"
    AVAILABLE = "available"
    FILLING = "filling"
    FULL = "full"


class HallAvailability(BaseModel):
    """Room-finding view of a hall. Carries no occupant identities."""

    id: str
    name: str
    capacity: int
    occupancy: int
    seats_free: int
    state: HallState


class AccessScanRequest(BaseModel):
    card_uid: str
    hall_id: str
    action: AccessAction


class AccessLogResponse(BaseModel):
    id: str
    user_id: str
    hall_id: str
    action: AccessAction
    timestamp: datetime
    allowed: bool
    reason: Optional[str] = None

    class Config:
        from_attributes = True


class TeamCreate(BaseModel):
    name: str
    capacity: int = Field(gt=0)


class TeamUpdate(BaseModel):
    name: Optional[str] = None
    capacity: Optional[int] = Field(None, gt=0)


class TeamResponse(BaseModel):
    id: str
    name: str
    capacity: int
    current_size: int

    class Config:
        from_attributes = True


class NotificationCreate(BaseModel):
    """Legacy team-only broadcast body, kept so the existing
    `POST /teams/{id}/notifications` route kept working while clients migrate."""

    message: str = Field(min_length=1, max_length=1000)


class BroadcastCreate(BaseModel):
    message: str = Field(min_length=1, max_length=1000)
    audience: BroadcastAudience
    team_id: Optional[str] = None
    recipient_id: Optional[str] = None

    @model_validator(mode="after")
    def check_target(self) -> "BroadcastCreate":
        # The target id is validated here rather than in the router so a malformed body is a 422
        # with a field name on it, not a 400 with prose.
        if self.audience is BroadcastAudience.TEAM and not self.team_id:
            raise ValueError("team_id is required when audience is 'team'")
        if self.audience is BroadcastAudience.PARTICIPANT and not self.recipient_id:
            raise ValueError("recipient_id is required when audience is 'participant'")
        return self


class NotificationResponse(BaseModel):
    id: str
    sender_id: str
    sender_name: Optional[str] = None
    audience: BroadcastAudience = BroadcastAudience.TEAM
    team_id: Optional[str] = None
    team_name: Optional[str] = None
    recipient_id: Optional[str] = None
    recipient_name: Optional[str] = None
    message: str
    timestamp: datetime
    # Resolved at send time. Kept on the record because team membership drifts, and a sender needs
    # to know how many people a message actually reached, not how many it would reach today.
    recipient_count: int = 0

    class Config:
        from_attributes = True
