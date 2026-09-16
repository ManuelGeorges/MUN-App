import uuid
from dataclasses import dataclass
from datetime import datetime, time
from typing import Optional

from app.schemas import MealType, TransactionType, UserResponse, UserRole, AccessAction, HallResponse, TeamResponse, TeamRole, NotificationResponse


@dataclass
class MealWindow:
    start: time
    end: time


@dataclass
class MealRecord:
    id: str
    user_id: str
    meal_type: MealType
    swipe_timestamp: datetime


@dataclass
class TransactionRecord:
    id: str
    user_id: str
    amount: float
    transaction_type: TransactionType
    balance_before: float
    balance_after: float
    created_at: datetime


@dataclass
class AccessLogRecord:
    id: str
    user_id: str
    hall_id: str
    action: AccessAction
    timestamp: datetime
    allowed: bool
    reason: Optional[str]


class InMemoryStore:
    def __init__(self) -> None:
        self.users: dict[str, UserResponse] = {}
        self.cards: dict[str, str] = {}
        self.meals: list[MealRecord] = []
        self.transactions: list[TransactionRecord] = []
        self.meal_windows = {
            MealType.breakfast: MealWindow(start=time(6, 0), end=time(10, 0)),
            MealType.lunch: MealWindow(start=time(11, 30), end=time(14, 30)),
        }
        self.idempotency_keys: set[str] = set()
        self.halls: dict[str, HallResponse] = {}
        self.access_logs: list[AccessLogRecord] = []
        self.hall_presence: dict[str, set[str]] = {}
        self.teams: dict[str, TeamResponse] = {}
        self.notifications: list[NotificationResponse] = []

    def create_user(self, user: UserResponse) -> UserResponse:
        self.users[user.id] = user
        return user

    def update_user(self, user_id: str, updated: UserResponse) -> UserResponse:
        self.users[user_id] = updated
        return updated

    def delete_user(self, user_id: str) -> None:
        self.users.pop(user_id, None)
        cards_to_remove = [uid for uid, uid_user in self.cards.items() if uid_user == user_id]
        for uid in cards_to_remove:
            self.cards.pop(uid, None)

    def link_card(self, user_id: str, card_uid: str) -> None:
        self.cards[card_uid] = user_id

    def get_user_by_card(self, card_uid: str) -> Optional[UserResponse]:
        user_id = self.cards.get(card_uid)
        if not user_id:
            return None
        return self.users.get(user_id)

    def can_swipe(self, user_id: str, meal_type: MealType, timestamp: datetime) -> bool:
        date_key = timestamp.date()
        return not any(
            meal.user_id == user_id
            and meal.meal_type == meal_type
            and meal.swipe_timestamp.date() == date_key
            for meal in self.meals
        )

    def within_window(self, meal_type: MealType, timestamp: datetime) -> bool:
        window = self.meal_windows[meal_type]
        current_time = timestamp.time()
        return window.start <= current_time <= window.end

    def record_meal(self, user_id: str, meal_type: MealType, timestamp: datetime) -> MealRecord:
        record = MealRecord(
            id=str(uuid.uuid4()),
            user_id=user_id,
            meal_type=meal_type,
            swipe_timestamp=timestamp,
        )
        self.meals.append(record)
        return record

    def record_transaction(
        self,
        user_id: str,
        amount: float,
        transaction_type: TransactionType,
        description: Optional[str],
    ) -> TransactionRecord:
        user = self.users[user_id]
        balance_before = user.token_balance
        balance_after = balance_before + amount if transaction_type == TransactionType.recharge else balance_before - amount
        updated_user = user.model_copy(update={"token_balance": balance_after})
        self.users[user_id] = updated_user
        record = TransactionRecord(
            id=str(uuid.uuid4()),
            user_id=user_id,
            amount=amount,
            transaction_type=transaction_type,
            balance_before=balance_before,
            balance_after=balance_after,
            created_at=datetime.utcnow(),
        )
        self.transactions.append(record)
        return record

    def create_hall(self, hall: HallResponse) -> HallResponse:
        self.halls[hall.id] = hall
        self.hall_presence[hall.id] = set()
        return hall

    def update_hall(self, hall_id: str, updated: HallResponse) -> HallResponse:
        self.halls[hall_id] = updated
        return updated
        
    def get_hall(self, hall_id: str) -> Optional[HallResponse]:
        return self.halls.get(hall_id)
        
    def record_access(self, user_id: str, user_role: UserRole, hall_id: str, action: AccessAction) -> AccessLogRecord:
        hall = self.halls.get(hall_id)
        if not hall:
            return AccessLogRecord(id=str(uuid.uuid4()), user_id=user_id, hall_id=hall_id, action=action, timestamp=datetime.utcnow(), allowed=False, reason="Hall not found")
            
        allowed = True
        reason = None
        
        if user_role not in hall.allowed_roles:
            allowed = False
            reason = "Role not allowed"
            
        if allowed and action == AccessAction.entry:
            presence = self.hall_presence.setdefault(hall_id, set())
            if len(presence) >= hall.capacity_threshold and user_id not in presence:
                allowed = False
                reason = "Hall at full capacity"
                
        if allowed:
            presence = self.hall_presence.setdefault(hall_id, set())
            if action == AccessAction.entry:
                presence.add(user_id)
            elif action == AccessAction.exit:
                presence.discard(user_id)
            
            hall.current_occupancy = len(presence)
            
        log = AccessLogRecord(
            id=str(uuid.uuid4()),
            user_id=user_id,
            hall_id=hall_id,
            action=action,
            timestamp=datetime.utcnow(),
            allowed=allowed,
            reason=reason
        )
        self.access_logs.append(log)
        return log

    def create_team(self, team: TeamResponse) -> TeamResponse:
        self.teams[team.id] = team
        return team

    def update_team(self, team_id: str, updated: TeamResponse) -> TeamResponse:
        self.teams[team_id] = updated
        return updated
        
    def get_team(self, team_id: str) -> Optional[TeamResponse]:
        return self.teams.get(team_id)

    def get_users_in_team(self, team_id: str) -> list[UserResponse]:
        return [u for u in self.users.values() if u.team_id == team_id]

    def create_notification(self, notification: NotificationResponse) -> NotificationResponse:
        self.notifications.append(notification)
        return notification
        
    def get_notifications_for_team(self, team_id: str) -> list[NotificationResponse]:
        return [n for n in self.notifications if n.team_id == team_id]


STORE = InMemoryStore()
