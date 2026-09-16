from datetime import datetime

from fastapi import APIRouter, Depends
from sqlalchemy import func
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.models.models import MealRecord, TransactionRecord
from app.schemas import AnalyticsSummary

router = APIRouter(prefix="/analytics", tags=["analytics"])


@router.get("/daily", response_model=AnalyticsSummary)
def analytics_daily(db: Session = Depends(get_db)) -> AnalyticsSummary:
    today = datetime.utcnow().date()
    total = (
        db.query(func.count(MealRecord.id))
        .filter(func.date(MealRecord.swipe_timestamp) == today)
        .scalar()
    ) or 0
    return AnalyticsSummary(metric="meals_today", total=total, generated_at=datetime.utcnow())


@router.get("/trends", response_model=AnalyticsSummary)
def analytics_trends(db: Session = Depends(get_db)) -> AnalyticsSummary:
    total = db.query(func.count(TransactionRecord.id)).scalar() or 0
    return AnalyticsSummary(metric="transactions_total", total=total, generated_at=datetime.utcnow())
