from datetime import datetime

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.models.models import MealRecord, TransactionRecord, User
from app.schemas import ReportSummary

router = APIRouter(prefix="/reports", tags=["reports"])


@router.get("/meals", response_model=ReportSummary)
def meals_report(db: Session = Depends(get_db)) -> ReportSummary:
    count = db.query(MealRecord).count()
    return ReportSummary(total_records=count, generated_at=datetime.utcnow())


@router.get("/transactions", response_model=ReportSummary)
def transactions_report(db: Session = Depends(get_db)) -> ReportSummary:
    count = db.query(TransactionRecord).count()
    return ReportSummary(total_records=count, generated_at=datetime.utcnow())


@router.get("/users", response_model=ReportSummary)
def users_report(db: Session = Depends(get_db)) -> ReportSummary:
    count = db.query(User).count()
    return ReportSummary(total_records=count, generated_at=datetime.utcnow())


@router.get("/export")
def export_report(db: Session = Depends(get_db)) -> dict[str, str]:
    meals_count = db.query(MealRecord).count()
    txns_count = db.query(TransactionRecord).count()
    users_count = db.query(User).count()
    timestamp = datetime.utcnow().strftime("%Y-%m-%d_%H-%M")
    filename = f"mianu_export_{timestamp}.csv"
    return {
        "status": "ready",
        "path": f"/downloads/{filename} ({users_count} delegates, {meals_count} meals, {txns_count} txns)",
    }
