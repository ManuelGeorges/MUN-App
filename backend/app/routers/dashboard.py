from datetime import datetime, time

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import func
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.models.models import MealRecord, TransactionRecord, User
from app.schemas import DashboardOverview, MealWindowRequest, MealType
from app.store import MealWindow, STORE

router = APIRouter(prefix="/dashboard", tags=["dashboard"])


@router.get("/overview", response_model=DashboardOverview)
def overview(db: Session = Depends(get_db)) -> DashboardOverview:
    """Event totals.

    Reads the database rather than the in-memory store: users, meals and transactions are all
    persisted there, and `STORE.users` is never written to, so this reported zero users and a zero
    balance on every request.
    """
    today = datetime.utcnow().date()

    meals_today = (
        db.query(func.count(MealRecord.id))
        .filter(func.date(MealRecord.swipe_timestamp) == today)
        .scalar()
    )
    txns_today = (
        db.query(func.count(TransactionRecord.id))
        .filter(func.date(TransactionRecord.created_at) == today)
        .scalar()
    )
    active_users = db.query(func.count(User.id)).filter(User.is_active.is_(True)).scalar()
    total_balance = db.query(func.coalesce(func.sum(User.token_balance), 0.0)).scalar()

    return DashboardOverview(
        total_meals_today=meals_today or 0,
        total_transactions_today=txns_today or 0,
        active_users=active_users or 0,
        total_token_balance=float(total_balance or 0.0),
    )


@router.put("/settings/meal-windows")
def update_meal_windows(payload: MealWindowRequest) -> dict[str, MealWindowRequest]:
    def parse_time(value: str) -> time:
        try:
            hour, minute = value.split(":")
            return time(int(hour), int(minute))
        except ValueError as exc:
            raise HTTPException(status_code=400, detail="Invalid time format") from exc

    STORE.meal_windows[MealType.breakfast] = MealWindow(
        start=parse_time(payload.breakfast_start),
        end=parse_time(payload.breakfast_end),
    )
    STORE.meal_windows[MealType.lunch] = MealWindow(
        start=parse_time(payload.lunch_start),
        end=parse_time(payload.lunch_end),
    )
    return {"settings": payload}


@router.get("/analytics/daily")
def analytics_daily() -> dict[str, str]:
    return {"status": "moved_to_/analytics/daily"}


@router.get("/analytics/trends")
def analytics_trends() -> dict[str, str]:
    return {"status": "moved_to_/analytics/trends"}
